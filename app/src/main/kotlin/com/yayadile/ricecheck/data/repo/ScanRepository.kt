package com.yayadile.ricecheck.data.repo

import com.yayadile.ricecheck.data.local.ScanResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for scans (docs/architecture.md §1).
 */
interface ScanRepository {
    fun observeAll(): Flow<List<ScanResultEntity>>
    fun observeById(id: Long): Flow<ScanResultEntity?>
    fun observeByResult(result: String): Flow<List<ScanResultEntity>>
    fun countSince(since: Long): Flow<Int>

    /**
     * Classifies the photo, derives the verdict from price vs quality and stores the row.
     * @return the new scan id (used to open result/{scanId})
     */
    suspend fun save(photoPath: String, price: Int?): Long

    /** Deletes the row and its photo file. */
    suspend fun delete(id: Long)
}
