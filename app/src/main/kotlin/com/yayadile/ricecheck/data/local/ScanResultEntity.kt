package com.yayadile.ricecheck.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single table for v1 — docs/architecture.md §2.
 */
@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,          // path file di internal storage
    val price: Int?,                // Rp/kg, null = tidak diisi
    val result: String,             // "ASLI" | "BERPOTENSI_OPLOSAN" | "LAPORAN_KUALITAS"
    val qualityScore: Int,          // 0..100
    val wholeGrainPct: Int,         // % butir utuh
    val brokenPct: Int,             // % menir/patah
    val bleachLevel: String,        // "RENDAH" | "TINGGI"
    val bleachPct: Int?,            // 0..100 keyakinan model; null untuk scan lama (pra-v2)
    val createdAt: Long             // epoch millis
)
