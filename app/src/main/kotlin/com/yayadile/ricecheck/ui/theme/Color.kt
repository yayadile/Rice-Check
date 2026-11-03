package com.yayadile.ricecheck.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Canonical palette from docs/design-tokens.md.
 * These hex values are the source of truth — do not add ad-hoc colours to screens.
 */
object RiceColors {
    /** CTA utama, badge ASLI, tab filter aktif. */
    val Primary = Color(0xFF388E33)

    /** Hero banner Beranda, kartu Tips. */
    val Cream = Color(0xFFFFE3A7)

    /** Background semua screen. */
    val Background = Color(0xFFFBFBF9)

    /** Kartu. */
    val Surface = Color(0xFFFFFFFF)

    /** Chip, background ikon, banner info. */
    val SurfaceGreen = Color(0xFFEBFFE6)

    /** Teks utama. */
    val Text = Color(0xFF0D200E)

    /** Teks sekunder, tanggal. */
    val TextSecondary = Color(0xFF40493D)

    /** Border kartu. */
    val Outline = Color(0xFFBFCABA)

    /** Badge OPLOSAN, tombol hapus. */
    val Danger = Color(0xFFD32F2F)

    /** Teks di atas hijau/merah. */
    val OnPrimary = Color(0xFFFFFFFF)
}
