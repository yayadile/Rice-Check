package com.yayadile.ricecheck.viewmodel

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yayadile.ricecheck.data.prefs.OnboardingPrefs
import com.yayadile.ricecheck.data.repo.ScanFileStore
import com.yayadile.ricecheck.data.repo.ScanRepository

/**
 * Small hand-written factories (docs/architecture.md §7: "ViewModel dibuat via factory
 * sederhana") — no DI framework.
 */
fun homeViewModelFactory(repository: ScanRepository) = viewModelFactory {
    initializer { HomeViewModel(repository) }
}

fun historyViewModelFactory(repository: ScanRepository) = viewModelFactory {
    initializer { HistoryViewModel(repository) }
}

fun resultViewModelFactory(repository: ScanRepository, scanId: Long) = viewModelFactory {
    initializer { ResultViewModel(repository, scanId) }
}

fun historyDetailViewModelFactory(repository: ScanRepository, scanId: Long) = viewModelFactory {
    initializer { HistoryDetailViewModel(repository, scanId) }
}

fun analyzingViewModelFactory(
    repository: ScanRepository,
    photoPath: String,
    price: Int?
) = viewModelFactory {
    initializer { AnalyzingViewModel(repository, photoPath, price) }
}

fun cameraViewModelFactory(fileStore: ScanFileStore) = viewModelFactory {
    initializer { CameraViewModel(fileStore) }
}

fun onboardingViewModelFactory(prefs: OnboardingPrefs) = viewModelFactory {
    initializer { OnboardingViewModel(prefs) }
}
