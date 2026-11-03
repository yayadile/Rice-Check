package com.yayadile.ricecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.ui.format.formatScanDate
import com.yayadile.ricecheck.ui.preview.PreviewFixtures
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.CardShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.Spacing

/** History row: thumbnail, result, date (docs/IA.md §4). */
@Composable
fun ScanRow(
    scan: ScanResultEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(RiceColors.Surface)
            .border(1.dp, RiceColors.Outline, CardShape)
            .clickable(onClick = onClick)
            .padding(Spacing.sm + 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScanThumbnail(path = scan.photoPath, size = 56.dp)
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            VerdictBadge(result = scan.result)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = formatScanDate(scan.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = RiceColors.TextSecondary
            )
        }
    }
}

// PREVIEW ONLY — rows come from PreviewFixtures, never from Room.
@Preview(showBackground = true, name = "ScanRow")
@Composable
private fun ScanRowPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            ScanRow(scan = PreviewFixtures.asli, onClick = {})
            ScanRow(scan = PreviewFixtures.berpotensiOplosan, onClick = {})
            ScanRow(scan = PreviewFixtures.laporanKualitas, onClick = {})
        }
    }
}
