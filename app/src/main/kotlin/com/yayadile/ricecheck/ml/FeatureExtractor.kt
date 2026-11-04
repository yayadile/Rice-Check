package com.yayadile.ricecheck.ml

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Pure-Kotlin port of the bleach SVM feature pipeline (no Android dependencies).
 *
 * Reproduces `Deteksi Beras/train_rice_bleach.py` exactly: BGR 256x256 -> gray ->
 * Otsu + morphology segmentation -> GLCM (6) + HOG (8100) + color histogram (96).
 *
 * The algorithm was validated against scikit-image/OpenCV golden vectors; see
 * `app/src/test/resources/golden/svm`.
 */
object FeatureExtractor {

    const val INPUT_SIZE = 256
    const val FEATURE_COUNT = 8202

    private const val ORIENTATIONS = 9
    private const val CELL = 16
    private const val BLOCK = 2
    private const val HIST_BINS = 32

    /** Golden-parity entry point: an already 256x256 BGR image (row-major, 3 bytes/px). */
    fun extractFromResizedBgr(bgr: ByteArray, width: Int = INPUT_SIZE, height: Int = INPUT_SIZE): FloatArray {
        val gray = grayFromBgr(bgr, width, height)
        val mask = segment(gray, width, height)
        val graySeg = ByteArray(width * height)
        val segmented = ByteArray(bgr.size)
        for (i in graySeg.indices) {
            if (mask[i].toInt() != 0) {
                graySeg[i] = gray[i]
                segmented[i * 3] = bgr[i * 3]
                segmented[i * 3 + 1] = bgr[i * 3 + 1]
                segmented[i * 3 + 2] = bgr[i * 3 + 2]
            }
        }
        val features = FloatArray(FEATURE_COUNT)
        var o = 0
        o += glcmProps(graySeg, width, height, features, o)
        o += hog(graySeg, width, height, features, o)
        o += colorHistogram(segmented, width, height, features, o)
        check(o == FEATURE_COUNT) { "expected $FEATURE_COUNT features, got $o" }
        return features
    }

    /** App entry point: resizes an ARGB image to 256x256 (area) and extracts features. */
    fun extractFromArgb(argb: IntArray, width: Int, height: Int): FloatArray {
        val resized = resizeAreaArgb(argb, width, height, INPUT_SIZE, INPUT_SIZE)
        val bgr = ByteArray(INPUT_SIZE * INPUT_SIZE * 3)
        for (i in resized.indices) {
            val p = resized[i]
            bgr[i * 3] = (p and 0xFF).toByte()
            bgr[i * 3 + 1] = ((p ushr 8) and 0xFF).toByte()
            bgr[i * 3 + 2] = ((p ushr 16) and 0xFF).toByte()
        }
        return extractFromResizedBgr(bgr)
    }

    private fun grayFromBgr(bgr: ByteArray, width: Int, height: Int): ByteArray {
        val out = ByteArray(width * height)
        for (i in out.indices) {
            val b = bgr[i * 3].toInt() and 0xFF
            val g = bgr[i * 3 + 1].toInt() and 0xFF
            val r = bgr[i * 3 + 2].toInt() and 0xFF
            // OpenCV 5.0 cvtColor BGR2GRAY fixed point: (9798R + 19235G + 3735B + 16384) >> 15.
            out[i] = ((r * 9798 + g * 19235 + b * 3735 + (1 shl 14)) shr 15).toByte()
        }
        return out
    }

    private fun segment(gray: ByteArray, width: Int, height: Int): ByteArray {
        val threshold = otsu(gray)
        var mask = ByteArray(width * height) {
            if ((gray[it].toInt() and 0xFF) > threshold) 255.toByte() else 0
        }
        // OpenCV MORPH_OPEN iterations=1 -> 3x3; MORPH_CLOSE iterations=2 -> single 5x5
        // (iterations>1 grows the structuring element instead of repeating the op).
        mask = dilate(erode(mask, width, height, 1), width, height, 1)
        mask = erode(dilate(mask, width, height, 2), width, height, 2)
        return mask
    }

    private fun otsu(gray: ByteArray): Int {
        val hist = IntArray(256)
        for (v in gray) hist[v.toInt() and 0xFF]++
        val total = gray.size
        var sum = 0.0
        for (i in 0..255) sum += i * hist[i]
        var sumB = 0.0
        var weightB = 0
        var maxVar = -1.0
        var threshold = 0
        for (t in 0..255) {
            weightB += hist[t]
            if (weightB == 0) continue
            val weightF = total - weightB
            if (weightF == 0) break
            sumB += t * hist[t]
            val meanB = sumB / weightB
            val meanF = (sum - sumB) / weightF
            val between = weightB.toDouble() * weightF * (meanB - meanF) * (meanB - meanF)
            if (between > maxVar) {
                maxVar = between
                threshold = t
            }
        }
        return threshold
    }

