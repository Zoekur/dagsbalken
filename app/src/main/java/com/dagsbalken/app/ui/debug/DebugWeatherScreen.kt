package com.dagsbalken.app.ui.debug

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dagsbalken.core.data.WeatherData
import com.dagsbalken.core.data.WeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugWeatherScreen(
    weatherRepository: WeatherRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val effectiveWeather by weatherRepository.weatherDataFlow.collectAsState(initial = WeatherData())
    val mockEnabled by weatherRepository.isMockWeatherEnabledFlow.collectAsState(initial = false)

    var tempText by rememberSaveable { mutableStateOf("") }
    var precipText by rememberSaveable { mutableStateOf("") }
    var adviceIconText by rememberSaveable { mutableStateOf("") }
    var adviceText by rememberSaveable { mutableStateOf("") }
    var clothingTypeText by rememberSaveable { mutableStateOf("") }
    var locationText by rememberSaveable { mutableStateOf("") }
    var providerText by rememberSaveable { mutableStateOf("") }
    var lastUpdatedText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Starta med nuvarande effektiva väderdata som förval i formuläret.
        // Om ingen explicit hittepå-data finns än, använd samma deterministiska logik
        // som fetchAndSaveWeatherOnce använder när provider är "Hittepå".
        val prefsBased = weatherRepository.getEffectiveWeatherOnce()

        val base: WeatherData = if (!prefsBased.isDataLoaded) {
            // Generera hittepå-data enligt samma princip som provider == "Hittepå"
            val locationSettings = weatherRepository.locationSettingsFlow.first()
            val useCurrent = locationSettings.useCurrentLocation
            val manualName = locationSettings.manualLocationName
            val (temp, precip, loc) = if (useCurrent) {
                Triple(20, 10, if (manualName.isNotBlank()) manualName else "Hittepå-plats")
            } else {
                if (manualName.isNotBlank()) {
                    val seed = manualName.length
                    val tempSeed = seed % 30
                    val precipSeed = (seed * 7) % 100
                    Triple(tempSeed, precipSeed, manualName)
                } else {
                    Triple(18, 0, "Hittepå-plats")
                }
            }
            WeatherData(
                temperatureCelsius = temp,
                precipitationChance = precip,
                adviceIcon = "☁️",
                adviceText = "Hittepå-data",
                clothingType = "NORMAL",
                isDataLoaded = true,
                locationName = loc,
                lastUpdatedMillis = System.currentTimeMillis(),
                provider = "Hittepå"
            )
        } else {
            prefsBased
        }

        tempText = base.temperatureCelsius.toString()
        precipText = base.precipitationChance.toString()
        adviceIconText = base.adviceIcon
        adviceText = base.adviceText
        clothingTypeText = base.clothingType
        locationText = base.locationName
        providerText = base.provider.ifBlank { "Hittepå" }
        lastUpdatedText = if (base.lastUpdatedMillis > 0L) base.lastUpdatedMillis.toString() else System.currentTimeMillis().toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Debug") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Läge: " + if (mockEnabled) "HITTEPÅ" else "REAL",
                style = MaterialTheme.typography.titleMedium,
                color = if (mockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            // Kort sammanfattning av aktuell data
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Aktuell data (effektiv):", style = MaterialTheme.typography.titleSmall)
                Text("Temp: ${effectiveWeather.temperatureCelsius} °C")
                Text("Nederbördschans: ${effectiveWeather.precipitationChance} %")
                Text("Ikon: ${effectiveWeather.adviceIcon}")
                Text("Råd: ${effectiveWeather.adviceText}")
                Text("Klädtyp: ${effectiveWeather.clothingType}")
                Text("Plats: ${effectiveWeather.locationName}")
                val lastUpdatedLabel = if (effectiveWeather.lastUpdatedMillis > 0L) {
                    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    df.format(Date(effectiveWeather.lastUpdatedMillis))
                } else "--"
                Text("Senast uppdaterad: $lastUpdatedLabel")
                Text("Provider: ${effectiveWeather.provider}")
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Använd hittepå-väderdata", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = mockEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            weatherRepository.setMockWeatherEnabled(enabled)
                        }
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            Text("Hittepå-data (redigerbar)", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = tempText,
                onValueChange = { tempText = it },
                label = { Text("Temperatur (°C)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precipText,
                onValueChange = { precipText = it },
                label = { Text("Nederbördschans (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text("Platsnamn") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = adviceText,
                onValueChange = { adviceText = it },
                label = { Text("Rådtext") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = adviceIconText,
                onValueChange = { adviceIconText = it },
                label = { Text("Rådikon (emoji)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clothingTypeText,
                onValueChange = { clothingTypeText = it },
                label = { Text("Klädtyp (NORMAL/COLD/HOT/RAIN)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = providerText,
                onValueChange = { providerText = it },
                label = { Text("Provider") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lastUpdatedText,
                onValueChange = { lastUpdatedText = it },
                label = { Text("LastUpdated millis (epoch)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val nowMillis = System.currentTimeMillis()
                        lastUpdatedText = nowMillis.toString()
                    }
                ) {
                    Text("Sätt tid till nu")
                }

                Button(
                    onClick = {
                        scope.launch {
                            val real = weatherRepository.getRealWeatherOnce()
                            tempText = real.temperatureCelsius.toString()
                            precipText = real.precipitationChance.toString()
                            adviceIconText = real.adviceIcon
                            adviceText = real.adviceText
                            clothingTypeText = real.clothingType
                            locationText = real.locationName
                            providerText = real.provider.ifBlank { "Hittepå" }
                            lastUpdatedText = (if (real.lastUpdatedMillis > 0L) real.lastUpdatedMillis else System.currentTimeMillis()).toString()
                        }
                    }
                ) {
                    Text("Ladda real-snapshot")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val temp = tempText.toIntOrNull() ?: 0
                            val precip = precipText.toIntOrNull()?.coerceIn(0, 100) ?: 0
                            val lastUpdated = lastUpdatedText.toLongOrNull() ?: System.currentTimeMillis()

                            val data = WeatherData(
                                temperatureCelsius = temp,
                                precipitationChance = precip,
                                adviceIcon = adviceIconText.ifBlank { "☁️" },
                                adviceText = adviceText.ifBlank { "Hittepå-data" },
                                clothingType = clothingTypeText.ifBlank { "NORMAL" },
                                isDataLoaded = true,
                                locationName = locationText,
                                lastUpdatedMillis = lastUpdated,
                                provider = providerText.ifBlank { "Hittepå" }
                            )
                            weatherRepository.saveMockWeatherData(data)
                            weatherRepository.setMockWeatherEnabled(true)
                            Toast.makeText(context, "Hittepå-väder sparat", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Spara hittepå-data")
                }

                Button(
                    onClick = {
                        scope.launch {
                            weatherRepository.clearMockWeatherData()
                            Toast.makeText(context, "Hittepå-data rensad", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rensa hittepå")
                }
            }
        }
    }
}
