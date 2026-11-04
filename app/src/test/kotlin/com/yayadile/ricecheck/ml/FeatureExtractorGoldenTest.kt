package com.yayadile.ricecheck.ml

import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class FeatureExtractorGoldenTest {

    private val tolerance = 1e-3

    @Test
    fun `matches scikit-image opencv golden features`() {
        val cases = GoldenResources.jsonArray("/golden/svm/manifest.json")
        var worst = 0.0

        for (element in cases) {
            val name = element.asJsonObject["name"].asString
            val bgr = GoldenResources.resourceBytes("/golden/svm/resized/$name.bin")
            val expected = GoldenResources.floats("/golden/svm/features/$name.f32")
            val actual = FeatureExtractor.extractFromResizedBgr(bgr)

            assertEquals(FeatureExtractor.FEATURE_COUNT, actual.size)
            var maxDiff = 0.0
            for (i in expected.indices) maxDiff = maxOf(maxDiff, abs(expected[i] - actual[i]).toDouble())
            worst = maxOf(worst, maxDiff)
            assertTrue("$name feature maxdiff $maxDiff > $tolerance", maxDiff <= tolerance)
        }
        println("FeatureExtractor golden worst diff = $worst")
    }
}
