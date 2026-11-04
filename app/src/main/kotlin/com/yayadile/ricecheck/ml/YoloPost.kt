package com.yayadile.ricecheck.ml

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Pure-Kotlin YOLO11 preprocessing/postprocessing (no Android dependencies), matching
 * Ultralytics defaults: letterbox (pad 114, stride 32, scaleup), conf 0.25, class-aware
 * NMS IoU 0.45, max_det 1000.
 */
object YoloPost {

    const val IMG_SIZE = 1280
    const val NUM_CLASSES = 5
    const val NUM_ANCHORS = 33600
    const val CONF_THRESHOLD = 0.25f
    const val IOU_THRESHOLD = 0.45f
    const val MAX_DETECTIONS = 1000
    private const val PAD_VALUE = 114

    data class Detection(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val cls: Int,
        val conf: Float,
    )

    data class Letterbox(val scale: Float, val padX: Int, val padY: Int, val newW: Int, val newH: Int)

    fun letterbox(srcW: Int, srcH: Int, size: Int = IMG_SIZE): Letterbox {
        val scale = min(size.toDouble() / srcW, size.toDouble() / srcH).toFloat()
        val newW = max((srcW * scale).roundToInt(), 1)
        val newH = max((srcH * scale).roundToInt(), 1)
        val padX = ((size - newW) / 2.0 - 0.1).roundToInt()
        val padY = ((size - newH) / 2.0 - 0.1).roundToInt()
        return Letterbox(scale, padX, padY, newW, newH)
    }

    /** ARGB pixels -> NHWC RGB float in [0,1], letterboxed to [size, size, 3]. */
    fun toInputTensor(argb: IntArray, srcW: Int, srcH: Int, size: Int = IMG_SIZE): FloatArray {
        val lb = letterbox(srcW, srcH, size)
        val resized = resizeBilinearArgb(argb, srcW, srcH, lb.newW, lb.newH)
        val tensor = FloatArray(size * size * 3)
        val pad = PAD_VALUE / 255f
        for (i in tensor.indices) tensor[i] = pad
        for (y in 0 until lb.newH) {
            val ty = y + lb.padY
            if (ty < 0 || ty >= size) continue
            val srcBase = y * lb.newW
            val dstBase = (ty * size + lb.padX) * 3
            for (x in 0 until lb.newW) {
                val tx = x + lb.padX
                if (tx < 0 || tx >= size) continue
                val p = resized[srcBase + x]
                val o = dstBase + x * 3
                tensor[o] = ((p ushr 16) and 0xFF) / 255f
                tensor[o + 1] = ((p ushr 8) and 0xFF) / 255f
                tensor[o + 2] = (p and 0xFF) / 255f
            }
        }
        return tensor
    }

    /** Decode a `[1, 4+classes, anchors]` raw output into boxes in letterbox coordinates. */
    fun decode(
        raw: FloatArray,
        anchors: Int = NUM_ANCHORS,
        classes: Int = NUM_CLASSES,
        confThreshold: Float = CONF_THRESHOLD,
    ): List<Detection> {
        val out = ArrayList<Detection>()
        for (i in 0 until anchors) {
            var best = confThreshold
            var bestClass = -1
            for (c in 0 until classes) {
                val score = raw[(4 + c) * anchors + i]
                if (score > best) {
                    best = score
                    bestClass = c
                }
            }
            if (bestClass < 0) continue
            val cx = raw[i]
            val cy = raw[anchors + i]
            val w = raw[2 * anchors + i]
            val h = raw[3 * anchors + i]
            out.add(Detection(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f, bestClass, best))
        }
        return out
    }

    /** Class-aware NMS. */
    fun nms(
        detections: List<Detection>,
        iouThreshold: Float = IOU_THRESHOLD,
        maxDetections: Int = MAX_DETECTIONS,
    ): List<Detection> {
        val sorted = detections.sortedByDescending { it.conf }
        val removed = BooleanArray(sorted.size)
        val kept = ArrayList<Detection>(min(maxDetections, sorted.size))
        for (i in sorted.indices) {
            if (removed[i]) continue
            val a = sorted[i]
            kept.add(a)
            if (kept.size >= maxDetections) break
            for (j in i + 1 until sorted.size) {
                if (removed[j]) continue
                val b = sorted[j]
                if (b.cls != a.cls) continue
                if (iou(a, b) > iouThreshold) removed[j] = true
            }
        }
        return kept
    }

