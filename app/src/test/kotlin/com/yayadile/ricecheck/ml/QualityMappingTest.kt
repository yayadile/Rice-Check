package com.yayadile.ricecheck.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class QualityMappingTest {

    @Test
    fun `applies the approved formula`() {
        // utuh, patah, menir, gabah, batu
        val metrics = QualityMapping.fromCounts(intArrayOf(7, 9, 5, 4, 3))
        // grain = 21 -> whole 33, broken 67; total = 28 -> score 25
        assertEquals(33, metrics.wholeGrainPct)
        assertEquals(67, metrics.brokenPct)
        assertEquals(25, metrics.qualityScore)
    }

    @Test
    fun `only foreign objects still yields a score`() {
        val metrics = QualityMapping.fromCounts(intArrayOf(0, 0, 0, 2, 2))
        assertEquals(0, metrics.wholeGrainPct)
        assertEquals(0, metrics.brokenPct)
        assertEquals(0, metrics.qualityScore)
    }
}
