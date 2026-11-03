package com.yayadile.ricecheck.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.R
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.RiceColors
import com.yayadile.ricecheck.ui.theme.Spacing

@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Kembali",
                tint = RiceColors.Text
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = RiceColors.Text,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

// PREVIEW ONLY — static title, no navigation.
@Preview(showBackground = true, name = "BackTopBar")
@Composable
private fun BackTopBarPreview() {
    PreviewSurface {
        BackTopBar(title = "Hasil Analisis", onBack = {})
    }
}
