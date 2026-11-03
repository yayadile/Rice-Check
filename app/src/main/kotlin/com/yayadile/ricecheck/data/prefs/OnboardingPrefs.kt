package com.yayadile.ricecheck.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.riceCheckDataStore by preferencesDataStore(name = "ricecheck_prefs")

/**
 * DataStore preferences — docs/architecture.md §6.
 * Exactly one key in v1: onboarding_completed (kept out of Room on purpose).
 */
class OnboardingPrefs(private val context: Context) {

    val onboardingCompleted: Flow<Boolean> = context.riceCheckDataStore.data
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.riceCheckDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
