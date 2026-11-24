package com.yayadile.ricecheck.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yayadile.ricecheck.R
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.domain.VerdictCalculator
import com.yayadile.ricecheck.domain.VerdictConfig
import com.yayadile.ricecheck.ui.components.RicePrimaryButton
import com.yayadile.ricecheck.ui.components.ScoreRing
import com.yayadile.ricecheck.ui.format.bleachLabel
import com.yayadile.ricecheck.ui.format.formatPricePerKg
import com.yayadile.ricecheck.ui.format.formatScanDate
import com.yayadile.ricecheck.ui.format.resultLabel
import com.yayadile.ricecheck.ui.preview.PreviewFixtures
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.CardLargeShape
import com.yayadile.ricecheck.ui.theme.CardShape
import com.yayadile.ricecheck.ui.theme.PillShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.ScreenPadding
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.viewmodel.ResultViewModel
import com.yayadile.ricecheck.viewmodel.ScanDetailState
import com.yayadile.ricecheck.viewmodel.resultViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Route: result/{scanId} — 3 states derived from the stored verdict (docs/IA.md §4).
 *
 * The quality score ring, the verdict badge and the price line are the only hero elements.
 * Everything else comes straight from the stored scan — no invented stats, no IDs, no toggle.
 */
@Composable
fun ResultScreen(
    scanId: Long,
    onBack: () -> Unit,
    onScanAgain: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = LocalAppContainer.current
    val viewModel: ResultViewModel = viewModel(
        key = "result-$scanId",
        factory = remember(scanId) { resultViewModelFactory(container.scanRepository, scanId) }
    )
    val state by viewModel.state.collectAsState()

    // While the model asset is missing every report is a stub — say so plainly.
    val isStub by produceState(initialValue = false) {
        value = withContext(Dispatchers.IO) { container.isClassifierStub }
    }

    val scan = (state as? ScanDetailState.Loaded)?.scan
    when {
        state is ScanDetailState.Loading -> ResultStatusScreen(
            message = "Memuat hasil analisis…",
            loading = true,
            onBack = onBack,
            modifier = modifier
        )

        scan == null -> ResultStatusScreen(
            message = "Data scan tidak ditemukan.",
            loading = false,
            onBack = onBack,
            modifier = modifier
        )

        else -> ResultContent(
            scan = scan,
            isStub = isStub,
            onBack = onBack,
            onScanAgain = onScanAgain,
            onHistory = onHistory,
            modifier = modifier
        )
    }
}

@Composable
fun ResultContent(
    scan: ScanResultEntity,
    isStub: Boolean,
    onBack: () -> Unit,
    onScanAgain: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RiceColors.Background)
            .navigationBarsPadding()
            .padding(horizontal = ScreenPadding)
    ) {
        ResultTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.xs))

            // Hero — score ring, one-line conclusion, verdict badge and the price line.
            ResultHeroCard(scan = scan)

            // Real metrics only: % whole grain, % broken, bleach indication.
            QualityMetricsCard(scan = scan)

            if (isStub) {
                StubNotice()
            }

            // Date and disclaimer share one caption block — they are the same kind of note.
            Text(
                text = "${formatScanDate(scan.createdAt)} · Hasil estimasi, bukan uji laboratorium.",
                style = MaterialTheme.typography.bodySmall,
                color = RiceColors.TextSecondary
            )

            Spacer(Modifier.height(Spacing.xs))
            RicePrimaryButton(text = "Scan Lagi", onClick = onScanAgain)
            TextButton(
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Lihat Riwayat",
                    style = MaterialTheme.typography.labelLarge,
                    color = RiceColors.Primary
                )
            }

            Spacer(Modifier.height(Spacing.md))
        }
    }
}

/**
 * Small logo in the top app bar — the only branding on the screen, per the design system.
 */
@Composable
private fun ResultTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Kembali",
                tint = RiceColors.Text
            )
        }
        Image(
            painter = painterResource(R.drawable.logo_ricecheck),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "Hasil Analisis",
            style = MaterialTheme.typography.titleMedium,
            color = RiceColors.Text,
            modifier = Modifier.padding(start = Spacing.sm)
        )
    }
}

/**
 * The ring follows the score, using the same threshold as the verdict formula:
 * below 60 = red, 60 and above = green. The palette only has these two accents.
 */
private fun scoreAccent(score: Int): Color =
    if (score < VerdictConfig.LOW_QUALITY) RiceColors.Danger else RiceColors.Primary

/** The verdict badge shown in the hero; a quality report has no verdict, so it gets none. */
private data class VerdictStyle(val label: String, val background: Color)

private fun verdictBadge(result: String): VerdictStyle? = when (result) {
    VerdictCalculator.ASLI -> VerdictStyle(resultLabel(result), RiceColors.Primary)
    VerdictCalculator.BERPOTENSI_OPLOSAN -> VerdictStyle(resultLabel(result), RiceColors.Danger)
    else -> null
}

/** One plain sentence built from the real metrics, so the score never stands alone. */
private fun qualityConclusion(scan: ScanResultEntity): String =
    if (scan.qualityScore < VerdictConfig.LOW_QUALITY) {
        "Kualitas rendah — ${scan.brokenPct}% butir patah."
    } else {
        "Kualitas baik — ${scan.wholeGrainPct}% butir utuh."
    }

