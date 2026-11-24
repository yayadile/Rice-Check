package com.yayadile.ricecheck.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yayadile.ricecheck.R
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.ui.components.RiceBottomBar
import com.yayadile.ricecheck.ui.components.RicePrimaryButton
import com.yayadile.ricecheck.ui.components.ScanRow
import com.yayadile.ricecheck.ui.navigation.Routes
import com.yayadile.ricecheck.ui.preview.PreviewFixtures
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.CardLargeShape
import com.yayadile.ricecheck.ui.theme.PillShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.RiceGradients
import com.yayadile.ricecheck.ui.theme.ScreenPadding
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.viewmodel.HomeUiState
import com.yayadile.ricecheck.viewmodel.HomeViewModel
import com.yayadile.ricecheck.viewmodel.homeViewModelFactory

private val tips = listOf(
    "Latar kontras dengan warna beras",
    "Pencahayaan rata, hindari bayangan",
    "Jarak 10–15 cm, tegak lurus"
)

/**
 * Route: home — hero with the real 3-step flow, real weekly count, tips, 3 most recent scans.
 */
@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onHistory: () -> Unit,
    onOpenScan: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(
        factory = remember { homeViewModelFactory(container.scanRepository) }
    )
    val state by viewModel.uiState.collectAsState()

    HomeContent(
        state = state,
        onScan = onScan,
        onHistory = onHistory,
        onOpenScan = onOpenScan,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onScan: () -> Unit,
    onHistory: () -> Unit,
    onOpenScan: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var tipsExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = RiceColors.Background,
        bottomBar = {
            RiceBottomBar(
                currentRoute = Routes.HOME,
                onHome = {},
                onScan = onScan,
                onHistory = onHistory
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { HomeHero(onScan = onScan) }

            item {
                TipsRow(
                    expanded = tipsExpanded,
                    onToggle = { tipsExpanded = !tipsExpanded }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pemindaian terakhir",
                        style = MaterialTheme.typography.titleLarge,
                        color = RiceColors.Text
                    )
                    Spacer(Modifier.weight(1f))
                    WeeklyCountPill(count = state.weeklyScanCount)
                }
            }

            if (state.recentScans.isEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Belum ada pemindaian.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RiceColors.TextSecondary
                        )
                        TextButton(onClick = onScan) {
                            Text(
                                text = "Scan sekarang",
                                style = MaterialTheme.typography.labelLarge,
                                color = RiceColors.Primary
                            )
                        }
                    }
                }
            } else {
                items(state.recentScans, key = { it.id }) { scan ->
                    ScanRow(scan = scan, onClick = { onOpenScan(scan.id) })
                }
            }

            item { Spacer(Modifier.height(Spacing.sm)) }
        }
    }
}

/** Hero: logo, greeting, the real 1→2→3 flow, then the CTA. */
@Composable
private fun HomeHero(onScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardLargeShape)
            .background(RiceGradients.HeroCream)
            .padding(horizontal = Spacing.md, vertical = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo_ricecheck),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "Cek kualitas beras sebelum membeli.",
            style = MaterialTheme.typography.headlineSmall,
            color = RiceColors.Text
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "Analisis berjalan di perangkat, tanpa koneksi internet.",
            style = MaterialTheme.typography.bodyMedium,
            color = RiceColors.TextSecondary
        )
        Spacer(Modifier.height(Spacing.md))
        FlowSteps()
        Spacer(Modifier.height(Spacing.md))
        RicePrimaryButton(text = "Scan Beras", onClick = onScan)
    }
}

/** The real product flow: 1 Foto → 2 Harga → 3 Hasil. */
@Composable
private fun FlowSteps() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepChip(number = "1", label = "Foto")
        StepArrow()
        StepChip(number = "2", label = "Harga")
        StepArrow()
        StepChip(number = "3", label = "Hasil")
    }
}

@Composable
private fun StepChip(number: String, label: String) {
    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(RiceColors.SurfaceGreen)
            .padding(start = Spacing.xs, end = Spacing.sm, top = Spacing.xs, bottom = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(RiceColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = RiceColors.OnPrimary
            )
        }
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = RiceColors.Text
        )
    }
}

@Composable
private fun StepArrow() {
    Icon(
        painter = painterResource(R.drawable.ic_arrow_forward),
        contentDescription = null,
        tint = RiceColors.Outline,
        modifier = Modifier
            .padding(horizontal = Spacing.xs)
            .size(16.dp)
    )
}

/** Weekly count lives in the section header, so it does not float on its own. */
@Composable
private fun WeeklyCountPill(count: Int) {
    Text(
        text = if (count == 0) {
            "Minggu ini: belum ada"
        } else {
            "Minggu ini: $count"
        },
        style = MaterialTheme.typography.labelMedium,
        color = RiceColors.TextSecondary,
        modifier = Modifier
            .clip(PillShape)
            .background(RiceColors.SurfaceGreen)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    )
}

/** Tips are a plain expandable row — icon, title, chevron — not a full card. */
@Composable
private fun TipsRow(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(RiceColors.SurfaceGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lightbulb),
                    contentDescription = null,
                    tint = RiceColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = "Tips Foto Beras",
                style = MaterialTheme.typography.bodyLarge,
                color = RiceColors.Text
            )
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(
                    if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                ),
                contentDescription = if (expanded) "Tutup tips" else "Lihat tips",
                tint = RiceColors.TextSecondary
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                tips.forEach { tip ->
                    Text(
                        text = "•  $tip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RiceColors.TextSecondary
                    )
                }
            }
        }
    }
}

// PREVIEW ONLY — data comes from PreviewFixtures, never from Room.
@Preview(showBackground = true, name = "Beranda", heightDp = 1000)
@Composable
private fun HomeContentPreview() {
    PreviewSurface {
        HomeContent(
            state = HomeUiState(
                weeklyScanCount = 7,
                recentScans = PreviewFixtures.recentScans
            ),
            onScan = {},
            onHistory = {},
            onOpenScan = { _ -> }
        )
    }
}

// PREVIEW ONLY — fresh install, no rows yet.
@Preview(showBackground = true, name = "Beranda — belum ada scan", heightDp = 1000)
@Composable
private fun HomeContentEmptyPreview() {
    PreviewSurface {
        HomeContent(
            state = HomeUiState(weeklyScanCount = 0, recentScans = emptyList()),
            onScan = {},
            onHistory = {},
            onOpenScan = { _ -> }
        )
    }
}
