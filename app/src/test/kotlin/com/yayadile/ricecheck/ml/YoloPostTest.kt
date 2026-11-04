package com.yayadile.ricecheck.ml

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YoloPostTest {

    @Test
    fun `letterbox matches ultralytics golden params`() {
        val cases = GoldenResources.jsonArray("/golden/yolo/manifest.json")
        for (element in cases) {
            val obj = element.asJsonObject
            val lb = YoloPost.letterbox(obj["width"].asInt, obj["height"].asInt, obj["imgsz"].asInt)
            assertTrue("scale", abs(lb.scale - obj["scale"].asFloat) < 1e-4f)
            assertEquals("padX", obj["pad_x"].asInt, lb.padX)
            assertEquals("padY", obj["pad_y"].asInt, lb.padY)
        }
    }

    @Test
    fun `decode picks the best class above threshold`() {
        val anchors = 3
        val classes = 5
        val raw = FloatArray((4 + classes) * anchors)
        // anchor 0: below threshold everywhere -> dropped.
        // anchor 1: class 3 with score 0.9, box centre 100,50 size 20x10.
        raw[0 * anchors + 1] = 100f
        raw[1 * anchors + 1] = 50f
        raw[2 * anchors + 1] = 20f
        raw[3 * anchors + 1] = 10f
        raw[(4 + 3) * anchors + 1] = 0.9f
        // anchor 2: class 0 with score 0.5.
        raw[0 * anchors + 2] = 10f
        raw[1 * anchors + 2] = 10f
        raw[2 * anchors + 2] = 4f
        raw[3 * anchors + 2] = 4f
        raw[(4 + 0) * anchors + 2] = 0.5f

        val decoded = YoloPost.decode(raw, anchors, classes, 0.25f)
        assertEquals(2, decoded.size)
        val best = decoded.first { it.cls == 3 }
        assertEquals(90f, best.x1, 1e-4f)
        assertEquals(45f, best.y1, 1e-4f)
        assertEquals(110f, best.x2, 1e-4f)
        assertEquals(55f, best.y2, 1e-4f)
    }

    @Test
    fun `nms suppresses overlapping boxes of the same class only`() {
        val a = YoloPost.Detection(0f, 0f, 10f, 10f, 0, 0.9f)
        val b = YoloPost.Detection(1f, 1f, 11f, 11f, 0, 0.8f)   // ~0.68 IoU with a -> suppressed
        val c = YoloPost.Detection(1f, 1f, 11f, 11f, 1, 0.7f)   // different class -> kept
        val d = YoloPost.Detection(100f, 100f, 110f, 110f, 0, 0.6f) // no overlap -> kept

        val kept = YoloPost.nms(listOf(a, b, c, d), 0.45f, 1000)
        assertEquals(3, kept.size)
        assertTrue(kept.contains(a))
        assertTrue(kept.contains(c))
        assertTrue(kept.contains(d))
        assertTrue(!kept.contains(b))
    }

    @Test
    fun `toOriginal un-letterboxes and clamps`() {
        val lb = YoloPost.Letterbox(1f, 160, 0, 960, 1280)
        val det = YoloPost.Detection(200f, 10f, 300f, 100f, 0, 0.9f)
        val out = YoloPost.toOriginal(listOf(det), lb, 960, 1280)
        assertEquals(40f, out[0].x1, 1e-4f)
        assertEquals(10f, out[0].y1, 1e-4f)
        assertEquals(140f, out[0].x2, 1e-4f)
        assertEquals(100f, out[0].y2, 1e-4f)
    }
}
