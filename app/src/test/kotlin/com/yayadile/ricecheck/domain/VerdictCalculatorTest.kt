package com.yayadile.ricecheck.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Full decision matrix from docs/features.md:
 *
 *  Mahal (>= 14.000) + Rendah (< 60)  -> BERPOTENSI_OPLOSAN
 *  Mahal (>= 14.000) + Tinggi (>= 60) -> ASLI
 *  Murah (< 14.000), kualitas apapun  -> LAPORAN_KUALITAS
 *  Harga tidak diisi, kualitas apapun -> LAPORAN_KUALITAS
 */
class VerdictCalculatorTest {

    private fun report(score: Int) = QualityReport(
        qualityScore = score,
        wholeGrainPct = 80,
        brokenPct = 20,
        bleachLevel = BleachLevel.RENDAH,
        bleachPct = 10
    )

    @Test
    fun `premium price with low quality is BERPOTENSI_OPLOSAN`() {
        assertEquals(
            VerdictCalculator.BERPOTENSI_OPLOSAN,
            VerdictCalculator.decide(14_000, report(59))
        )
    }

    @Test
    fun `premium price with very low quality is BERPOTENSI_OPLOSAN`() {
        assertEquals(
            VerdictCalculator.BERPOTENSI_OPLOSAN,
            VerdictCalculator.decide(16_000, report(0))
        )
    }

    @Test
    fun `premium price with high quality is ASLI`() {
        assertEquals(
            VerdictCalculator.ASLI,
            VerdictCalculator.decide(14_000, report(60))
        )
    }

    @Test
    fun `premium price with maximum quality is ASLI`() {
        assertEquals(
            VerdictCalculator.ASLI,
            VerdictCalculator.decide(20_000, report(100))
        )
    }

    @Test
    fun `cheap price with low quality is LAPORAN_KUALITAS`() {
        assertEquals(
            VerdictCalculator.LAPORAN_KUALITAS,
            VerdictCalculator.decide(13_999, report(10))
        )
    }

    @Test
    fun `cheap price with high quality is LAPORAN_KUALITAS`() {
        assertEquals(
            VerdictCalculator.LAPORAN_KUALITAS,
            VerdictCalculator.decide(5_000, report(95))
        )
    }

    @Test
    fun `null price with low quality is LAPORAN_KUALITAS`() {
        assertEquals(
            VerdictCalculator.LAPORAN_KUALITAS,
            VerdictCalculator.decide(null, report(0))
        )
    }

    @Test
    fun `null price with high quality is LAPORAN_KUALITAS`() {
        assertEquals(
            VerdictCalculator.LAPORAN_KUALITAS,
            VerdictCalculator.decide(null, report(100))
        )
    }

    @Test
    fun `premium boundary is inclusive on price and exclusive on quality`() {
        // 13.999 is cheap no matter how bad the quality is.
        assertEquals(
            VerdictCalculator.LAPORAN_KUALITAS,
            VerdictCalculator.decide(13_999, report(1))
        )
        // Exactly 14.000 is premium; score 59 is low -> flagged.
        assertEquals(
            VerdictCalculator.BERPOTENSI_OPLOSAN,
            VerdictCalculator.decide(14_000, report(59))
        )
        // Exactly 14.000 with score exactly 60 is high -> ASLI.
        assertEquals(
            VerdictCalculator.ASLI,
            VerdictCalculator.decide(14_000, report(60))
        )
    }

    @Test
    fun `config holds the documented thresholds`() {
        assertEquals(14_000, VerdictConfig.PREMIUM_PRICE)
        assertEquals(60, VerdictConfig.LOW_QUALITY)
    }
}
