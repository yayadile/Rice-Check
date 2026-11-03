package com.yayadile.ricecheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * The design tokens define a single light palette (#FBFBF9 background, #388E33 primary),
 * so the app does not ship a dark variant or dynamic colour in v1.
 */
private val RiceCheckColorScheme = lightColorScheme(
    primary = RiceColors.Primary,
    onPrimary = RiceColors.OnPrimary,
    primaryContainer = RiceColors.SurfaceGreen,
    onPrimaryContainer = RiceColors.Text,
    secondary = RiceColors.Cream,
    onSecondary = RiceColors.Text,
    secondaryContainer = RiceColors.Cream,
    onSecondaryContainer = RiceColors.Text,
    background = RiceColors.Background,
    onBackground = RiceColors.Text,
    surface = RiceColors.Surface,
    onSurface = RiceColors.Text,
    surfaceVariant = RiceColors.SurfaceGreen,
    onSurfaceVariant = RiceColors.TextSecondary,
    outline = RiceColors.Outline,
    outlineVariant = RiceColors.Outline,
    error = RiceColors.Danger,
    onError = RiceColors.OnPrimary,
    errorContainer = RiceColors.Danger,
    onErrorContainer = RiceColors.OnPrimary
)

@Composable
fun RiceCheckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RiceCheckColorScheme,
        typography = RiceTypography,
        shapes = RiceShapes,
        content = content
    )
}
