package com.yayadile.ricecheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.RiceColors

/**
 * Circular progress ring for the quality score — the hero value of the Result screen,
 * in the spirit of a "portfolio value" ring. Real data only: the score is 0–100.
 */
@Composable
fun ScoreRing(
    score: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    ringSize: Dp = 150.dp,
    strokeWidth: Dp = 12.dp
) {
    val safeScore = score.coerceIn(0, 100)
    val sweep = 360f * safeScore / 100f

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            val arcStyle = Stroke(width = stroke, cap = StrokeCap.Round)

            // Track — a soft tint of the accent so it reads on the tinted hero card.
            drawArc(
                color = accent.copy(alpha = 0.16f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = arcStyle
            )
            // Progress — the score itself.
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = arcStyle
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = safeScore.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = accent
            )
            Text(
                text = "dari 100",
                style = MaterialTheme.typography.labelMedium,
                color = RiceColors.TextSecondary
            )
        }
    }
}

// PREVIEW ONLY — fixed scores, no scan behind them.
@Preview(showBackground = true, name = "ScoreRing")
@Composable
private fun ScoreRingPreview() {
    PreviewSurface {
        Box(contentAlignment = Alignment.Center) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ScoreRing(score = 82, accent = RiceColors.Primary)
                ScoreRing(score = 45, accent = RiceColors.Danger)
            }
        }
    }
}
