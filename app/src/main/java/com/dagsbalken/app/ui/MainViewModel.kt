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

    // AOD Settings Flows
    val aodColorFlow = appPreferences.aodColor
    val aodOpacityFlow = appPreferences.aodOpacity
    val aodPositionPercentFlow = appPreferences.aodPositionPercent

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

    fun setAodColor(color: Int) = viewModelScope.launch { appPreferences.setAodColor(color) }
    fun setAodOpacity(opacity: Float) = viewModelScope.launch { appPreferences.setAodOpacity(opacity) }
    fun setAodPositionPercent(positionPercent: Float) = viewModelScope.launch { appPreferences.setAodPositionPercent(positionPercent) }
}
