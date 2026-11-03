package com.yayadile.ricecheck.domain

/**
 * Single place for the verdict thresholds (docs/features.md, docs/architecture.md §4b).
 * Change them here only — no other file may hardcode 14_000 or 60.
 */
object VerdictConfig {
    const val PREMIUM_PRICE = 14_000   // Rp/kg
    const val LOW_QUALITY = 60         // skor < 60 = rendah
}
