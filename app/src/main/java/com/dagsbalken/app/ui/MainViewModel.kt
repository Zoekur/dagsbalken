package com.dagsbalken.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagsbalken.app.ui.settings.AppPreferences
import com.dagsbalken.app.ui.settings.ThemePreferences
import com.dagsbalken.app.ui.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    private val themePreferences: ThemePreferences,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val themeOptionFlow: Flow<ThemeOption> = themePreferences.themeOptionFlow()

    // Visibility Flows
    val showClockFlow = appPreferences.showClock
    val showEventsFlow = appPreferences.showEvents
    val showTimersFlow = appPreferences.showTimers
    val showWeatherFlow = appPreferences.showWeather
    val showClothingFlow = appPreferences.showClothing

    fun onThemeOptionChange(option: ThemeOption) {
        viewModelScope.launch {
            themePreferences.setThemeOption(option)
        }
    }

    fun setShowClock(show: Boolean) = viewModelScope.launch { appPreferences.setShowClock(show) }
    fun setShowEvents(show: Boolean) = viewModelScope.launch { appPreferences.setShowEvents(show) }
    fun setShowTimers(show: Boolean) = viewModelScope.launch { appPreferences.setShowTimers(show) }
    fun setShowWeather(show: Boolean) = viewModelScope.launch { appPreferences.setShowWeather(show) }
    fun setShowClothing(show: Boolean) = viewModelScope.launch { appPreferences.setShowClothing(show) }
}
