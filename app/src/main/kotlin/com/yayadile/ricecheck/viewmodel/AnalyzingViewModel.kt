package com.yayadile.ricecheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayadile.ricecheck.data.repo.ScanRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AnalyzingState {
    data object Working : AnalyzingState
    data class Completed(val scanId: Long) : AnalyzingState
    data class Failed(val message: String) : AnalyzingState
}

class AnalyzingViewModel(
    private val repository: ScanRepository,
    private val photoPath: String,
    private val price: Int?
) : ViewModel() {

    private val _state = MutableStateFlow<AnalyzingState>(AnalyzingState.Working)
    val state: StateFlow<AnalyzingState> = _state.asStateFlow()

    private var job: Job? = null

    init {
        start()
    }

    fun start() {
        if (job?.isActive == true) return
        _state.value = AnalyzingState.Working
        job = viewModelScope.launch {
            val result = runCatching { repository.save(photoPath, price) }
            _state.value = result.fold(
                onSuccess = { AnalyzingState.Completed(it) },
                onFailure = {
                    AnalyzingState.Failed(it.message ?: "Analisis gagal. Coba lagi.")
                }
            )
        }
    }

    /**
     * Cancel = kembali ke Camera tanpa menyimpan apa pun (docs/IA.md §5).
     * Nothing is written to the DB before save() completes, so only the temp photo is removed.
     */
    fun cancel() {
        if (_state.value is AnalyzingState.Completed) return
        job?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { File(photoPath).delete() }
        }
    }
}
