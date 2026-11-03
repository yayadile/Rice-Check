package com.yayadile.ricecheck.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val predictedClass: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

