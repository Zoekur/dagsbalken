package com.dagsbalken.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dagsbalken.app.ui.MainViewModel
import com.dagsbalken.app.ui.icons.DagsbalkenIcons
import com.dagsbalken.app.ui.theme.ThemeOption
import com.dagsbalken.app.ui.theme.ThemeSelector
import com.dagsbalken.core.data.WeatherLocationSettings
import com.dagsbalken.core.data.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit,
    onBack: () -> Unit,
    viewModel: MainViewModel? = null // Passed to access visibility settings
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val weatherRepository = remember { WeatherRepository(context) }

    val locationSettings by weatherRepository.locationSettingsFlow.collectAsState(initial = WeatherLocationSettings())
    val currentProvider by weatherRepository.providerFlow.collectAsState(initial = "Open-Meteo")
    var manualLocationText by rememberSaveable { mutableStateOf("") }

    // Visibility States (if ViewModel is provided)
    val showClock = viewModel?.showClockFlow?.collectAsState(initial = true)
    val showEvents = viewModel?.showEventsFlow?.collectAsState(initial = true)
    val showTimers = viewModel?.showTimersFlow?.collectAsState(initial = true)
    val showWeather = viewModel?.showWeatherFlow?.collectAsState(initial = true)
    val showClothing = viewModel?.showClothingFlow?.collectAsState(initial = true)


    LaunchedEffect(locationSettings.manualLocationName) {
        if (manualLocationText != locationSettings.manualLocationName) {
            manualLocationText = locationSettings.manualLocationName
        }
    }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                          permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) {
                scope.launch {
                    weatherRepository.saveLocationSettings(true, locationSettings.manualLocationName)
                }
            } else {
                scope.launch {
                    weatherRepository.saveLocationSettings(false, locationSettings.manualLocationName)
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inställningar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(DagsbalkenIcons.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Theme Section
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ThemeSelector(
                selectedOption = currentTheme,
                onOptionSelected = onThemeSelected
            )

            if (viewModel != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Visa på startsidan", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Toggles
                SettingsToggle("Tidslinje", showClock?.value ?: true) { viewModel.setShowClock(it) }
                SettingsToggle("Kalender (Events)", showEvents?.value ?: true) { viewModel.setShowEvents(it) }
                SettingsToggle("Timers", showTimers?.value ?: true) { viewModel.setShowTimers(it) }
                SettingsToggle("Väderkort", showWeather?.value ?: true) { viewModel.setShowWeather(it) }
                SettingsToggle("Klädrådskort", showClothing?.value ?: true) { viewModel.setShowClothing(it) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Väder", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Location Settings
            Text("Plats", style = MaterialTheme.typography.titleSmall)

            // Toggle for Current Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                         val newValue = !locationSettings.useCurrentLocation
                         if (newValue) {
                             // Check permission before enabling
                             val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                             if (hasPermission) {
                                 scope.launch { weatherRepository.saveLocationSettings(true, locationSettings.manualLocationName) }
                             } else {
                                 locationPermissionLauncher.launch(
                                     arrayOf(
                                         Manifest.permission.ACCESS_COARSE_LOCATION,
                                         Manifest.permission.ACCESS_FINE_LOCATION
                                     )
                                 )
                             }
                         } else {
                             scope.launch { weatherRepository.saveLocationSettings(false, locationSettings.manualLocationName) }
                         }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Använd nuvarande position",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = locationSettings.useCurrentLocation,
                    onCheckedChange = { checked ->
                        if (checked) {
                             val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                             if (hasPermission) {
                                 scope.launch { weatherRepository.saveLocationSettings(true, locationSettings.manualLocationName) }
                             } else {
                                 locationPermissionLauncher.launch(
                                     arrayOf(
                                         Manifest.permission.ACCESS_COARSE_LOCATION,
                                         Manifest.permission.ACCESS_FINE_LOCATION
                                     )
                                 )
                             }
                        } else {
                            scope.launch { weatherRepository.saveLocationSettings(false, locationSettings.manualLocationName) }
                        }
                    }
                )
            }

            // Manual Location Input
            if (!locationSettings.useCurrentLocation) {
                Spacer(modifier = Modifier.height(8.dp))

                var suggestions by remember { mutableStateOf(emptyList<WeatherRepository.LocationSuggestion>()) }
                var showSuggestions by remember { mutableStateOf(false) }
                var searchJob by remember { mutableStateOf<Job?>(null) }

                Column {
                    OutlinedTextField(
                        value = manualLocationText,
                        onValueChange = { newName: String ->
                            manualLocationText = newName
                            searchJob?.cancel()
                            searchJob = scope.launch {
                                if (newName.length >= 2) {
                                    delay(500)
                                    suggestions = weatherRepository.searchLocations(newName)
                                    showSuggestions = suggestions.isNotEmpty()
                                } else {
                                    showSuggestions = false
                                }
                            }
                        },
                        label = { Text("Ange ort") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (showSuggestions) {
                        DropdownMenu(
                            expanded = showSuggestions,
                            onDismissRequest = { showSuggestions = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            suggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion.displayName()) },
                                    onClick = {
                                        manualLocationText = suggestion.displayName()
                                        showSuggestions = false
                                        scope.launch {
                                            weatherRepository.cacheManualLocation(suggestion.displayName(), suggestion.latitude, suggestion.longitude)
                                            weatherRepository.saveLocationSettings(false, suggestion.displayName())
                                            weatherRepository.fetchAndSaveWeatherOnce()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Välj väderleverantör", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            val providerOptions = listOf("Open-Meteo", "Mock")
            var expanded by remember { mutableStateOf(false) }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$currentProvider",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    providerOptions.forEach { p ->
                        DropdownMenuItem(text = { Text(p) }, onClick = {
                            expanded = false
                            scope.launch {
                                weatherRepository.saveProvider(p)
                                val success = weatherRepository.fetchAndSaveWeatherOnce()
                                Toast.makeText(context, if (success) "Väder uppdaterat från $p" else "Uppdatering misslyckades, använder fallback", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Kalender", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Kalenderkälla: Enhetskalender", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SettingsToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
