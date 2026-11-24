package com.yayadile.ricecheck.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Gradien tipis yang diturunkan dari palet kanonik (docs/design-tokens.md).
 *
 * Tiap gradien hanya mencerahkan token dasarnya lewat [lighten], jadi tidak pernah
 * memunculkan hue baru — layar tetap memakai bahasa warna yang sama. Dipakai hero
 * Beranda dan seluruh tombol utama supaya terasa seragam.
 */
private fun lighten(color: Color, fraction: Float): Color =
    lerp(color, Color.White, fraction)

object RiceGradients {

    /** Hero Beranda: cream yang sedikit lebih terang ke arah kanan-bawah. */
    val HeroCream: Brush = Brush.linearGradient(
        colors = listOf(
            RiceColors.Cream,
            lighten(RiceColors.Cream, 0.30f)
        )
    )

    /**
     * Tombol utama: sheen vertikal tipis (atas sedikit lebih terang) — terasa lembut
     * di pill, bukan kilau menyamping yang berlebihan.
     */
    val PrimaryButton: Brush = Brush.verticalGradient(
        colors = listOf(
            lighten(RiceColors.Primary, 0.14f),
            RiceColors.Primary
        )
    )
}
