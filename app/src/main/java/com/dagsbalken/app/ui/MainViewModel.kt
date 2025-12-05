package com.dagsbalken.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagsbalken.app.ui.settings.ThemePreferences
import com.dagsbalken.app.ui.theme.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(private val themePreferences: ThemePreferences) : ViewModel() {
    // Note: showSettings state is handled by Navigation now, so we don't strictly need it here
    // unless we want to control navigation from VM logic, but the prompt suggested using VM for state.
    // However, with Navigation Compose, the "screen" state is implicitly the route.
    // We will still keep theme logic here.

    val themeOptionFlow: Flow<ThemeOption> = themePreferences.themeOptionFlow()

    fun onThemeOptionChange(option: ThemeOption) {
        viewModelScope.launch {
            themePreferences.setThemeOption(option)
        }
    }
}
