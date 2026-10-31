package com.fuad.quietsort.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.privacyDataStore by preferencesDataStore(name = "privacy")

@Singleton
class PrivacyPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val captureEnabled = booleanPreferencesKey("capture_enabled")

    val isCaptureEnabled: Flow<Boolean> =
        context.privacyDataStore.data.map { preferences ->
            preferences[captureEnabled] ?: false
        }

    suspend fun setCaptureEnabled(enabled: Boolean) {
        context.privacyDataStore.edit { preferences ->
            preferences[captureEnabled] = enabled
        }
    }
}
