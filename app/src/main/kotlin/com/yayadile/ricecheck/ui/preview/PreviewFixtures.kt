package com.yayadile.ricecheck.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.domain.VerdictCalculator
import com.yayadile.ricecheck.ui.theme.RiceCheckTheme
import com.yayadile.ricecheck.ui.theme.RiceColors

/**
 * PREVIEW ONLY.
 *
 * These rows exist so IDE previews can render screens without Room, CameraX or DataStore.
 * Production code must never import this object — real rows always come from ScanDao.
 * photoPath is intentionally empty: the thumbnail falls back to the neutral placeholder.
 */
object PreviewFixtures {

    // Fixed timestamp so previews are stable (early Nov 2026).
    private const val BASE_TIME = 1_793_700_000_000L
    private const val ONE_DAY = 86_400_000L

    val asli = ScanResultEntity(
        id = 1,
        photoPath = "",
        price = 15_500,
        result = VerdictCalculator.ASLI,
        qualityScore = 82,
        wholeGrainPct = 88,
        brokenPct = 12,
        bleachLevel = "RENDAH",
        bleachPct = 12,
        createdAt = BASE_TIME
    )

    val berpotensiOplosan = ScanResultEntity(
        id = 2,
        photoPath = "",
        price = 16_000,
        result = VerdictCalculator.BERPOTENSI_OPLOSAN,
        qualityScore = 45,
        wholeGrainPct = 58,
        brokenPct = 42,
        bleachLevel = "TINGGI",
        bleachPct = 88,
        createdAt = BASE_TIME - ONE_DAY
    )

    // Cheap price — the quality report carries no verdict.
    val laporanKualitas = ScanResultEntity(
        id = 3,
        photoPath = "",
        price = 11_500,
        result = VerdictCalculator.LAPORAN_KUALITAS,
        qualityScore = 68,
        wholeGrainPct = 74,
        brokenPct = 26,
        bleachLevel = "RENDAH",
        bleachPct = 20,
        createdAt = BASE_TIME - 2 * ONE_DAY
    )

    // Empty price — same quality-report state, price shown as "Tidak diisi".
    val laporanTanpaHarga = laporanKualitas.copy(id = 4, price = null)

    val recentScans = listOf(berpotensiOplosan, asli, laporanKualitas)
}

/** Preview-only scaffold: theme + app background, no Application/AppContainer required. */
@Composable
fun PreviewSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    RiceCheckTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = RiceColors.Background
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RiceColors.Background)
            ) {
                content()

                // Visible reminder that the data on screen is fake, not a real scan.
                androidx.compose.material3.Text(
                    text = "PREVIEW DATA",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = RiceColors.TextSecondary,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopEnd)
                        .padding(6.dp)
                        .background(RiceColors.Cream, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
