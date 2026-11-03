package com.yayadile.ricecheck.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.ai.edge.litert.CompiledModel
import com.yayadile.ricecheck.domain.BleachLevel
import com.yayadile.ricecheck.domain.QualityReport
import java.io.Closeable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LiteRT wrapper — docs/architecture.md §4.
 *
 * Loads assets/ricecheck_v1.tflite lazily and runs inference on Dispatchers.Default.
 * The trained model is not in the repo yet, so until it is added this class returns a
 * STUB QualityReport (isStub = true). Do not ship numbers from here as a real analysis.
 */
class RiceClassifier(
    context: Context,
    private val assetName: String = ASSET_NAME
) : Closeable {

    private val appContext = context.applicationContext

    /** Loaded once, on first use. Null when the model asset is missing or invalid. */
    private val model: CompiledModel? by lazy {
        runCatching { CompiledModel.create(appContext.assets, assetName) }
            .onFailure { Log.w(TAG, "Gagal memuat $assetName — memakai output stub.", it) }
            .getOrNull()
    }

    val isModelAvailable: Boolean
        get() = model != null

    /**
     * Returns the quality report for a captured photo.
     * Runs off the main thread; preprocessing (224×224 + normalisasi 0..1) already happens here.
     */
    suspend fun classify(bitmap: Bitmap): QualityReport = withContext(Dispatchers.Default) {
        val input = preprocess(bitmap)

        // TODO(ml): once ricecheck_v1.tflite ships, run:
        //   val inputs = model.createInputBuffers().also { it[0].writeFloat(input) }
        //   val outputs = model.createOutputBuffers()
        //   model.run(inputs, outputs) -> map logits to QualityReport(isStub = false)
        if (model != null) {
            Log.d(TAG, "Model tersedia; inferensi belum diimplementasikan, input=${input.size} float.")
        }
        stubReport()
    }

    /** Resize to the model input size and normalise to 0..1 RGB floats. */
    private fun preprocess(bitmap: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        if (scaled !== bitmap) scaled.recycle()

        val output = FloatArray(pixels.size * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            output[i * 3] = ((pixel shr 16) and 0xFF) / 255f
            output[i * 3 + 1] = ((pixel shr 8) and 0xFF) / 255f
            output[i * 3 + 2] = (pixel and 0xFF) / 255f
        }
        return output
    }

    private fun stubReport(): QualityReport = QualityReport(
        qualityScore = 72,
        wholeGrainPct = 82,
        brokenPct = 18,
        bleachLevel = BleachLevel.RENDAH,
        isStub = true
    )

    override fun close() {
        runCatching { model?.close() }
    }

    companion object {
        const val ASSET_NAME = "ricecheck_v1.tflite"
        const val INPUT_SIZE = 224
        private const val TAG = "RiceClassifier"
    }
}
