package com.yayadile.ricecheck.data.repo

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

/**
 * Photos live in internal storage at filesDir/scans/<timestamp>.jpg — the DB only
 * stores the path (docs/architecture.md §3).
 */
class ScanFileStore(context: Context) {

    private val appContext = context.applicationContext

    private val scansDir: File
        get() = File(appContext.filesDir, "scans").apply { mkdirs() }

    /** Fresh destination for a CameraX capture. */
    fun newScanFile(): File = File(scansDir, "${System.currentTimeMillis()}.jpg")

    /** Copies a gallery pick into internal storage so the scan owns its photo. */
    fun importFromUri(uri: Uri): String {
        val target = newScanFile()
        val input = appContext.contentResolver.openInputStream(uri)
            ?: throw IOException("Tidak bisa membaca gambar yang dipilih")
        input.use { source ->
            target.outputStream().use { sink -> source.copyTo(sink) }
        }
        return target.absolutePath
    }
}
