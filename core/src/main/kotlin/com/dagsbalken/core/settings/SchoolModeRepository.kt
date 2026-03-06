package com.dagsbalken.core.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "dagsbalken_settings"

private val Context.settingsDataStore by preferencesDataStore(name = PREFERENCES_NAME)

object SchoolModePrefs {
    val SCHOOL_MODE_ENABLED = booleanPreferencesKey("school_mode_enabled")
    const val DEF_SCHOOL_MODE_ENABLED: Boolean = false
}

class SchoolModeRepository(private val context: Context) {

    private val dataStore get() = context.settingsDataStore

    val schoolModeEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs: Preferences ->
        prefs[SchoolModePrefs.SCHOOL_MODE_ENABLED] ?: SchoolModePrefs.DEF_SCHOOL_MODE_ENABLED
    }

    suspend fun setSchoolModeEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[SchoolModePrefs.SCHOOL_MODE_ENABLED] = enabled
        }
    }
}

