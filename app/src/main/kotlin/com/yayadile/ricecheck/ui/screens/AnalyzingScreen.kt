package com.yayadile.ricecheck.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.ui.components.RicePrimaryButton
import com.yayadile.ricecheck.ui.components.RiceSecondaryButton
import com.yayadile.ricecheck.ui.components.rememberScanPhoto
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.CardLargeShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.ScreenPadding
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.viewmodel.AnalyzingState
import com.yayadile.ricecheck.viewmodel.AnalyzingViewModel
import com.yayadile.ricecheck.viewmodel.analyzingViewModelFactory

/**
 * Route: analyzing?photoUri={photoUri}&price={price}
 * Runs classify -> verdict -> insert, then hands the new scan id to the nav graph.
 */
@Composable
fun AnalyzingScreen(
    photoUri: String,
    price: Int?,
    onCompleted: (Long) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = LocalAppContainer.current
    val viewModel: AnalyzingViewModel = viewModel(
        key = "analyzing-$photoUri",
        factory = remember(photoUri, price) {
            analyzingViewModelFactory(container.scanRepository, photoUri, price)
        }
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val current = state
        if (current is AnalyzingState.Completed) onCompleted(current.scanId)
    }

    AnalyzingContent(
        state = state,
        photoPath = photoUri,
        onRetry = { viewModel.start() },
        onCancel = {
            viewModel.cancel()
            onCancel()
        },
        modifier = modifier
    )
}

@Composable
fun AnalyzingContent(
    state: AnalyzingState,
    photoPath: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RiceColors.Background)
            .padding(ScreenPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            when (state) {
                is AnalyzingState.Failed -> {
                    Text(
                        text = "Analisis gagal",
                        style = MaterialTheme.typography.titleLarge,
                        color = RiceColors.Text
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = RiceColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    RicePrimaryButton(
                        text = "Coba lagi",
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                    RiceSecondaryButton(
                        text = "Kembali",
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> {
                    ScanningPreview(photoPath = photoPath)
                    CircularProgressIndicator(color = RiceColors.Primary)
                    Text(
                        text = "Menganalisis foto…",
                        style = MaterialTheme.typography.titleMedium,
                        color = RiceColors.Text
                    )
                    Text(
                        text = "Menilai kualitas beras di perangkat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RiceColors.TextSecondary
                    )
                    RiceSecondaryButton(
                        text = "Batal",
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.md)
                    )
                }
            }
        }
    }
}

/** Photo with a sweeping scan line — the "animasi scan" from docs/IA.md §4. */
@Composable
private fun ScanningPreview(photoPath: String) {
    val photo = rememberScanPhoto(photoPath, targetSizePx = 512)
    val transition = rememberInfiniteTransition(label = "scan")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    val boxSize = 220.dp
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(CardLargeShape)
            .background(RiceColors.SurfaceGreen)
    ) {
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = "Foto yang dianalisis",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .offset(y = (boxSize - 3.dp) * progress)
                .background(RiceColors.Primary.copy(alpha = 0.85f))
        )
    }
}

// PREVIEW ONLY — photoPath is empty, so the photo area shows the neutral placeholder.
@Preview(showBackground = true, name = "Analisis — berjalan", heightDp = 800)
@Composable
private fun AnalyzingWorkingPreview() {
    PreviewSurface {
        AnalyzingContent(
            state = AnalyzingState.Working,
            photoPath = "",
            onRetry = {},
            onCancel = {}
        )
    }
}

// PREVIEW ONLY — failure branch with realistic error copy.
@Preview(showBackground = true, name = "Analisis — gagal", heightDp = 800)
@Composable
private fun AnalyzingFailedPreview() {
    PreviewSurface {
        AnalyzingContent(
            state = AnalyzingState.Failed("Foto tidak bisa dibaca. Coba ambil ulang."),
            photoPath = "",
            onRetry = {},
            onCancel = {}
        )
    }
}
