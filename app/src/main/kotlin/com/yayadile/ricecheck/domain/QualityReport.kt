package com.yayadile.ricecheck.domain

/**
 * Output of the quality model. It is NOT an oplosan classifier — the verdict is
 * derived from price vs quality in [VerdictCalculator].
 */
data class QualityReport(
    val qualityScore: Int,        // 0..100
    val wholeGrainPct: Int,       // % butir utuh
    val brokenPct: Int,           // % menir/patah
    val bleachLevel: BleachLevel,  // indikasi pemutih
    /**
     * True while the bundled model is still a placeholder. Screens must not present
     * stub numbers as a real analysis.
     */
    val isStub: Boolean = false
)

enum class BleachLevel {
    RENDAH,
    TINGGI
}
