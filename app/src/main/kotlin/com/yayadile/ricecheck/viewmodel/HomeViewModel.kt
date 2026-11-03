package com.yayadile.ricecheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.data.repo.ScanRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val weeklyScanCount: Int = 0,
    val recentScans: List<ScanResultEntity> = emptyList()
)

class HomeViewModel(repository: ScanRepository) : ViewModel() {

    private val weekStartMillis: Long = LocalDate.now()
        .with(DayOfWeek.MONDAY)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeAll(),
        repository.countSince(weekStartMillis)
    ) { all, count ->
        HomeUiState(
            weeklyScanCount = count,
            recentScans = all.take(3)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState()
    )
}
