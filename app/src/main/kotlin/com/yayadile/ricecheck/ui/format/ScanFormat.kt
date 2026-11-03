package com.yayadile.ricecheck.ui.format

import com.yayadile.ricecheck.domain.VerdictCalculator
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val idLocale = Locale("id", "ID")

private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH.mm", idLocale)

/** "Rp16.000/kg", or "Tidak diisi" when the user left the price blank. */
fun formatPricePerKg(price: Int?): String {
    if (price == null) return "Tidak diisi"
    return "Rp" + NumberFormat.getNumberInstance(idLocale).format(price) + "/kg"
}

fun formatScanDate(epochMillis: Long): String =
    dateFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

fun resultLabel(result: String): String = when (result) {
    VerdictCalculator.ASLI -> "ASLI"
    VerdictCalculator.BERPOTENSI_OPLOSAN -> "BERPOTENSI OPLOSAN"
    VerdictCalculator.LAPORAN_KUALITAS -> "LAPORAN KUALITAS"
    else -> result
}

/** Short form used in rows and history rows. */
fun resultShortLabel(result: String): String = when (result) {
    VerdictCalculator.ASLI -> "Asli"
    VerdictCalculator.BERPOTENSI_OPLOSAN -> "Berpotensi oplosan"
    VerdictCalculator.LAPORAN_KUALITAS -> "Laporan kualitas"
    else -> result
}

fun bleachLabel(level: String): String = when (level) {
    "TINGGI" -> "Tinggi"
    "RENDAH" -> "Rendah"
    else -> level
}