    private fun erode(src: ByteArray, width: Int, height: Int, radius: Int): ByteArray {
        val out = ByteArray(src.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var minV = 255
                for (dy in -radius..radius) {
                    val yy = y + dy
                    if (yy < 0 || yy >= height) continue
                    for (dx in -radius..radius) {
                        val xx = x + dx
                        if (xx < 0 || xx >= width) continue
                        val v = src[yy * width + xx].toInt() and 0xFF
                        if (v < minV) minV = v
                    }
                }
                out[y * width + x] = minV.toByte()
            }
        }
        return out
    }

    private fun dilate(src: ByteArray, width: Int, height: Int, radius: Int): ByteArray {
        val out = ByteArray(src.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var maxV = 0
                for (dy in -radius..radius) {
                    val yy = y + dy
                    if (yy < 0 || yy >= height) continue
                    for (dx in -radius..radius) {
                        val xx = x + dx
                        if (xx < 0 || xx >= width) continue
                        val v = src[yy * width + xx].toInt() and 0xFF
                        if (v > maxV) maxV = v
                    }
                }
                out[y * width + x] = maxV.toByte()
            }
        }
        return out
    }

    private fun glcmProps(gray: ByteArray, width: Int, height: Int, out: FloatArray, offset: Int): Int {
        // (dr, dc) for angles 0, pi/4, pi/2, 3pi/4 with distance 1.
        val dr = intArrayOf(0, 1, 1, 1)
        val dc = intArrayOf(1, 1, 0, -1)
        val sums = Array(4) { DoubleArray(6) }

        for (a in 0..3) {
            val p = FloatArray(256 * 256)
            for (y in 0 until height) {
                val fy = y + dr[a]
                if (fy < 0 || fy >= height) continue
                val rowBase = y * width
                val nBase = fy * width
                for (x in 0 until width) {
                    val fx = x + dc[a]
                    if (fx < 0 || fx >= width) continue
                    val i = gray[rowBase + x].toInt() and 0xFF
                    val j = gray[nBase + fx].toInt() and 0xFF
                    p[i * 256 + j] += 1f
                }
            }
            // Symmetric: P + P^T (the diagonal is doubled, matching skimage).
            val original = p.copyOf()
            for (i in 0..255) {
                val rowBase = i * 256
                for (j in 0..255) {
                    p[rowBase + j] = original[rowBase + j] + original[j * 256 + i]
                }
            }
            var total = 0.0
            for (v in p) total += v
            val inv = if (total > 0.0) 1.0 / total else 0.0
            for (k in p.indices) p[k] = (p[k] * inv).toFloat()

            var contrast = 0.0
            var dissimilarity = 0.0
            var homogeneity = 0.0
            var asm = 0.0
            var meanI = 0.0
            var meanJ = 0.0
            for (i in 0..255) {
                val base = i * 256
                for (j in 0..255) {
                    val prob = p[base + j].toDouble()
                    if (prob == 0.0) continue
                    val d = i - j
                    contrast += prob * d * d
                    dissimilarity += prob * abs(d)
                    homogeneity += prob / (1.0 + d * d)
                    asm += prob * prob
                    meanI += prob * i
                    meanJ += prob * j
                }
            }
            var varI = 0.0
            var varJ = 0.0
            var cov = 0.0
            for (i in 0..255) {
                val base = i * 256
                val di = i - meanI
                for (j in 0..255) {
                    val prob = p[base + j].toDouble()
                    if (prob == 0.0) continue
                    val dj = j - meanJ
                    varI += prob * di * di
                    varJ += prob * dj * dj
                    cov += prob * di * dj
                }
            }
            val stdI = sqrt(varI)
            val stdJ = sqrt(varJ)
            val correlation = if (stdI < 1e-15 || stdJ < 1e-15) 1.0 else cov / (stdI * stdJ)

            sums[a][0] = contrast
            sums[a][1] = dissimilarity
            sums[a][2] = homogeneity
            sums[a][3] = sqrt(asm)
            sums[a][4] = correlation
            sums[a][5] = asm
        }

        for (k in 0..5) {
            var mean = 0.0
            for (a in 0..3) mean += sums[a][k]
            out[offset + k] = (mean / 4.0).toFloat()
        }
        return 6
    }

    private fun hog(gray: ByteArray, width: Int, height: Int, out: FloatArray, offset: Int): Int {
        val n = width * height
        val img = FloatArray(n) { (gray[it].toInt() and 0xFF).toFloat() }
        val gRow = FloatArray(n)
        val gCol = FloatArray(n)
        for (y in 1 until height - 1) {
            val base = y * width
            for (x in 0 until width) gRow[base + x] = img[base + width + x] - img[base - width + x]
        }
        for (y in 0 until height) {
            val base = y * width
            for (x in 1 until width - 1) gCol[base + x] = img[base + x + 1] - img[base + x - 1]
        }

        val nCellsRow = height / CELL
        val nCellsCol = width / CELL
        val hist = FloatArray(nCellsRow * nCellsCol * ORIENTATIONS)
        for (ci in 0 until nCellsRow) {
            for (cj in 0 until nCellsCol) {
                val cellBase = (ci * nCellsCol + cj) * ORIENTATIONS
                for (yy in ci * CELL until (ci + 1) * CELL) {
                    val rowBase = yy * width
                    for (xx in cj * CELL until (cj + 1) * CELL) {
                        val idx = rowBase + xx
                        val mag = hypot(gRow[idx].toDouble(), gCol[idx].toDouble())
                        var ang = atan2(gRow[idx].toDouble(), gCol[idx].toDouble())
                        if (ang < 0.0) ang += Math.PI
                        var bin = floor(ang / Math.PI * ORIENTATIONS).toInt()
                        if (bin == ORIENTATIONS) bin = 0
                        hist[cellBase + bin] += mag.toFloat()
                    }
                }
            }
        }

        val nBlocksRow = nCellsRow - BLOCK + 1
        val nBlocksCol = nCellsCol - BLOCK + 1
        val block = FloatArray(BLOCK * BLOCK * ORIENTATIONS)
        var o = offset
        for (r in 0 until nBlocksRow) {
            for (c in 0 until nBlocksCol) {
                var k = 0
                for (bi in 0 until BLOCK) {
                    for (bj in 0 until BLOCK) {
                        val cellBase = ((r + bi) * nCellsCol + (c + bj)) * ORIENTATIONS
                        for (oo in 0 until ORIENTATIONS) block[k++] = hist[cellBase + oo]
                    }
                }
                var norm = 0.0
                for (v in block) norm += v.toDouble() * v
                val inv = 1.0 / sqrt(norm + 1e-10)
                for (i in block.indices) {
                    val v = block[i] * inv
                    block[i] = if (v > 0.2) 0.2f else v.toFloat()
                }
                var norm2 = 0.0
                for (v in block) norm2 += v.toDouble() * v
                val inv2 = 1.0 / sqrt(norm2 + 1e-10)
                for (i in block.indices) out[o++] = (block[i] * inv2).toFloat()
            }
        }
        return o - offset
    }

    private fun colorHistogram(bgr: ByteArray, width: Int, height: Int, out: FloatArray, offset: Int): Int {
        val n = width * height
        for (channel in 0..2) {
            val hist = FloatArray(HIST_BINS)
            for (i in 0 until n) {
                val v = bgr[i * 3 + channel].toInt() and 0xFF
                hist[v * HIST_BINS / 256] += 1f
            }
            var norm = 0.0
            for (v in hist) norm += v.toDouble() * v
            val inv = if (norm > 0.0) 1.0 / sqrt(norm) else 0.0
            for (b in 0 until HIST_BINS) out[offset + channel * HIST_BINS + b] = (hist[b] * inv).toFloat()
        }
        return 3 * HIST_BINS
    }

    private fun resizeAreaArgb(src: IntArray, sw: Int, sh: Int, dw: Int, dh: Int): IntArray {
        val out = IntArray(dw * dh)
        val xScale = sw.toDouble() / dw
        val yScale = sh.toDouble() / dh
        for (dy in 0 until dh) {
            val sy0 = dy * yScale
            val sy1 = (dy + 1) * yScale
            val iy0 = floor(sy0).toInt()
            val iy1 = ceil(sy1).toInt()
            for (dx in 0 until dw) {
                val sx0 = dx * xScale
                val sx1 = (dx + 1) * xScale
                val ix0 = floor(sx0).toInt()
                val ix1 = ceil(sx1).toInt()
                var a = 0.0
                var r = 0.0
                var g = 0.0
                var b = 0.0
                var wsum = 0.0
                for (yy in iy0 until min(iy1, sh)) {
                    val wy = min(sy1, yy + 1.0) - max(sy0, yy.toDouble())
                    if (wy <= 0.0) continue
                    val rowBase = yy * sw
                    for (xx in ix0 until min(ix1, sw)) {
                        val wx = min(sx1, xx + 1.0) - max(sx0, xx.toDouble())
                        if (wx <= 0.0) continue
                        val w = wx * wy
                        val p = src[rowBase + xx]
                        a += w * ((p ushr 24) and 0xFF)
                        r += w * ((p ushr 16) and 0xFF)
                        g += w * ((p ushr 8) and 0xFF)
                        b += w * (p and 0xFF)
                        wsum += w
                    }
                }
                out[dy * dw + dx] = if (wsum > 0.0) {
                    val ai = (a / wsum).roundToInt().coerceIn(0, 255)
                    val ri = (r / wsum).roundToInt().coerceIn(0, 255)
                    val gi = (g / wsum).roundToInt().coerceIn(0, 255)
                    val bi = (b / wsum).roundToInt().coerceIn(0, 255)
                    (ai shl 24) or (ri shl 16) or (gi shl 8) or bi
                } else {
                    0
                }
            }
        }
        return out
    }
}
