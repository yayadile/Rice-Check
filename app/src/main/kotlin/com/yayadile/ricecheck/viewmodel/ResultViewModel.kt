package com.yayadile.ricecheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.data.repo.ScanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ScanDetailState {
    data object Loading : ScanDetailState
    data class Loaded(val scan: ScanResultEntity?) : ScanDetailState
}

class ResultViewModel(
    repository: ScanRepository,
    scanId: Long
) : ViewModel() {

    val state: StateFlow<ScanDetailState> = repository.observeById(scanId)
        .map { ScanDetailState.Loaded(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ScanDetailState.Loading
        )
}
