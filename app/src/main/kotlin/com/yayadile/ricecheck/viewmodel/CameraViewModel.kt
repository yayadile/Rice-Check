package com.yayadile.ricecheck.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayadile.ricecheck.data.repo.ScanFileStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface GalleryImportState {
    data object Idle : GalleryImportState
    data object Working : GalleryImportState
    data class Imported(val photoPath: String) : GalleryImportState
    data class Failed(val message: String) : GalleryImportState
}

class CameraViewModel(private val fileStore: ScanFileStore) : ViewModel() {

    private val _priceInput = MutableStateFlow("")
    val priceInput: StateFlow<String> = _priceInput.asStateFlow()

    private val _galleryState = MutableStateFlow<GalleryImportState>(GalleryImportState.Idle)
    val galleryState: StateFlow<GalleryImportState> = _galleryState.asStateFlow()

    fun onPriceChange(value: String) {
        _priceInput.value = value.filter { it.isDigit() }.take(7)
    }

    fun priceOrNull(): Int? = _priceInput.value.toIntOrNull()

    /** Destination file for a CameraX capture (created by the screen, written by CameraX). */
    fun newCaptureFile(): File = fileStore.newScanFile()

    fun importFromGallery(uri: Uri) {
        if (_galleryState.value is GalleryImportState.Working) return
        _galleryState.value = GalleryImportState.Working
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { fileStore.importFromUri(uri) }
            }
            _galleryState.value = result.fold(
                onSuccess = { GalleryImportState.Imported(it) },
                onFailure = {
                    GalleryImportState.Failed(it.message ?: "Gagal memuat foto dari galeri")
                }
            )
        }
    }

    fun consumeGalleryState() {
        _galleryState.value = GalleryImportState.Idle
    }
}
