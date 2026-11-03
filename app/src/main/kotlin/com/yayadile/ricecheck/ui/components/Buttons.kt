package com.yayadile.ricecheck.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.PillShape
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.Spacing

@Composable
fun RicePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = RiceColors.Primary,
            contentColor = RiceColors.OnPrimary,
            disabledContainerColor = RiceColors.Outline,
            disabledContentColor = RiceColors.OnPrimary
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun RiceSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = PillShape,
        border = BorderStroke(1.dp, RiceColors.Outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = RiceColors.Text)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// PREVIEW ONLY — both button variants, enabled and disabled.
@Preview(showBackground = true, name = "Buttons")
@Composable
private fun ButtonsPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            RicePrimaryButton(text = "Scan Beras", onClick = {})
            RiceSecondaryButton(text = "Lihat riwayat", onClick = {})
            RicePrimaryButton(text = "Nonaktif", onClick = {}, enabled = false)
        }
    }
}
