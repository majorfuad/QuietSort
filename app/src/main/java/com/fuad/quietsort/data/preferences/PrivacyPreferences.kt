package com.fuad.quietsort.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
    private val excludedPackages = stringSetPreferencesKey("excluded_packages")
    private val retentionDays = intPreferencesKey("retention_days")

    val isCaptureEnabled: Flow<Boolean> =
        context.privacyDataStore.data.map { it[captureEnabled] ?: false }

    val excludedApps: Flow<Set<String>> =
        context.privacyDataStore.data.map { it[excludedPackages] ?: emptySet() }

    // 30 days by default; user may select 1, 7, 30 or 90 days.
    val retentionPeriodDays: Flow<Int> =
        context.privacyDataStore.data.map { it[retentionDays] ?: 30 }

    suspend fun setCaptureEnabled(enabled: Boolean) {
        context.privacyDataStore.edit { it[captureEnabled] = enabled }
    }

    suspend fun setExcludedPackage(packageName: String, excluded: Boolean) {
        require(PACKAGE_PATTERN.matches(packageName)) { "Invalid package identifier" }
        context.privacyDataStore.edit { prefs ->
            val packages = (prefs[excludedPackages] ?: emptySet()).toMutableSet()
            if (excluded) packages.add(packageName) else packages.remove(packageName)
            prefs[excludedPackages] = packages
        }
    }

    suspend fun setRetentionDays(days: Int) {
        require(days in ALLOWED_RETENTION_DAYS) { "Invalid retention period" }
        context.privacyDataStore.edit { it[retentionDays] = days }
    }

    companion object {
        val ALLOWED_RETENTION_DAYS = setOf(1, 7, 30, 90)
        private val PACKAGE_PATTERN =
            Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+")
    }
}
