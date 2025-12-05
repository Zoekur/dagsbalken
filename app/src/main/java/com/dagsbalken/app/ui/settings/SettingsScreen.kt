package com.dagsbalken.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dagsbalken.app.data.WeatherLocationSettings
import com.dagsbalken.app.data.WeatherRepository
import com.dagsbalken.app.ui.icons.DagsbalkenIcons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val weatherRepository = remember { WeatherRepository(context) }

    val locationSettings by weatherRepository.locationSettingsFlow.collectAsState(initial = WeatherLocationSettings())

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                          permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) {
                // If granted, we ensure the setting is set to true
                scope.launch {
                    weatherRepository.saveLocationSettings(true, locationSettings.manualLocationName)
                }
            } else {
                // If denied, we might revert the toggle or just leave it.
                // For better UX, we could show a snackbar, but let's stick to simple logic:
                // If denied, we turn off "Use current location" to be consistent.
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
                .padding(16.dp)
        ) {
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

            // Manual Location Input (Visible if Current Location is OFF)
            if (!locationSettings.useCurrentLocation) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = locationSettings.manualLocationName,
                    onValueChange = { newName ->
                        scope.launch {
                            weatherRepository.saveLocationSettings(false, newName)
                        }
                    },
                    label = { Text("Ange ort") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Provider Info
            Text("Väderleverantör: Open-Meteo (Standard)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Kalender", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // Placeholder for Calendar Selection
            Text("Kalenderkälla: Enhetskalender", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
