package com.yayadile.ricecheck.di

import android.content.Context
import androidx.room.Room
import com.yayadile.ricecheck.data.local.AppDatabase
import com.yayadile.ricecheck.data.local.ScanDao
import com.yayadile.ricecheck.data.prefs.OnboardingPrefs
import com.yayadile.ricecheck.data.repo.ScanFileStore
import com.yayadile.ricecheck.data.repo.ScanRepository
import com.yayadile.ricecheck.data.repo.ScanRepositoryImpl
import com.yayadile.ricecheck.ml.RiceClassifier

/**
 * Manual DI — docs/architecture.md §7. No Hilt for v1.
 * Created once in RiceCheckApplication and read from ViewModel factories.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "ricecheck.db"
        ).build()
    }

    val scanDao: ScanDao by lazy { database.scanDao() }

    val riceClassifier: RiceClassifier by lazy { RiceClassifier(appContext) }

    val scanFileStore: ScanFileStore by lazy { ScanFileStore(appContext) }

    val scanRepository: ScanRepository by lazy {
        ScanRepositoryImpl(scanDao, riceClassifier)
    }

    val onboardingPrefs: OnboardingPrefs by lazy { OnboardingPrefs(appContext) }

    /**
     * True only when the YOLO or SVM asset is missing/unreadable. With the shipped models this is
     * false; if it were ever true, `classify` fails instead of showing fabricated numbers.
     */
    val isClassifierStub: Boolean by lazy { !riceClassifier.isModelAvailable }
}
