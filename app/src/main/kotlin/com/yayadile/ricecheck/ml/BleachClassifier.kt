package com.yayadile.ricecheck.ml

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.roundToInt

/**
 * Pure-Kotlin reader for the exported bleach SVM (`app/src/main/assets/bleach_svm.bin`).
 *
 * The asset is: standardize with [mean]/[scale], then
 *  - RBF:    `decision = sum(dual_i * exp(-gamma * ||z - sv_i||^2)) + bias`
 *  - linear: `decision = sum(dual_i * dot(z, sv_i)) + bias`
 * A positive decision means class 1 (`pemutih`).
 */
class BleachClassifier private constructor(
    private val kernel: Kernel,
    private val gamma: Float,
    private val bias: Float,
    private val mean: FloatArray,
    private val scale: FloatArray,
    private val support: FloatArray,
    private val dual: FloatArray,
    private val nSupportVectors: Int,
    private val nFeatures: Int,
) {

    enum class Kernel { RBF, LINEAR }

    /** Precomputed `sum(dual_i * sv_i)` so the linear decision is just a dot product. */
    private val linearWeights: FloatArray? =
        if (kernel == Kernel.LINEAR) {
            val w = FloatArray(nFeatures)
            for (s in 0 until nSupportVectors) {
                val base = s * nFeatures
                val d = dual[s]
                for (f in 0 until nFeatures) w[f] += d * support[base + f]
            }
            w
        } else {
            null
        }

    fun decision(features: FloatArray): Float {
        require(features.size == nFeatures) {
            "expected $nFeatures features, got ${features.size}"
        }
        val z = FloatArray(nFeatures) { (features[it] - mean[it]) / scale[it] }

        if (kernel == Kernel.LINEAR) {
            val w = linearWeights!!
            var sum = 0.0
            for (f in 0 until nFeatures) sum += w[f] * z[f]
            return (sum + bias).toFloat()
        }

        var sum = 0.0
        for (s in 0 until nSupportVectors) {
            val base = s * nFeatures
            var dist = 0.0
            for (f in 0 until nFeatures) {
                val diff = (z[f] - support[base + f]).toDouble()
                dist += diff * diff
            }
            sum += dual[s] * exp(-gamma * dist)
        }
        return (sum + bias).toFloat()
    }

    fun isPemutih(features: FloatArray): Boolean = decision(features) > 0f

    /**
     * Model confidence that the sample is `pemutih`, as a 0..100 percentage.
     *
     * The SVM decision value is a signed margin, not a calibrated probability, so we map it
     * through a stable sigmoid. It is a confidence score, NOT a bleach concentration.
     */
    fun pemutihConfidence(features: FloatArray): Int {
        val d = decision(features).toDouble()
        val probability = if (d >= 0.0) {
            1.0 / (1.0 + exp(-d))
        } else {
            exp(d) / (1.0 + exp(d))
        }
        return (probability * 100.0).roundToInt().coerceIn(0, 100)
    }

    companion object {
        private val MAGIC = byteArrayOf('R'.code.toByte(), 'S'.code.toByte(), 'V'.code.toByte(), 'M'.code.toByte())

        fun fromBytes(bytes: ByteArray): BleachClassifier {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val magic = ByteArray(4).also { buffer.get(it) }
            require(magic.contentEquals(MAGIC)) { "not a bleach SVM asset" }
            val version = buffer.int
            require(version == 1 || version == 2) { "unsupported asset version $version" }
            val nFeatures = buffer.int
            val nSupport = buffer.int
            buffer.int // n_classes
            val kernelRaw = buffer.int
            // Version 1 had a reserved field where version 2 stores the kernel; v1 was rbf.
            val kernel = if (version == 1) Kernel.RBF else if (kernelRaw == 1) Kernel.LINEAR else Kernel.RBF
            val gamma = buffer.float
            val bias = buffer.float
            val mean = FloatArray(nFeatures) { buffer.float }
            val scale = FloatArray(nFeatures) { buffer.float }
            val support = FloatArray(nSupport * nFeatures) { buffer.float }
            val dual = FloatArray(nSupport) { buffer.float }
            return BleachClassifier(kernel, gamma, bias, mean, scale, support, dual, nSupport, nFeatures)
        }
    }
}
