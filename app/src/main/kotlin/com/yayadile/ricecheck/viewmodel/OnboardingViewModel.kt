package com.yayadile.ricecheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayadile.ricecheck.data.prefs.OnboardingPrefs
import kotlinx.coroutines.launch

class OnboardingViewModel(private val prefs: OnboardingPrefs) : ViewModel() {

    /** Persists the single v1 preference key; the screen navigates on its own. */
    fun markCompleted() {
        viewModelScope.launch { prefs.setOnboardingCompleted(true) }
    }
}
