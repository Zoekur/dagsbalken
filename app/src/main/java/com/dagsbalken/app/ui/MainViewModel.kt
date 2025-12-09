package com.dagsbalken.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagsbalken.app.ui.settings.ThemePreferences
import com.dagsbalken.app.ui.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(private val themePreferences: ThemePreferences) : ViewModel() {

    val themeOptionFlow: Flow<ThemeOption> = themePreferences.themeOptionFlow()

    fun onThemeOptionChange(option: ThemeOption) {
        viewModelScope.launch {
            themePreferences.setThemeOption(option)
        }
    }
}
