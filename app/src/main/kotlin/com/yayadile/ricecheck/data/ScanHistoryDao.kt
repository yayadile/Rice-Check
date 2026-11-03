package com.yayadile.ricecheck.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScanHistory(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanHistoryById(id: Long): ScanHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistory(entity: ScanHistoryEntity): Long

    @Update
    suspend fun updateScanHistory(entity: ScanHistoryEntity)

    @Delete
    suspend fun deleteScanHistory(entity: ScanHistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}

