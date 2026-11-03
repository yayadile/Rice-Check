package com.yayadile.ricecheck.domain

/**
 * Pure verdict logic — docs/architecture.md §4b.
 *
 * Price vs quality, not image-based judgement:
 *  - price null                      -> LAPORAN_KUALITAS
 *  - premium (>= 14_000) + low (<60) -> BERPOTENSI_OPLOSAN
 *  - premium (>= 14_000) + high      -> ASLI
 *  - cheap (< 14_000)                -> LAPORAN_KUALITAS
 *
 * The returned strings are the values stored in `scan_results.result`.
 */
object VerdictCalculator {
    const val ASLI = "ASLI"
    const val BERPOTENSI_OPLOSAN = "BERPOTENSI_OPLOSAN"
    const val LAPORAN_KUALITAS = "LAPORAN_KUALITAS"

    fun decide(price: Int?, q: QualityReport): String = when {
        price == null -> LAPORAN_KUALITAS
        price >= VerdictConfig.PREMIUM_PRICE && q.qualityScore < VerdictConfig.LOW_QUALITY ->
            BERPOTENSI_OPLOSAN
        price >= VerdictConfig.PREMIUM_PRICE -> ASLI
        else -> LAPORAN_KUALITAS   // harga murah: tampilkan kualitas saja, tanpa vonis
    }
}
