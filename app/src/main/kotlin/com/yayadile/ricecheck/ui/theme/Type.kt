package com.yayadile.ricecheck.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yayadile.ricecheck.R

/**
 * Manrope (headlines) and Hanken Grotesk (body/label) are bundled in res/font as
 * variable fonts, so the app never needs the network to style text.
 */
val Manrope = FontFamily(
    Font(R.font.manrope, weight = FontWeight.Normal),
    Font(R.font.manrope, weight = FontWeight.Medium),
    Font(R.font.manrope, weight = FontWeight.SemiBold),
    Font(R.font.manrope, weight = FontWeight.Bold),
    Font(R.font.manrope, weight = FontWeight.ExtraBold)
)

val HankenGrotesk = FontFamily(
    Font(R.font.hanken_grotesk, weight = FontWeight.Normal),
    Font(R.font.hanken_grotesk, weight = FontWeight.Medium),
    Font(R.font.hanken_grotesk, weight = FontWeight.SemiBold),
    Font(R.font.hanken_grotesk, weight = FontWeight.Bold)
)

/** Type scale from docs/design-tokens.md. */
val RiceTypography = Typography(
    // Display — 28sp / 36sp / 800 — skor kualitas
    displaySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    // Title — 22sp / 30sp / 700 — screen titles
    titleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    // Heading — 20sp / 28sp / 700 — card titles
    headlineSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    // Heading small — 18sp / 24sp / 600 — app bar titles
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // Body large — 16sp / 24sp / 500
    bodyLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Body — 14sp / 20sp / 400
    bodyMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Caption — 12sp / 16sp / 400 — dates, hints
    bodySmall = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Label large — 14sp / 20sp / 700 — button labels
    labelLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Label — 12sp / 16sp / 600
    labelMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Label small — 11sp / 14sp / 700 — ASLI/OPLOSAN badge
    labelSmall = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
