package com.yayadile.ricecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.ui.format.bleachLabel
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.CardLargeShape
import com.yayadile.ricecheck.ui.theme.PillShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.Spacing

/**
 * Real scan metrics only: quality score, % whole grain, % broken, bleach indication.
 * Nothing here is derived or invented.
 */
@Composable
fun QualityBreakdown(
    qualityScore: Int,
    wholeGrainPct: Int,
    brokenPct: Int,
    bleachLevel: String,
    bleachPct: Int?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RiceColors.Surface, CardLargeShape)
            .border(1.dp, RiceColors.Outline, CardLargeShape)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = qualityScore.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = RiceColors.Primary
            )
            Text(
                text = "/100",
                style = MaterialTheme.typography.bodySmall,
                color = RiceColors.TextSecondary,
                modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.sm)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Skor kualitas",
                style = MaterialTheme.typography.labelMedium,
                color = RiceColors.TextSecondary
            )
        }

        MetricBar(
            label = "Butir utuh",
            valueText = "$wholeGrainPct%",
            progress = wholeGrainPct / 100f,
            color = RiceColors.Primary
        )
        MetricBar(
            label = "Menir / patah",
            valueText = "$brokenPct%",
            progress = brokenPct / 100f,
            color = RiceColors.Danger
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Indikasi pemutih",
                style = MaterialTheme.typography.bodyMedium,
                color = RiceColors.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = bleachLabel(bleachLevel) + (bleachPct?.let { " · $it%" } ?: ""),
                style = MaterialTheme.typography.labelLarge,
                color = if (bleachLevel == "TINGGI") RiceColors.Danger else RiceColors.Primary
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
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = RiceColors.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge,
                color = RiceColors.Text
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(PillShape),
            color = color,
            trackColor = RiceColors.SurfaceGreen
        )
    }
}

// PREVIEW ONLY — fixed metrics, no scan behind them.
@Preview(showBackground = true, name = "QualityBreakdown")
@Composable
private fun QualityBreakdownPreview() {
    PreviewSurface {
        Column(modifier = Modifier.padding(Spacing.md)) {
            QualityBreakdown(
                qualityScore = 82,
                wholeGrainPct = 91,
                brokenPct = 9,
                bleachLevel = "RENDAH",
                bleachPct = 12
            )
        }
    }
}
