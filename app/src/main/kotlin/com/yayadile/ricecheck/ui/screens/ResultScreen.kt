package com.yayadile.ricecheck.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.domain.VerdictCalculator
import com.yayadile.ricecheck.ui.components.BackTopBar
import com.yayadile.ricecheck.ui.components.QualityBreakdown
import com.yayadile.ricecheck.ui.components.RicePrimaryButton
import com.yayadile.ricecheck.ui.components.RiceSecondaryButton
import com.yayadile.ricecheck.ui.components.VerdictBadge
import com.yayadile.ricecheck.ui.format.formatPricePerKg
import com.yayadile.ricecheck.ui.format.formatScanDate
import com.yayadile.ricecheck.ui.preview.PreviewFixtures
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.CardLargeShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.ScreenPadding
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.viewmodel.ResultViewModel
import com.yayadile.ricecheck.viewmodel.ScanDetailState
import com.yayadile.ricecheck.viewmodel.resultViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Route: result/{scanId} — 3 states derived from the stored verdict.
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
            .padding(horizontal = ScreenPadding)
    ) {
        BackTopBar(title = "Hasil Analisis", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.xs))
            VerdictBadge(result = scan.result)
            Text(
                text = verdictHeadline(scan.result),
                style = MaterialTheme.typography.titleLarge,
                color = RiceColors.Text
            )
            Text(
                text = verdictExplanation(scan),
                style = MaterialTheme.typography.bodyMedium,
                color = RiceColors.TextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardLargeShape)
                    .background(RiceColors.Surface)
                    .padding(Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Harga per kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RiceColors.TextSecondary
                )
                Text(
                    text = formatPricePerKg(scan.price),
                    style = MaterialTheme.typography.labelLarge,
                    color = RiceColors.Text
                )
            }

            QualityBreakdown(
                qualityScore = scan.qualityScore,
                wholeGrainPct = scan.wholeGrainPct,
                brokenPct = scan.brokenPct,
                bleachLevel = scan.bleachLevel,
                bleachPct = scan.bleachPct
            )

            Text(
                text = formatScanDate(scan.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = RiceColors.TextSecondary
            )

            if (isStub) {
                StubNotice()
            }

            Text(
                text = "Hasil estimasi, bukan uji laboratorium.",
                style = MaterialTheme.typography.bodySmall,
                color = RiceColors.TextSecondary
            )

            Spacer(Modifier.height(Spacing.xs))
            RicePrimaryButton(text = "Scan lagi", onClick = onScanAgain)
            RiceSecondaryButton(text = "Lihat riwayat", onClick = onHistory)
            Spacer(Modifier.height(Spacing.lg))
        }
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
        BackTopBar(title = "Hasil Analisis", onBack = onBack)
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

private fun verdictHeadline(result: String): String = when (result) {
    VerdictCalculator.ASLI -> "ASLI, kualitas baik"
    VerdictCalculator.BERPOTENSI_OPLOSAN -> "Berpotensi oplosan"
    else -> "Laporan kualitas"
}

private fun verdictExplanation(scan: ScanResultEntity): String = when (scan.result) {
    VerdictCalculator.BERPOTENSI_OPLOSAN ->
        "Harga ${formatPricePerKg(scan.price)} tergolong premium, tapi kualitas terdeteksi rendah (${scan.qualityScore}%)."
    VerdictCalculator.ASLI ->
        "Harga ${formatPricePerKg(scan.price)} tergolong premium dan kualitas terdeteksi tinggi (${scan.qualityScore}%)."
    else ->
        if (scan.price == null) {
            "Harga tidak diisi — tanpa vonis, hanya laporan kualitas."
        } else {
            "Harga ${formatPricePerKg(scan.price)} tergolong murah — tanpa vonis, hanya laporan kualitas."
        }
}

// PREVIEW ONLY — the three Result states, rendered from PreviewFixtures.

@Preview(showBackground = true, name = "Hasil — ASLI", heightDp = 950)
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

@Preview(showBackground = true, name = "Hasil — Berpotensi Oplosan", heightDp = 950)
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

@Preview(showBackground = true, name = "Hasil — Laporan Kualitas", heightDp = 950)
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
