package com.dagsbalken.app.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppPreferences(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_EVENTS = booleanPreferencesKey("show_events")
        val SHOW_TIMERS = booleanPreferencesKey("show_timers")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val SHOW_CLOTHING = booleanPreferencesKey("show_clothing")

        // AOD Settings
        val AOD_COLOR = intPreferencesKey("aod_color")
        val AOD_OPACITY = floatPreferencesKey("aod_opacity")
    }

    val showClock: Flow<Boolean> = appContext.appSettingsDataStore.data
        .map { prefs -> prefs[SHOW_CLOCK] ?: true }

    val showEvents: Flow<Boolean> = appContext.appSettingsDataStore.data
        .map { prefs -> prefs[SHOW_EVENTS] ?: true }

    val showTimers: Flow<Boolean> = appContext.appSettingsDataStore.data
        .map { prefs -> prefs[SHOW_TIMERS] ?: true }

    val showWeather: Flow<Boolean> = appContext.appSettingsDataStore.data
        .map { prefs -> prefs[SHOW_WEATHER] ?: true }

    val showClothing: Flow<Boolean> = appContext.appSettingsDataStore.data
        .map { prefs -> prefs[SHOW_CLOTHING] ?: true }

    val aodColor: Flow<Int> = appContext.appSettingsDataStore.data
        .map { prefs -> prefs[AOD_COLOR] ?: -65536 } // Default Red (0xFFFF0000 -> -65536 in signed Int)

    val aodOpacity: Flow<Float> = appContext.appSettingsDataStore.data
        .map { prefs -> prefs[AOD_OPACITY] ?: 0.5f }

    suspend fun setShowClock(show: Boolean) {
        appContext.appSettingsDataStore.edit { it[SHOW_CLOCK] = show }
    }

    suspend fun setShowEvents(show: Boolean) {
        appContext.appSettingsDataStore.edit { it[SHOW_EVENTS] = show }
    }

    suspend fun setShowTimers(show: Boolean) {
        appContext.appSettingsDataStore.edit { it[SHOW_TIMERS] = show }
    }

    suspend fun setShowWeather(show: Boolean) {
        appContext.appSettingsDataStore.edit { it[SHOW_WEATHER] = show }
    }

    suspend fun setShowClothing(show: Boolean) {
        appContext.appSettingsDataStore.edit { it[SHOW_CLOTHING] = show }
    }

    suspend fun setAodColor(color: Int) {
        appContext.appSettingsDataStore.edit { it[AOD_COLOR] = color }
    }

    suspend fun setAodOpacity(opacity: Float) {
        appContext.appSettingsDataStore.edit { it[AOD_OPACITY] = opacity }
    }
}
