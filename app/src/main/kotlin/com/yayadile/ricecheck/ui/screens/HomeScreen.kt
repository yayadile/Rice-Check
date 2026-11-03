package com.yayadile.ricecheck.ui.screens

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
 * Route: home — hero, CTA, real weekly count, tips, 3 most recent scans.
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
            item {
                Column {
                    Text(
                        text = "Halo!",
                        style = MaterialTheme.typography.displaySmall,
                        color = RiceColors.Text
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Cek kualitas beras sebelum membeli.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RiceColors.TextSecondary
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardLargeShape)
                        .background(RiceColors.Cream)
                        .padding(Spacing.lg)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RiceColors.Surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_photo_camera),
                            contentDescription = null,
                            tint = RiceColors.Primary
                        )
                    }
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        text = "Foto beras, isi harga, lihat hasilnya.",
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
                    RicePrimaryButton(text = "Scan Beras", onClick = onScan)
                }
            }

            item {
                Text(
                    text = if (state.weeklyScanCount == 0) {
                        "Belum ada pemindaian minggu ini"
                    } else {
                        "${state.weeklyScanCount} pemindaian minggu ini"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = RiceColors.TextSecondary,
                    modifier = Modifier
                        .clip(PillShape)
                        .background(RiceColors.SurfaceGreen)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardLargeShape)
                        .background(RiceColors.Cream)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tipsExpanded = !tipsExpanded }
                            .padding(horizontal = Spacing.md, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tips Foto Beras",
                            style = MaterialTheme.typography.titleMedium,
                            color = RiceColors.Text
                        )
                        Text(
                            text = if (tipsExpanded) "Tutup" else "Lihat",
                            style = MaterialTheme.typography.labelMedium,
                            color = RiceColors.Primary
                        )
                    }
                    if (tipsExpanded) {
                        Column(
                            modifier = Modifier.padding(
                                start = Spacing.md,
                                end = Spacing.md,
                                bottom = Spacing.md
                            ),
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

            item {
                Text(
                    text = "Pemindaian terakhir",
                    style = MaterialTheme.typography.headlineSmall,
                    color = RiceColors.Text,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
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
                            Text(text = "Scan sekarang", style = MaterialTheme.typography.labelMedium)
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
