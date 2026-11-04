package com.yayadile.ricecheck.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.yayadile.ricecheck.domain.BleachLevel
import com.yayadile.ricecheck.domain.QualityReport
import java.io.Closeable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real on-device analysis pipeline (docs/architecture.md §4).
 *
 * YOLO11s detects and counts grains; the bleach SVM decides the bleach level; the approved
 * mapping turns the counts into a [QualityReport]. The verdict itself stays in
 * `VerdictCalculator`.
 *
 * There is no stub anymore: if a model is missing or no grain is detected this throws
 * [AnalysisException] instead of fabricating numbers.
 */
class RiceClassifier(
    context: Context,
    private val yoloAsset: String = YoloDetector.ASSET_NAME,
    private val svmAsset: String = SVM_ASSET_NAME,
) : Closeable {

    private val appContext = context.applicationContext

    private val yolo: YoloDetector? by lazy {
        runCatching { YoloDetector(appContext, yoloAsset) }
            .onFailure { Log.w(TAG, "Gagal memuat $yoloAsset.", it) }
            .getOrNull()
    }

    private val bleach: BleachClassifier? by lazy {
        runCatching {
            appContext.assets.open(svmAsset).use { BleachClassifier.fromBytes(it.readBytes()) }
        }.onFailure { Log.w(TAG, "Gagal memuat $svmAsset.", it) }
            .getOrNull()
    }

    val isModelAvailable: Boolean
        get() = yolo != null && bleach != null

    suspend fun classify(bitmap: Bitmap): QualityReport = withContext(Dispatchers.Default) {
        val detector = yolo ?: throw AnalysisException("Model deteksi tidak tersedia.")
        val classifier = bleach ?: throw AnalysisException("Model kualitas tidak tersedia.")

        val width = bitmap.width
        val height = bitmap.height
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)

        val detections = detector.detect(argb, width, height)
        if (detections.isEmpty()) {
            throw AnalysisException("Tidak ada butir beras yang terdeteksi. Dekatkan kamera dan coba lagi.")
        }
        val counts = YoloPost.counts(detections)
        val metrics = QualityMapping.fromCounts(counts)

        val features = FeatureExtractor.extractFromArgb(argb, width, height)
        val bleachLevel = if (classifier.isPemutih(features)) BleachLevel.TINGGI else BleachLevel.RENDAH

        QualityReport(
            qualityScore = metrics.qualityScore,
            wholeGrainPct = metrics.wholeGrainPct,
            brokenPct = metrics.brokenPct,
            bleachLevel = bleachLevel,
            isStub = false,
        )
    }

    override fun close() {
        runCatching { yolo?.close() }
    }

    companion object {
        const val SVM_ASSET_NAME = "bleach_svm_rbf.bin"
        private const val TAG = "RiceClassifier"
    }
}
