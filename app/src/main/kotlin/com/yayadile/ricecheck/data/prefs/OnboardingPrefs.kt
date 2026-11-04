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
 * `onboarding_completed` is kept out of Room on purpose. `legacy_stub_rows_cleared`
 * marks the one-time deletion of history rows created while the model was still a stub.
 */
class OnboardingPrefs(private val context: Context) {

    val onboardingCompleted: Flow<Boolean> = context.riceCheckDataStore.data
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.riceCheckDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    val legacyStubRowsCleared: Flow<Boolean> = context.riceCheckDataStore.data
        .map { prefs -> prefs[Keys.LEGACY_STUB_ROWS_CLEARED] ?: false }

    suspend fun setLegacyStubRowsCleared(cleared: Boolean) {
        context.riceCheckDataStore.edit { prefs ->
            prefs[Keys.LEGACY_STUB_ROWS_CLEARED] = cleared
        }
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LEGACY_STUB_ROWS_CLEARED = booleanPreferencesKey("legacy_stub_rows_cleared")
    }
}
