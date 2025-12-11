package com.dagsbalken.app.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dagsbalken.app.ui.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferences(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        private val THEME_OPTION_KEY = stringPreferencesKey("theme_option")
    }

    fun themeOptionFlow(): Flow<ThemeOption> = appContext.themeDataStore.data
        .map { prefs ->
            val savedValue = prefs[THEME_OPTION_KEY]
            // Fallback to Cold (previously NordicCalm)
            // Handle legacy naming mapping if needed, or just default to Cold if mismatch
            val mappedOption = ThemeOption.values().firstOrNull { it.name == savedValue }

            if (mappedOption != null) {
                mappedOption
            } else {
                // Legacy mapping attempt
                when (savedValue) {
                    "NordicCalm" -> ThemeOption.Cold
                    "SolarDawn" -> ThemeOption.Warm
                    else -> ThemeOption.Cold
                }
            }
        }

    suspend fun setThemeOption(option: ThemeOption) {
        appContext.themeDataStore.edit { prefs ->
            prefs[THEME_OPTION_KEY] = option.name
        }
    }
}
