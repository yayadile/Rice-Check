package com.yayadile.ricecheck.ml

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BleachClassifierGoldenTest {

    @Test
    fun `exported asset reproduces the trained svm decisions`() {
        val classifier = BleachClassifier.fromBytes(GoldenResources.assetBytes(RiceClassifier.SVM_ASSET_NAME))
        val cases = GoldenResources.jsonArray("/golden/svm/manifest.json")
        var worst = 0.0

        for (element in cases) {
            val obj = element.asJsonObject
            val name = obj["name"].asString
            val expectedDecision = obj["decision"].asDouble
            val expectedClass = obj["class"].asInt

            val features = GoldenResources.floats("/golden/svm/features/$name.f32")
            val decision = classifier.decision(features)
            val cls = if (decision > 0f) 1 else 0

            assertEquals("$name class", expectedClass, cls)
            val diff = abs(expectedDecision - decision)
            worst = maxOf(worst, diff)
            assertTrue("$name decision diff $diff", diff <= 1e-3)
        }
        println("BleachClassifier golden worst diff = $worst")
    }
}
