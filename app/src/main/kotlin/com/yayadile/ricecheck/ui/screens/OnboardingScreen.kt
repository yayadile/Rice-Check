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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yayadile.ricecheck.R
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.ui.components.RicePrimaryButton
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.ScreenPadding
import com.yayadile.ricecheck.ui.theme.Spacing
import com.yayadile.ricecheck.viewmodel.OnboardingViewModel
import com.yayadile.ricecheck.viewmodel.onboardingViewModelFactory
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val body: String,
    val iconRes: Int
)

private val pages = listOf(
    OnboardingPage(
        title = "Foto + harga",
        body = "Ambil foto beras, lalu isi harga per kg. Harga boleh dikosongkan.",
        iconRes = R.drawable.ic_photo_camera
    ),
    OnboardingPage(
        title = "Analisis on-device",
        body = "Kualitas dinilai langsung di ponsel: skor 0–100, butir utuh, menir/patah, dan indikasi pemutih.",
        iconRes = R.drawable.ic_image
    ),
    OnboardingPage(
        title = "Hasil dari harga vs kualitas",
        body = "Harga premium dengan kualitas rendah ditandai berpotensi oplosan. Hasil estimasi, bukan uji laboratorium.",
        iconRes = R.drawable.ic_history
    )
)

/**
 * Route: onboarding — 3 swipeable cards, skippable (docs/IA.md §3.1).
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = LocalAppContainer.current
    val viewModel: OnboardingViewModel = viewModel(
        factory = remember { onboardingViewModelFactory(container.onboardingPrefs) }
    )

    OnboardingContent(
        onFinish = {
            viewModel.markCompleted()
            onFinish()
        },
        modifier = modifier
    )
}

@Composable
fun OnboardingContent(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RiceColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(ScreenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinish) {
                Text(text = "Lewati", style = MaterialTheme.typography.labelLarge)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = Spacing.lg),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(RiceColors.SurfaceGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(page.iconRes),
                        contentDescription = null,
                        tint = RiceColors.Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = RiceColors.Text,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = page.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = RiceColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = Spacing.xs)
                        .size(width = if (active) 20.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (active) RiceColors.Primary else RiceColors.Outline)
                )
            }
        }

        RicePrimaryButton(
            text = if (pagerState.currentPage == pages.lastIndex) "Mulai" else "Lanjut",
            onClick = {
                if (pagerState.currentPage == pages.lastIndex) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.padding(bottom = Spacing.md)
        )
    }
}

// PREVIEW ONLY — first onboarding card; onFinish just does nothing.
@Preview(showBackground = true, name = "Onboarding — kartu 1", heightDp = 800)
@Composable
private fun OnboardingPreview() {
    PreviewSurface {
        OnboardingContent(onFinish = {})
    }
}
