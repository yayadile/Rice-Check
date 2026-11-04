package com.yayadile.ricecheck.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.ui.components.BackTopBar
import com.yayadile.ricecheck.ui.components.QualityBreakdown
import com.yayadile.ricecheck.ui.components.VerdictBadge
import com.yayadile.ricecheck.ui.components.rememberScanPhoto
import com.yayadile.ricecheck.ui.format.formatPricePerKg
import com.yayadile.ricecheck.ui.format.formatScanDate
import com.yayadile.ricecheck.ui.preview.PreviewFixtures
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.CardLargeShape
import com.yayadile.ricecheck.ui.theme.PillShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.ScreenPadding
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.viewmodel.HistoryDetailViewModel
import com.yayadile.ricecheck.viewmodel.ScanDetailState
import com.yayadile.ricecheck.viewmodel.historyDetailViewModelFactory

/**
 * Route: historyDetail/{scanId} — photo, price, verdict, metrics, date, delete with confirmation.
 */
@Composable
fun HistoryDetailScreen(
    scanId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = LocalAppContainer.current
    val viewModel: HistoryDetailViewModel = viewModel(
        key = "historyDetail-$scanId",
        factory = remember(scanId) {
            historyDetailViewModelFactory(container.scanRepository, scanId)
        }
    )
    val state by viewModel.state.collectAsState()
    val deleted by viewModel.deleted.collectAsState()
    val deleteError by viewModel.deleteError.collectAsState()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(deleted) {
        if (deleted) onDeleted()
    }

    val scan = (state as? ScanDetailState.Loaded)?.scan
    when {
        state is ScanDetailState.Loading -> DetailStatusScreen(
            message = "Memuat riwayat…",
            loading = true,
            onBack = onBack,
            modifier = modifier
        )

        scan == null -> DetailStatusScreen(
            message = "Data scan tidak ditemukan.",
            loading = false,
            onBack = onBack,
            modifier = modifier
        )

        else -> HistoryDetailContent(
            scan = scan,
            deleteError = deleteError,
            onBack = onBack,
            onDelete = { showDeleteDialog = true },
            modifier = modifier
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Hapus dari riwayat?") },
            text = { Text(text = "Data scan dan fotonya akan dihapus permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    }
                ) {
                    Text(text = "Hapus", color = RiceColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Batal")
                }
            }
        )
    }
}

@Composable
fun HistoryDetailContent(
    scan: ScanResultEntity,
    deleteError: String?,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photo = rememberScanPhoto(scan.photoPath, targetSizePx = 1024)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RiceColors.Background)
            .padding(horizontal = ScreenPadding)
    ) {
        BackTopBar(title = "Detail Riwayat", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (photo != null) {
                Image(
                    bitmap = photo,
                    contentDescription = "Foto beras",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(CardLargeShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(CardLargeShape)
                        .background(RiceColors.SurfaceGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Foto tidak tersedia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RiceColors.TextSecondary
                    )
                }
            }

            VerdictBadge(result = scan.result)

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

            if (deleteError != null) {
                Text(
                    text = deleteError,
                    style = MaterialTheme.typography.bodySmall,
                    color = RiceColors.Danger
                )
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = PillShape,
                border = BorderStroke(1.dp, RiceColors.Danger)
            ) {
                Text(
                    text = "Hapus dari riwayat",
                    style = MaterialTheme.typography.labelLarge,
                    color = RiceColors.Danger
                )
            }

            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun DetailStatusScreen(
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
        BackTopBar(title = "Detail Riwayat", onBack = onBack)
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

// PREVIEW ONLY — fixture row; the photo placeholder renders because photoPath is empty.
@Preview(showBackground = true, name = "Detail Riwayat", heightDp = 950)
@Composable
private fun HistoryDetailPreview() {
    PreviewSurface {
        HistoryDetailContent(
            scan = PreviewFixtures.berpotensiOplosan,
            deleteError = null,
            onBack = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true, name = "Detail Riwayat — ASLI", heightDp = 950)
@Composable
private fun HistoryDetailAsliPreview() {
    PreviewSurface {
        HistoryDetailContent(
            scan = PreviewFixtures.asli,
            deleteError = null,
            onBack = {},
            onDelete = {}
        )
    }
}
