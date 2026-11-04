package com.yayadile.ricecheck

import android.app.Application
import com.yayadile.ricecheck.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RiceCheckApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        clearLegacyStubRowsOnce()
    }

    /**
     * Before the real model shipped every history row came from the stub. Delete them once
     * so the user never sees fabricated analysis next to real results (PROJECT_STATUS §12.3).
     */
    private fun clearLegacyStubRowsOnce() {
        applicationScope.launch {
            runCatching {
                if (!container.onboardingPrefs.legacyStubRowsCleared.first()) {
                    container.scanDao.deleteAll()
                    container.onboardingPrefs.setLegacyStubRowsCleared(true)
                }
            }
        }
    }
}
