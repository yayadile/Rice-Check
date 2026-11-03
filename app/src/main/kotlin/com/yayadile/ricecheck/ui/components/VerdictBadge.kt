package com.yayadile.ricecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.domain.VerdictCalculator
import com.yayadile.ricecheck.ui.format.resultLabel
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.PillShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.Spacing

/**
 * ASLI = green, BERPOTENSI OPLOSAN = red, LAPORAN KUALITAS = neutral chip
 * (a quality report is not a verdict, so it gets no coloured badge).
 */
@Composable
fun VerdictBadge(
    result: String,
    modifier: Modifier = Modifier
) {
    val background = when (result) {
        VerdictCalculator.ASLI -> RiceColors.Primary
        VerdictCalculator.BERPOTENSI_OPLOSAN -> RiceColors.Danger
        else -> RiceColors.SurfaceGreen
    }
    val foreground = when (result) {
        VerdictCalculator.ASLI, VerdictCalculator.BERPOTENSI_OPLOSAN -> RiceColors.OnPrimary
        else -> RiceColors.TextSecondary
    }

    Text(
        text = resultLabel(result),
        style = MaterialTheme.typography.labelSmall,
        color = foreground,
        modifier = modifier
            .background(background, PillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

// PREVIEW ONLY — all three stored verdict strings, no database involved.
@Preview(showBackground = true, name = "VerdictBadge — semua status")
@Composable
private fun VerdictBadgePreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            VerdictBadge(result = VerdictCalculator.ASLI)
            VerdictBadge(result = VerdictCalculator.BERPOTENSI_OPLOSAN)
            VerdictBadge(result = VerdictCalculator.LAPORAN_KUALITAS)
        }
    }
}
