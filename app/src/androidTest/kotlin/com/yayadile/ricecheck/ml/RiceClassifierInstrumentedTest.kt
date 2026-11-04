package com.yayadile.ricecheck.ml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yayadile.ricecheck.domain.BleachLevel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device end-to-end checks for the real ML pipeline (LiteRT + Kotlin SVM).
 *
 * Reference values come from Ultralytics `predict` (conf 0.25 / iou 0.45) and the trained
 * sklearn SVM; the TFLite/Kotlin implementation is allowed to differ by a small margin.
 */
@RunWith(AndroidJUnit4::class)
class RiceClassifierInstrumentedTest {

    private val target = InstrumentationRegistry.getInstrumentation().targetContext

    private fun bitmap(asset: String): Bitmap =
        InstrumentationRegistry.getInstrumentation().context.assets.open(asset).use {
            requireNotNull(BitmapFactory.decodeStream(it)) { "cannot decode $asset" }
        }

    private fun argb(bitmap: Bitmap): IntArray =
        IntArray(bitmap.width * bitmap.height).also {
            bitmap.getPixels(it, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        }

    @Test
    fun models_load_and_a_real_photo_produces_a_report() {
        val classifier = RiceClassifier(target)
        try {
            assertTrue("both model assets must load", classifier.isModelAvailable)
            val report = runBlocking { classifier.classify(bitmap("rice_test_00.jpg")) }
            println(
                "REPORT score=${report.qualityScore} whole=${report.wholeGrainPct} " +
                    "broken=${report.brokenPct} bleach=${report.bleachLevel} stub=${report.isStub}"
            )
            assertFalse(report.isStub)
            assertTrue(report.qualityScore in 0..100)
            assertTrue(report.wholeGrainPct in 0..100)
            assertTrue(report.brokenPct in 0..100)
            // Reference for rice_test_00.jpg: whole 33, broken 67, score 25, bleach RENDAH.
            assertTrue("wholeGrainPct=${report.wholeGrainPct}", report.wholeGrainPct in 15..50)
            assertTrue("brokenPct=${report.brokenPct}", report.brokenPct in 50..85)
            assertTrue("qualityScore=${report.qualityScore}", report.qualityScore in 5..45)
            assertEquals(BleachLevel.RENDAH, report.bleachLevel)
        } finally {
            classifier.close()
        }
    }

    @Test
    fun yolo_counts_match_the_reference_closely() {
        val detector = YoloDetector(target)
        try {
            val bmp = bitmap("rice_test_00.jpg")
            val counts = YoloPost.counts(detector.detect(argb(bmp), bmp.width, bmp.height))
            println(
                "COUNTS utuh=${counts[0]} patah=${counts[1]} menir=${counts[2]} " +
                    "gabah=${counts[3]} batu=${counts[4]}"
            )
            // Ultralytics reference: utuh 7, patah 9, menir 5, gabah 4, batu 3.
            assertTrue("must detect grains", counts.sum() > 0)
            assertTrue("utuh", counts[0] in 4..11)
            assertTrue("patah", counts[1] in 5..14)
            assertTrue("menir", counts[2] in 2..9)
            assertTrue("gabah", counts[3] in 1..7)
            assertTrue("batu", counts[4] in 0..6)
        } finally {
            detector.close()
        }
    }

    @Test
    fun report_metrics_follow_the_approved_mapping() {
        val detector = YoloDetector(target)
        try {
            val bmp = bitmap("rice_test_01.jpg")
            val counts = YoloPost.counts(detector.detect(argb(bmp), bmp.width, bmp.height))
            val expected = QualityMapping.fromCounts(counts)
            val report = runBlocking { RiceClassifier(target).classify(bmp) }
            println("MAPPING counts=${counts.toList()} expected=$expected report=$report")
            assertEquals(expected.qualityScore, report.qualityScore)
            assertEquals(expected.wholeGrainPct, report.wholeGrainPct)
            assertEquals(expected.brokenPct, report.brokenPct)
        } finally {
            detector.close()
        }
    }
}
