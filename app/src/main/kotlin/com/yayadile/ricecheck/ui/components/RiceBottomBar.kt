package com.yayadile.ricecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yayadile.ricecheck.R
import com.yayadile.ricecheck.ui.navigation.Routes
import com.yayadile.ricecheck.ui.preview.PreviewSurface
import com.yayadile.ricecheck.ui.theme.RiceColors

/**
 * Bottom nav with 3 points (docs/IA.md §1): Beranda | Scan (prominent) | Riwayat.
 */
@Composable
fun RiceBottomBar(
    currentRoute: String,
    onHome: () -> Unit,
    onScan: () -> Unit,
    onHistory: () -> Unit
) {
    NavigationBar(
        containerColor = RiceColors.Surface,
        tonalElevation = 0.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = RiceColors.Primary,
            selectedTextColor = RiceColors.Primary,
            indicatorColor = RiceColors.SurfaceGreen,
            unselectedIconColor = RiceColors.TextSecondary,
            unselectedTextColor = RiceColors.TextSecondary
        )

        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = onHome,
            icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = null) },
            label = { Text("Beranda") },
            colors = itemColors
        )

        NavigationBarItem(
            selected = false,
            onClick = onScan,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RiceColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_photo_camera),
                        contentDescription = null,
                        tint = RiceColors.OnPrimary
                    )
                }
            },
            label = { Text("Scan") },
            colors = itemColors
        )

        NavigationBarItem(
            selected = currentRoute == Routes.HISTORY,
            onClick = onHistory,
            icon = { Icon(painterResource(R.drawable.ic_history), contentDescription = null) },
            label = { Text("Riwayat") },
            colors = itemColors
        )
    }
}

// PREVIEW ONLY — Beranda tab selected.
@Preview(showBackground = true, name = "RiceBottomBar")
@Composable
private fun RiceBottomBarPreview() {
    PreviewSurface {
        Box(contentAlignment = Alignment.BottomCenter) {
            RiceBottomBar(
                currentRoute = Routes.HOME,
                onHome = {},
                onScan = {},
                onHistory = {}
            )
        }
    }
}
