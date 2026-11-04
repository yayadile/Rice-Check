package com.yayadile.ricecheck.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO from docs/architecture.md §2.
 * Flow comes from room-runtime directly — room-ktx is not used.
 */
@Dao
interface ScanDao {
    @Insert
    suspend fun insert(result: ScanResultEntity): Long

    @Query("SELECT * FROM scan_results ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE id = :id")
    fun getById(id: Long): Flow<ScanResultEntity?>

    @Query("SELECT * FROM scan_results WHERE result = :result ORDER BY createdAt DESC")
    fun getByResult(result: String): Flow<List<ScanResultEntity>>

    @Query("SELECT COUNT(*) FROM scan_results WHERE createdAt >= :since")
    fun countSince(since: Long): Flow<Int>   // untuk strip "N pemindaian minggu ini"

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM scan_results")
    suspend fun deleteAll()
}