@Composable
private fun ResultHeroCard(
    scan: ScanResultEntity,
    modifier: Modifier = Modifier
) {
    val badge = verdictBadge(scan.result)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RiceColors.Surface, CardLargeShape)
            .border(1.dp, RiceColors.Outline, CardLargeShape)
            .padding(horizontal = Spacing.md, vertical = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScoreRing(score = scan.qualityScore, accent = scoreAccent(scan.qualityScore))

        Spacer(Modifier.height(Spacing.md))
        Text(
            text = qualityConclusion(scan),
            style = MaterialTheme.typography.bodyMedium,
            color = RiceColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(Spacing.md))
        if (badge != null) {
            HeroBadge(text = badge.label, background = badge.background)
        } else {
            Text(
                text = "Laporan Kualitas",
                style = MaterialTheme.typography.headlineSmall,
                color = RiceColors.Text,
                textAlign = TextAlign.Center
            )
        }

        // Price is an input, not a result: a small line under the verdict, gone when empty.
        if (scan.price != null) {
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "${formatPricePerKg(scan.price)} • Skor ${scan.qualityScore} → ${resultLabel(scan.result)}",
                style = MaterialTheme.typography.bodySmall,
                color = RiceColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HeroBadge(text: String, background: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = RiceColors.OnPrimary,
        modifier = Modifier
            .background(background, PillShape)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
    )
}

@Composable
private fun QualityMetricsCard(
    scan: ScanResultEntity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RiceColors.Surface, CardShape)
            .border(1.dp, RiceColors.Outline, CardShape)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(RiceColors.SurfaceGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_analytics),
                    contentDescription = null,
                    tint = RiceColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.size(Spacing.sm))
            Text(
                text = "Rincian Kualitas",
                style = MaterialTheme.typography.headlineSmall,
                color = RiceColors.Text
            )
        }

        MetricBar(
            label = "Butir Utuh",
            valueText = "${scan.wholeGrainPct}%",
            progress = scan.wholeGrainPct / 100f,
            color = RiceColors.Primary
        )
        MetricBar(
            label = "Menir / Patah",
            valueText = "${scan.brokenPct}%",
            progress = scan.brokenPct / 100f,
            color = RiceColors.Danger
        )

        // Pemutih is a level, not a percentage — a plain row like the two above.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Indikasi Pemutih",
                style = MaterialTheme.typography.bodyMedium,
                color = RiceColors.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = bleachLabel(scan.bleachLevel),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                color = if (scan.bleachLevel == "TINGGI") RiceColors.Danger else RiceColors.Text
            )
        }
    }
}

@Composable
private fun MetricBar(
    label: String,
    valueText: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = RiceColors.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                color = RiceColors.Text
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(PillShape),
            color = color,
            trackColor = RiceColors.SurfaceGreen
        )
    }
}

@Composable
private fun ResultStatusScreen(
    message: String,
    loading: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RiceColors.Background)
            .padding(horizontal = ScreenPadding)
    ) {
        ResultTopBar(onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(color = RiceColors.Primary)
            } else {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = RiceColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StubNotice() {
    Text(
        text = "Model belum tersedia. Angka kualitas di atas berasal dari data contoh (stub), bukan analisis foto.",
        style = MaterialTheme.typography.bodySmall,
        color = RiceColors.TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardLargeShape)
            .background(RiceColors.Cream)
            .padding(Spacing.md)
    )
}

// PREVIEW ONLY — the three Result states, rendered from PreviewFixtures.

@Preview(showBackground = true, name = "Hasil — ASLI", heightDp = 950, widthDp = 360)
@Composable
private fun ResultAsliPreview() {
    PreviewSurface {
        ResultContent(
            scan = PreviewFixtures.asli,
            isStub = false,
            onBack = {},
            onScanAgain = {},
            onHistory = {}
        )
    }
}

@Preview(showBackground = true, name = "Hasil — Berpotensi Oplosan", heightDp = 950, widthDp = 360)
@Composable
private fun ResultOplosanPreview() {
    PreviewSurface {
        ResultContent(
            scan = PreviewFixtures.berpotensiOplosan,
            isStub = true,
            onBack = {},
            onScanAgain = {},
            onHistory = {}
        )
    }
}

@Preview(showBackground = true, name = "Hasil — Laporan Kualitas", heightDp = 950, widthDp = 360)
@Composable
private fun ResultLaporanPreview() {
    PreviewSurface {
        ResultContent(
            scan = PreviewFixtures.laporanKualitas,
            isStub = false,
            onBack = {},
            onScanAgain = {},
            onHistory = {}
        )
    }
}

@Preview(showBackground = true, name = "Hasil — Laporan tanpa harga", heightDp = 950, widthDp = 360)
@Composable
private fun ResultLaporanTanpaHargaPreview() {
    PreviewSurface {
        ResultContent(
            scan = PreviewFixtures.laporanTanpaHarga,
            isStub = false,
            onBack = {},
            onScanAgain = {},
            onHistory = {}
        )
    }
}
