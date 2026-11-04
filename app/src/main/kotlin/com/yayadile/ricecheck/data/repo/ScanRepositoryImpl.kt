package com.yayadile.ricecheck.data.repo

import android.graphics.BitmapFactory
import com.yayadile.ricecheck.data.local.ScanDao
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.domain.VerdictCalculator
import com.yayadile.ricecheck.ml.RiceClassifier
import com.yayadile.ricecheck.util.ImageDecoder
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Room + Files + LiteRT glue (docs/architecture.md §4): the verdict is computed here,
 * never in the UI and never by the model directly.
 */
class ScanRepositoryImpl(
    private val dao: ScanDao,
    private val classifier: RiceClassifier,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ScanRepository {

    private companion object {
        const val MAX_DECODE_SIZE = 1920
    }

    override fun observeAll(): Flow<List<ScanResultEntity>> = dao.getAll()

    override fun observeById(id: Long): Flow<ScanResultEntity?> = dao.getById(id)

    override fun observeByResult(result: String): Flow<List<ScanResultEntity>> =
        dao.getByResult(result)

    override fun countSince(since: Long): Flow<Int> = dao.countSince(since)

    override suspend fun save(photoPath: String, price: Int?): Long = withContext(ioDispatcher) {
        // Cap the decode size: the detector needs 1280 and the SVM 256, so ~1920 keeps
        // full fidelity while avoiding OOM on 48 MP camera photos.
        val bitmap = ImageDecoder.decode(photoPath, targetSizePx = MAX_DECODE_SIZE)
            ?: throw IOException("Foto tidak bisa dibaca: $photoPath")
        val report = classifier.classify(bitmap)
        val verdict = VerdictCalculator.decide(price, report)

        dao.insert(
            ScanResultEntity(
                photoPath = photoPath,
                price = price,
                result = verdict,
                qualityScore = report.qualityScore,
                wholeGrainPct = report.wholeGrainPct,
                brokenPct = report.brokenPct,
                bleachLevel = report.bleachLevel.name,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun delete(id: Long) {
        withContext(ioDispatcher) {
            val entity = dao.getById(id).first()
            dao.deleteById(id)
            entity?.photoPath?.let { path ->
                runCatching { File(path).delete() }
            }
        }
    }
}
