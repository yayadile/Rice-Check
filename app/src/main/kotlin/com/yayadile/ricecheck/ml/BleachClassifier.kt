package com.yayadile.ricecheck.ml

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp

/**
 * Pure-Kotlin reader for the exported bleach RBF SVM (`app/src/main/assets/bleach_svm_rbf.bin`).
 *
 * The asset is: standardize with [mean]/[scale], then
 * `decision = sum(dual_i * exp(-gamma * ||z - sv_i||^2)) + bias`.
 * A positive decision means class 1 (`pemutih`).
 */
class BleachClassifier private constructor(
    private val gamma: Float,
    private val bias: Float,
    private val mean: FloatArray,
    private val scale: FloatArray,
    private val support: FloatArray,
    private val dual: FloatArray,
    private val nSupportVectors: Int,
    private val nFeatures: Int,
) {

    fun decision(features: FloatArray): Float {
        require(features.size == nFeatures) {
            "expected $nFeatures features, got ${features.size}"
        }
        val z = FloatArray(nFeatures) { (features[it] - mean[it]) / scale[it] }
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

    companion object {
        private val MAGIC = byteArrayOf('R'.code.toByte(), 'S'.code.toByte(), 'V'.code.toByte(), 'M'.code.toByte())

        fun fromBytes(bytes: ByteArray): BleachClassifier {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val magic = ByteArray(4).also { buffer.get(it) }
            require(magic.contentEquals(MAGIC)) { "not a bleach SVM asset" }
            val version = buffer.int
            require(version == 1) { "unsupported asset version $version" }
            val nFeatures = buffer.int
            val nSupport = buffer.int
            buffer.int // n_classes
            buffer.int // reserved
            val gamma = buffer.float
            val bias = buffer.float
            val mean = FloatArray(nFeatures) { buffer.float }
            val scale = FloatArray(nFeatures) { buffer.float }
            val support = FloatArray(nSupport * nFeatures) { buffer.float }
            val dual = FloatArray(nSupport) { buffer.float }
            return BleachClassifier(gamma, bias, mean, scale, support, dual, nSupport, nFeatures)
        }
    }
}
