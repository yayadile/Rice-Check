package com.yayadile.ricecheck.ml

import kotlin.math.roundToInt

/**
 * Approved mapping from per-class grain counts to the quality metrics shown in the UI.
 *
 * counts order: 0 beras_utuh, 1 beras_patah, 2 menir, 3 gabah, 4 batu.
 */
object QualityMapping {

    data class Metrics(val qualityScore: Int, val wholeGrainPct: Int, val brokenPct: Int)

    fun fromCounts(counts: IntArray): Metrics {
        val whole = counts.getOrElse(0) { 0 }
        val broken = counts.getOrElse(1) { 0 }
        val menir = counts.getOrElse(2) { 0 }
        val gabah = counts.getOrElse(3) { 0 }
        val batu = counts.getOrElse(4) { 0 }

        val grain = whole + broken + menir
        val total = grain + gabah + batu
        val wholePct = if (grain > 0) (100.0 * whole / grain).roundToInt() else 0
        val brokenPct = if (grain > 0) (100.0 * (broken + menir) / grain).roundToInt() else 0
        val score = if (total > 0) (100.0 * whole / total).roundToInt() else 0
        return Metrics(score, wholePct, brokenPct)
    }
}
