package com.example.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// --- DataStore Setup ---
// Skapar en singleton DataStore för att lagra väderdata för huvudappen
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "main_app_weather")

// Nycklar för DataStore
object WeatherPreferencesKeys {
    val TEMP_CELSIUS = intPreferencesKey("temp_c")
    val PRECIP_CHANCE = intPreferencesKey("precip_chance_pct")
    val IS_COLD_ADVICE = stringPreferencesKey("advice_icon") // Sparar ikonen
    val ADVICE_TEXT = stringPreferencesKey("advice_text") // Sparar rådtexten
    val CLOTHING_TYPE = stringPreferencesKey("clothing_type") // Sparar typ av kläder som sträng
    val DATA_LOADED = booleanPreferencesKey("data_loaded")

    // New Location Settings
    val USE_CURRENT_LOCATION = booleanPreferencesKey("use_current_location")
    val MANUAL_LOCATION_NAME = stringPreferencesKey("manual_location_name")
}

// Data class för att representera väderdata i Compose
data class WeatherData(
    val temperatureCelsius: Int = 0,
    val precipitationChance: Int = 0, // i procent (0-100)
    val adviceIcon: String = "☁️",
    val adviceText: String = "Laddar väderdata...",
    val clothingType: String = "NORMAL",
    val isDataLoaded: Boolean = false
) {
    fun getClothingResourceId(): Int {
        return when (clothingType) {
            "COLD" -> com.dagsbalken.app.R.drawable.ic_clothing_cold
            "HOT" -> com.dagsbalken.app.R.drawable.ic_clothing_hot
            "RAIN" -> com.dagsbalken.app.R.drawable.ic_clothing_rain
            "NORMAL" -> com.dagsbalken.app.R.drawable.ic_clothing_normal
            else -> com.dagsbalken.app.R.drawable.ic_clothing_normal
        }
    }
}

// Data class for location settings
data class WeatherLocationSettings(
    val useCurrentLocation: Boolean = true,
    val manualLocationName: String = ""
)

// Konstanter för klädrådslogik (enligt din skiss)
object ClothingAdvice {
    // Kallt: Rekommenderar varma kläder vid denna temp eller kallare
    const val COLD_THRESHOLD_C = 5
    // Varmt: Rekommenderar lätta kläder vid denna temp eller varmare
    const val HOT_THRESHOLD_C = 25
    // Regn: Rekommenderar paraply vid denna risk eller högre
    const val PRECIPITATION_THRESHOLD_PCT = 30
}

// --- Repository (Logiken för att läsa/skriva data) ---
class WeatherRepository(private val context: Context) {
    private val dataStore = context.dataStore

    // Flow som tillhandahåller väderdata i realtid till Compose
    val weatherDataFlow = dataStore.data
        .map { prefs ->
            WeatherData(
                temperatureCelsius = prefs[WeatherPreferencesKeys.TEMP_CELSIUS] ?: 15,
                precipitationChance = prefs[WeatherPreferencesKeys.PRECIP_CHANCE] ?: 0,
                adviceIcon = prefs[WeatherPreferencesKeys.IS_COLD_ADVICE] ?: "☁️",
                adviceText = prefs[WeatherPreferencesKeys.ADVICE_TEXT] ?: "Väntar på data...",
                clothingType = prefs[WeatherPreferencesKeys.CLOTHING_TYPE] ?: "NORMAL",
                isDataLoaded = prefs[WeatherPreferencesKeys.DATA_LOADED] ?: false
            )
        }

    // Flow for location settings
    val locationSettingsFlow: Flow<WeatherLocationSettings> = dataStore.data
        .map { prefs ->
            WeatherLocationSettings(
                useCurrentLocation = prefs[WeatherPreferencesKeys.USE_CURRENT_LOCATION] ?: true,
                manualLocationName = prefs[WeatherPreferencesKeys.MANUAL_LOCATION_NAME] ?: ""
            )
        }

    // Skriver ny väderdata till DataStore
    suspend fun saveWeatherData(temp: Int, precipChance: Int) {
        val (adviceText, adviceIcon, clothingType) = generateClothingAdvice(temp, precipChance)

        dataStore.edit { prefs ->
            prefs[WeatherPreferencesKeys.TEMP_CELSIUS] = temp
            prefs[WeatherPreferencesKeys.PRECIP_CHANCE] = precipChance
            prefs[WeatherPreferencesKeys.ADVICE_TEXT] = adviceText
            prefs[WeatherPreferencesKeys.IS_COLD_ADVICE] = adviceIcon
            prefs[WeatherPreferencesKeys.CLOTHING_TYPE] = clothingType
            prefs[WeatherPreferencesKeys.DATA_LOADED] = true
        }
    }

    // Save location settings
    suspend fun saveLocationSettings(useCurrent: Boolean, manualName: String) {
        dataStore.edit { prefs ->
            prefs[WeatherPreferencesKeys.USE_CURRENT_LOCATION] = useCurrent
            prefs[WeatherPreferencesKeys.MANUAL_LOCATION_NAME] = manualName
        }
    }

    // Kärnlogiken för klädråd
    private fun generateClothingAdvice(temp: Int, precipChance: Int): Triple<String, String, String> {
        return when {
            // Varma kläder: -5 grader eller lägre (enligt skissen)
            temp <= ClothingAdvice.COLD_THRESHOLD_C -> Triple(
                "Rekommenderar varma kläder: Jacka, mössa, handskar.",
                "🧥🧣🧤",
                "COLD"
            )
            // Lätta kläder: +30 grader eller högre (enligt skissen)
            temp > ClothingAdvice.HOT_THRESHOLD_C -> Triple(
                "Välj lätta kläder: Shorts och linne.",
                "🩳👕☀️",
                "HOT"
            )
            // Regn
            precipChance >= ClothingAdvice.PRECIPITATION_THRESHOLD_PCT -> Triple(
                "Hög risk för nederbörd (${precipChance}%). Ta med paraply eller regnjacka!",
                "☔️🌧️",
                "RAIN"
            )
            // Normalt
            else -> Triple(
                "Lätt jacka eller tröja är lagom.",
                "👕",
                "NORMAL"
            )
        }
    }
}
