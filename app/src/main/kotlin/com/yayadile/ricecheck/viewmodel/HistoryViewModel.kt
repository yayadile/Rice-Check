package com.yayadile.ricecheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayadile.ricecheck.data.local.ScanResultEntity
import com.yayadile.ricecheck.data.repo.ScanRepository
import com.yayadile.ricecheck.domain.VerdictCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Filter labels from docs/IA.md §4 — no counts, no invented rows. */
enum class HistoryFilter(val label: String, val resultValue: String?) {
    ALL("Semua", null),
    ASLI("Asli", VerdictCalculator.ASLI),
    OPLOSAN("Berpotensi Oplosan", VerdictCalculator.BERPOTENSI_OPLOSAN)
}

class HistoryViewModel(repository: ScanRepository) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    val filter: StateFlow<HistoryFilter> = _filter.asStateFlow()

    val scans: StateFlow<List<ScanResultEntity>> = combine(
        repository.observeAll(),
        _filter
    ) { all, selected ->
        val wanted = selected.resultValue
        if (wanted == null) all else all.filter { it.result == wanted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList()
    )

    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
    }
}
