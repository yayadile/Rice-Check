package com.yayadile.ricecheck.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yayadile.ricecheck.R
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.ui.components.RiceBottomBar
import com.yayadile.ricecheck.ui.components.RicePrimaryButton
import com.yayadile.ricecheck.ui.components.ScanRow
import com.yayadile.ricecheck.ui.navigation.Routes
import com.yayadile.ricecheck.ui.preview.PreviewFixtures
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.PillShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.ScreenPadding
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.viewmodel.HistoryFilter
import com.yayadile.ricecheck.viewmodel.HistoryViewModel
import com.yayadile.ricecheck.viewmodel.historyViewModelFactory

/**
 * Route: history — filters Semua / Asli / Berpotensi Oplosan, rows, empty state.
 */
@Composable
fun HistoryScreen(
    onScan: () -> Unit,
    onHome: () -> Unit,
    onOpenScan: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val container = LocalAppContainer.current
    val viewModel: HistoryViewModel = viewModel(
        factory = remember { historyViewModelFactory(container.scanRepository) }
    )
    val scans by viewModel.scans.collectAsState()
    val selectedFilter by viewModel.filter.collectAsState()

    HistoryContent(
        scans = scans,
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::setFilter,
        onScan = onScan,
        onHome = onHome,
        onOpenScan = onOpenScan,
        modifier = modifier
    )
}

@Composable
fun HistoryContent(
    scans: List<ScanResultEntity>,
    selectedFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit,
    onScan: () -> Unit,
    onHome: () -> Unit,
    onOpenScan: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = RiceColors.Background,
        bottomBar = {
            RiceBottomBar(
                currentRoute = Routes.HISTORY,
                onHome = onHome,
                onScan = onScan,
                onHistory = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ScreenPadding)
        ) {
            Text(
                text = "Riwayat Analisis",
                style = MaterialTheme.typography.titleLarge,
                color = RiceColors.Text,
                modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.md)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                HistoryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = filter == selectedFilter,
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = PillShape,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = RiceColors.Surface,
                            labelColor = RiceColors.TextSecondary,
                            selectedContainerColor = RiceColors.Primary,
                            selectedLabelColor = RiceColors.OnPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filter == selectedFilter,
                            borderColor = RiceColors.Outline,
                            selectedBorderColor = RiceColors.Primary
                        )
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            if (scans.isEmpty()) {
                EmptyHistory(
                    onScan = onScan,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    contentPadding = PaddingValues(bottom = Spacing.lg)
                ) {
                    items(scans, key = { it.id }) { scan ->
                        ScanRow(scan = scan, onClick = { onOpenScan(scan.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory(onScan: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(RiceColors.SurfaceGreen, PillShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_history),
                    contentDescription = null,
                    tint = RiceColors.Primary
                )
            }
            Text(
                text = "Belum ada pemindaian",
                style = MaterialTheme.typography.titleMedium,
                color = RiceColors.Text,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Hasil scan akan muncul di sini.",
                style = MaterialTheme.typography.bodyMedium,
                color = RiceColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            RicePrimaryButton(
                text = "Scan sekarang",
                onClick = onScan,
                modifier = Modifier.padding(horizontal = Spacing.xl)
            )
        }
    }
}

// PREVIEW ONLY — rows come from PreviewFixtures, never from Room.
@Preview(showBackground = true, name = "Riwayat — ada data", heightDp = 800)
@Composable
private fun HistoryContentPreview() {
    PreviewSurface {
        HistoryContent(
            scans = PreviewFixtures.recentScans,
            selectedFilter = HistoryFilter.ALL,
            onFilterSelected = {},
            onScan = {},
            onHome = {},
            onOpenScan = { _ -> }
        )
    }
}

// PREVIEW ONLY — empty state (docs/IA.md §5).
@Preview(showBackground = true, name = "Riwayat — kosong", heightDp = 800)
@Composable
private fun HistoryContentEmptyPreview() {
    PreviewSurface {
        HistoryContent(
            scans = emptyList(),
            selectedFilter = HistoryFilter.ALL,
            onFilterSelected = {},
            onScan = {},
            onHome = {},
            onOpenScan = { _ -> }
        )
    }
}
