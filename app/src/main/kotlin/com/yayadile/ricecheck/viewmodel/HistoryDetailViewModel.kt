package com.yayadile.ricecheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayadile.ricecheck.data.repo.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryDetailViewModel(
    private val repository: ScanRepository,
    private val scanId: Long
) : ViewModel() {

    val state: StateFlow<ScanDetailState> = repository.observeById(scanId)
        .map { ScanDetailState.Loaded(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ScanDetailState.Loading
        )

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    /** Row + photo file are removed by the repository, never by the UI. */
    fun delete() {
        viewModelScope.launch {
            runCatching { repository.delete(scanId) }
                .onSuccess { _deleted.value = true }
                .onFailure { _deleteError.value = it.message ?: "Gagal menghapus riwayat" }
        }
    }

    fun consumeDeleteError() {
        _deleteError.value = null
    }
}