    fun toOriginal(detections: List<Detection>, lb: Letterbox, srcW: Int, srcH: Int): List<Detection> =
        detections.map { d ->
            val x1 = ((d.x1 - lb.padX) / lb.scale).coerceIn(0f, srcW.toFloat())
            val y1 = ((d.y1 - lb.padY) / lb.scale).coerceIn(0f, srcH.toFloat())
            val x2 = ((d.x2 - lb.padX) / lb.scale).coerceIn(0f, srcW.toFloat())
            val y2 = ((d.y2 - lb.padY) / lb.scale).coerceIn(0f, srcH.toFloat())
            Detection(x1, y1, x2, y2, d.cls, d.conf)
        }

    fun counts(detections: List<Detection>, classes: Int = NUM_CLASSES): IntArray {
        val counts = IntArray(classes)
        for (d in detections) if (d.cls in 0 until classes) counts[d.cls]++
        return counts
    }

    private fun iou(a: Detection, b: Detection): Float {
        val x1 = max(a.x1, b.x1)
        val y1 = max(a.y1, b.y1)
        val x2 = min(a.x2, b.x2)
        val y2 = min(a.y2, b.y2)
        val inter = max(0f, x2 - x1) * max(0f, y2 - y1)
        val areaA = max(0f, a.x2 - a.x1) * max(0f, a.y2 - a.y1)
        val areaB = max(0f, b.x2 - b.x1) * max(0f, b.y2 - b.y1)
        val union = areaA + areaB - inter
        return if (union <= 0f) 0f else inter / union
    }

    private fun resizeBilinearArgb(src: IntArray, sw: Int, sh: Int, dw: Int, dh: Int): IntArray {
        if (sw == dw && sh == dh) return src
        val out = IntArray(dw * dh)
        val xScale = if (dw > 1) (sw - 1).toDouble() / (dw - 1) else 0.0
        val yScale = if (dh > 1) (sh - 1).toDouble() / (dh - 1) else 0.0
        for (dy in 0 until dh) {
            val sy = dy * yScale
            val y0 = floor(sy).toInt().coerceIn(0, sh - 1)
            val y1 = (y0 + 1).coerceAtMost(sh - 1)
            val wy = sy - y0
            for (dx in 0 until dw) {
                val sx = dx * xScale
                val x0 = floor(sx).toInt().coerceIn(0, sw - 1)
                val x1 = (x0 + 1).coerceAtMost(sw - 1)
                val wx = sx - x0
                val p00 = src[y0 * sw + x0]
                val p01 = src[y0 * sw + x1]
                val p10 = src[y1 * sw + x0]
                val p11 = src[y1 * sw + x1]
                val a = bilerp((p00 ushr 24) and 0xFF, (p01 ushr 24) and 0xFF, (p10 ushr 24) and 0xFF, (p11 ushr 24) and 0xFF, wx, wy)
                val r = bilerp((p00 ushr 16) and 0xFF, (p01 ushr 16) and 0xFF, (p10 ushr 16) and 0xFF, (p11 ushr 16) and 0xFF, wx, wy)
                val g = bilerp((p00 ushr 8) and 0xFF, (p01 ushr 8) and 0xFF, (p10 ushr 8) and 0xFF, (p11 ushr 8) and 0xFF, wx, wy)
                val b = bilerp(p00 and 0xFF, p01 and 0xFF, p10 and 0xFF, p11 and 0xFF, wx, wy)
                out[dy * dw + dx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return out
    }

    private fun bilerp(p00: Int, p01: Int, p10: Int, p11: Int, wx: Double, wy: Double): Int {
        val top = p00 + (p01 - p00) * wx
        val bottom = p10 + (p11 - p10) * wx
        return (top + (bottom - top) * wy).roundToInt().coerceIn(0, 255)
    }
}
