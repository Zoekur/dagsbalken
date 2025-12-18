package com.dagsbalken.core.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale

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

    // Provider and metadata
    val PROVIDER = stringPreferencesKey("weather_provider")
    val LOCATION_NAME = stringPreferencesKey("weather_location_name")
    val LAST_UPDATED = longPreferencesKey("weather_last_updated")
    val FORWARD_GEOCODE_CACHE = stringPreferencesKey("forward_geocode_cache")
    val REVERSE_GEOCODE_CACHE = stringPreferencesKey("reverse_geocode_cache")
}

// Data class för att representera väderdata i Compose
data class WeatherData(
    val temperatureCelsius: Int = 0,
    val precipitationChance: Int = 0, // i procent (0-100)
    val adviceIcon: String = "☁️",
    val adviceText: String = "Laddar väderdata...",
    val clothingType: String = "NORMAL",
    val isDataLoaded: Boolean = false,
    val locationName: String = "",
    val lastUpdatedMillis: Long = 0L,
    val provider: String = ""
)

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
    data class LocationSuggestion(
        val name: String,
        val country: String?,
        val latitude: Double,
        val longitude: Double
    ) {
        fun displayName(): String = if (!country.isNullOrBlank()) "$name, $country" else name
    }
    private val dataStore = context.dataStore
    private val httpClient: OkHttpClient by lazy { createHttpClient() }

    companion object {
        private const val TAG = "WeatherRepository"
        private const val GEOCODE_CACHE_TTL_MS = 6 * 60 * 60 * 1000L // 6h
        private const val CACHE_MAX_ENTRIES = 64

        private data class ForwardEntry(val lat: Double, val lon: Double, val displayName: String, val timestamp: Long)
        private data class ReverseEntry(val displayName: String, val timestamp: Long)

        private val forwardCache = object : LinkedHashMap<String, ForwardEntry>(CACHE_MAX_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ForwardEntry>): Boolean = size > CACHE_MAX_ENTRIES
        }
        private val reverseCache = object : LinkedHashMap<String, ReverseEntry>(CACHE_MAX_ENTRIES, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ReverseEntry>): Boolean = size > CACHE_MAX_ENTRIES
        }
        @Volatile private var cachesLoaded = false
        @Volatile private var cachesDirty = false
        private val cacheLoadLock = Any()

        private fun normalizedQuery(query: String): String = query.trim().lowercase(Locale.getDefault())
        private fun reverseKey(lat: Double, lon: Double): String = String.format(Locale.US, "%.4f,%.4f", lat, lon)

        private fun getForwardCache(query: String): ForwardEntry? = synchronized(forwardCache) {
            val key = normalizedQuery(query)
            val entry = forwardCache[key]
            val now = System.currentTimeMillis()
            if (entry != null && now - entry.timestamp <= GEOCODE_CACHE_TTL_MS) entry else {
                if (entry != null) forwardCache.remove(key)
                null
            }
        }

        private fun putForwardCache(query: String, entry: ForwardEntry) = synchronized(forwardCache) {
            forwardCache[normalizedQuery(query)] = entry
            cachesDirty = true
        }

        private fun getReverseCache(lat: Double, lon: Double): ReverseEntry? = synchronized(reverseCache) {
            val key = reverseKey(lat, lon)
            val entry = reverseCache[key]
            val now = System.currentTimeMillis()
            if (entry != null && now - entry.timestamp <= GEOCODE_CACHE_TTL_MS) entry else {
                if (entry != null) reverseCache.remove(key)
                null
            }
        }

        private fun putReverseCache(lat: Double, lon: Double, entry: ReverseEntry) = synchronized(reverseCache) {
            reverseCache[reverseKey(lat, lon)] = entry
            cachesDirty = true
        }

        private fun loadForwardCacheFromJson(json: String?) {
            if (json.isNullOrBlank()) return
            val now = System.currentTimeMillis()
            try {
                val arr = JSONArray(json)
                synchronized(forwardCache) {
                    forwardCache.clear()
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val key = obj.optString("q")
                        val lat = obj.optDouble("lat", Double.NaN)
                        val lon = obj.optDouble("lon", Double.NaN)
                        val displayName = obj.optString("name", "")
                        val ts = obj.optLong("ts", 0L)
                        if (key.isNotBlank() && !lat.isNaN() && !lon.isNaN() && now - ts <= GEOCODE_CACHE_TTL_MS) {
                            forwardCache[key] = ForwardEntry(lat, lon, displayName, ts)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load forward cache")
            }
        }

        private fun loadReverseCacheFromJson(json: String?) {
            if (json.isNullOrBlank()) return
            val now = System.currentTimeMillis()
            try {
                val arr = JSONArray(json)
                synchronized(reverseCache) {
                    reverseCache.clear()
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val key = obj.optString("key")
                        val displayName = obj.optString("name", "")
                        val ts = obj.optLong("ts", 0L)
                        if (key.isNotBlank() && displayName.isNotBlank() && now - ts <= GEOCODE_CACHE_TTL_MS) {
                            reverseCache[key] = ReverseEntry(displayName, ts)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load reverse cache")
            }
        }

        private fun dumpForwardCacheToJson(): String {
            val arr = JSONArray()
            synchronized(forwardCache) {
                forwardCache.forEach { (key, entry) ->
                    val obj = JSONObject()
                        .put("q", key)
                        .put("lat", entry.lat)
                        .put("lon", entry.lon)
                        .put("name", entry.displayName)
                        .put("ts", entry.timestamp)
                    arr.put(obj)
                }
            }
            return arr.toString()
        }

        private fun dumpReverseCacheToJson(): String {
            val arr = JSONArray()
            synchronized(reverseCache) {
                reverseCache.forEach { (key, entry) ->
                    val obj = JSONObject()
                        .put("key", key)
                        .put("name", entry.displayName)
                        .put("ts", entry.timestamp)
                    arr.put(obj)
                }
            }
            return arr.toString()
        }

        private fun markCachesLoaded() {
            cachesLoaded = true
        }

        private fun areCachesLoaded(): Boolean = cachesLoaded

        private fun consumeDirtyFlag(): Boolean {
            return if (cachesDirty) {
                cachesDirty = false
                true
            } else false
        }
    }

    private suspend fun ensureCachesLoaded() {
        if (areCachesLoaded()) return
        val prefs = dataStore.data.first()
        synchronized(cacheLoadLock) {
            if (!areCachesLoaded()) {
                loadForwardCacheFromJson(prefs[WeatherPreferencesKeys.FORWARD_GEOCODE_CACHE])
                loadReverseCacheFromJson(prefs[WeatherPreferencesKeys.REVERSE_GEOCODE_CACHE])
                markCachesLoaded()
            }
        }
    }

    private suspend fun persistCachesIfNeeded() {
        if (!consumeDirtyFlag()) return
        val forwardJson = dumpForwardCacheToJson()
        val reverseJson = dumpReverseCacheToJson()
        dataStore.edit { prefs ->
            prefs[WeatherPreferencesKeys.FORWARD_GEOCODE_CACHE] = forwardJson
            prefs[WeatherPreferencesKeys.REVERSE_GEOCODE_CACHE] = reverseJson
        }
    }

    // Flow som tillhandahåller väderdata i realtid till Compose
    val weatherDataFlow = dataStore.data
        .map { prefs ->
            WeatherData(
                temperatureCelsius = prefs[WeatherPreferencesKeys.TEMP_CELSIUS] ?: 15,
                precipitationChance = prefs[WeatherPreferencesKeys.PRECIP_CHANCE] ?: 0,
                adviceIcon = prefs[WeatherPreferencesKeys.IS_COLD_ADVICE] ?: "☁️",
                adviceText = prefs[WeatherPreferencesKeys.ADVICE_TEXT] ?: "Väntar på data...",
                clothingType = prefs[WeatherPreferencesKeys.CLOTHING_TYPE] ?: "NORMAL",
                isDataLoaded = prefs[WeatherPreferencesKeys.DATA_LOADED] ?: false,
                locationName = prefs[WeatherPreferencesKeys.LOCATION_NAME] ?: "",
                lastUpdatedMillis = prefs[WeatherPreferencesKeys.LAST_UPDATED] ?: 0L,
                provider = prefs[WeatherPreferencesKeys.PROVIDER] ?: "Open-Meteo"
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

    // Flow for provider
    val providerFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[WeatherPreferencesKeys.PROVIDER] ?: "Open-Meteo" }

    // Skriver ny väderdata till DataStore
    suspend fun saveWeatherData(temp: Int, precipChance: Int, locationName: String = "", provider: String = "") {
        val (adviceText, adviceIcon, clothingType) = generateClothingAdvice(temp, precipChance)

        val now = System.currentTimeMillis()
        dataStore.edit { prefs ->
            prefs[WeatherPreferencesKeys.TEMP_CELSIUS] = temp
            prefs[WeatherPreferencesKeys.PRECIP_CHANCE] = precipChance
            prefs[WeatherPreferencesKeys.ADVICE_TEXT] = adviceText
            prefs[WeatherPreferencesKeys.IS_COLD_ADVICE] = adviceIcon
            prefs[WeatherPreferencesKeys.CLOTHING_TYPE] = clothingType
            prefs[WeatherPreferencesKeys.DATA_LOADED] = true
            prefs[WeatherPreferencesKeys.LOCATION_NAME] = locationName
            prefs[WeatherPreferencesKeys.LAST_UPDATED] = now
            prefs[WeatherPreferencesKeys.PROVIDER] = provider
        }
    }

    // Save location settings
    suspend fun saveLocationSettings(useCurrent: Boolean, manualName: String) {
        dataStore.edit { prefs ->
            prefs[WeatherPreferencesKeys.USE_CURRENT_LOCATION] = useCurrent
            prefs[WeatherPreferencesKeys.MANUAL_LOCATION_NAME] = manualName
        }
    }

    // Save provider
    suspend fun saveProvider(providerName: String) {
        dataStore.edit { prefs ->
            prefs[WeatherPreferencesKeys.PROVIDER] = providerName
        }
    }

    // Spara en manuellt vald plats i cachen så att fetchAndSaveWeatherOnce hittar den direkt
    fun cacheManualLocation(name: String, lat: Double, lon: Double) {
        putForwardCache(name, ForwardEntry(lat, lon, name, System.currentTimeMillis()))
    }

    // Sök efter platser via Open-Meteo Geocoding API
    suspend fun searchLocations(query: String): List<LocationSuggestion> {
        if (query.length < 2) return emptyList()
        val suggestions = mutableListOf<LocationSuggestion>()
        try {
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(query, "UTF-8")}&count=5&language=sv&format=json"
            val req = Request.Builder().url(url).get().build()
            val resp = httpClient.newCall(req).awaitResponse()
            resp.use { r ->
                if (r.isSuccessful) {
                    val body = r.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val results = json.optJSONArray("results")
                        if (results != null) {
                            for (i in 0 until results.length()) {
                                val item = results.getJSONObject(i)
                                val name = item.optString("name")
                                val country = item.optString("country", "")
                                val lat = item.optDouble("latitude")
                                val lon = item.optDouble("longitude")
                                if (name.isNotBlank() && !lat.isNaN() && !lon.isNaN()) {
                                    suggestions.add(LocationSuggestion(name, country, lat, lon))
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search locations failed")
        }
        return suggestions
    }

    // Helper function to extract "City, CC" from Nominatim JSON
    private fun formatLocationName(json: JSONObject): String {
        val addr = json.optJSONObject("address") ?: return json.optString("display_name", "")

        val city = addr.optString("city",
            addr.optString("town",
                addr.optString("village",
                    addr.optString("municipality",
                        addr.optString("hamlet",
                            addr.optString("county", "")
                        )
                    )
                )
            )
        )

        val countryCode = addr.optString("country_code", "").uppercase()

        if (city.isNotBlank() && countryCode.isNotBlank()) {
            return "$city, $countryCode"
        }

        return json.optString("display_name", "")
    }

    // Public helper to trigger an immediate fetch & save based on current settings.
    // Returns true if network fetch succeeded, false for simulated fallback.
    suspend fun fetchAndSaveWeatherOnce(): Boolean {
        ensureCachesLoaded()
        // Read settings once
        val prefs = dataStore.data.first()
        val useCurrent = prefs[WeatherPreferencesKeys.USE_CURRENT_LOCATION] ?: true
        val manualName = prefs[WeatherPreferencesKeys.MANUAL_LOCATION_NAME] ?: ""
        val provider = prefs[WeatherPreferencesKeys.PROVIDER] ?: "Open-Meteo"

        var temp: Int
        var precipChance: Int
        var locationName: String = ""

        if (provider == "Open-Meteo") {
            // Determine coordinates
            var lat: Double? = null
            var lon: Double? = null

            if (useCurrent) {
                try {
                    // Check permission before accessing location APIs
                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasFine || hasCoarse) {
                        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val providers = lm.getProviders(true)
                        var best: Location? = null
                        for (p in providers) {
                            val l = try { lm.getLastKnownLocation(p) } catch (_: SecurityException) { null }
                            if (l != null && (best == null || l.accuracy < best.accuracy)) {
                                best = l
                            }
                        }
                        if (best != null) {
                            lat = best.latitude
                            lon = best.longitude

                            // Reverse-geocode via Nominatim instead of Android Geocoder (with cache)
                            val cached = getReverseCache(lat!!, lon!!)
                            if (cached != null) {
                                locationName = cached.displayName
                            } else {
                                try {
                                    val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&accept-language=${URLEncoder.encode(Locale.getDefault().language, "UTF-8")}&zoom=10"
                                    val req = Request.Builder()
                                        .url(url)
                                        .header("User-Agent", "Dagsbalken/1.0")
                                        .get()
                                        .build()
                                    val resp = httpClient.newCall(req).awaitResponse()
                                    resp.use { r ->
                                        if (r.isSuccessful) {
                                            val body = r.body?.string()
                                            if (!body.isNullOrBlank()) {
                                                val json = JSONObject(body)
                                                // Use helper to format name
                                                locationName = formatLocationName(json)

                                                if (locationName.isBlank()) {
                                                    // Fallback to display_name or address logic if helper returned empty (unlikely as helper defaults to display_name)
                                                    locationName = json.optString("display_name", "")
                                                }
                                                if (locationName.isNotBlank()) {
                                                    putReverseCache(lat!!, lon!!, ReverseEntry(locationName, System.currentTimeMillis()))
                                                }
                                            }
                                        }
                                    }
                                } catch (_: Exception) {
                                    // ignore reverse geocode failures
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // ignore
                }
            } else {
                // manual: try to geocode the provided name to coordinates using Nominatim
                if (manualName.isNotBlank()) {
                    val cached = getForwardCache(manualName)
                    if (cached != null) {
                        lat = cached.lat
                        lon = cached.lon
                        locationName = cached.displayName
                    } else {
                        try {
                            val q = URLEncoder.encode(manualName, "UTF-8")
                            // Add addressdetails=1 to get fields for formatting
                            val url = "https://nominatim.openstreetmap.org/search?q=${q}&format=json&addressdetails=1&limit=1&accept-language=${URLEncoder.encode(Locale.getDefault().language, "UTF-8") }"
                            val req = Request.Builder()
                                .url(url)
                                .header("User-Agent", "Dagsbalken/1.0")
                                .get()
                                .build()

                            val resp = httpClient.newCall(req).awaitResponse()
                            resp.use { r ->
                                if (r.isSuccessful) {
                                    val body = r.body?.string()
                                    if (!body.isNullOrBlank()) {
                                        val arr = JSONArray(body)
                                        if (arr.length() > 0) {
                                            val first = arr.getJSONObject(0)
                                            val latStr = first.optString("lat", null)
                                            val lonStr = first.optString("lon", null)
                                            val parsedLat = latStr?.toDoubleOrNull() ?: first.optDouble("lat", Double.NaN)
                                            val parsedLon = lonStr?.toDoubleOrNull() ?: first.optDouble("lon", Double.NaN)
                                            if (!parsedLat.isNaN() && !parsedLon.isNaN()) {
                                                lat = parsedLat
                                                lon = parsedLon
                                                // Use helper
                                                locationName = formatLocationName(first)
                                                if (locationName.isBlank()) locationName = first.optString("display_name", manualName)

                                                putForwardCache(
                                                    manualName,
                                                    ForwardEntry(parsedLat, parsedLon, locationName.ifBlank { manualName }, System.currentTimeMillis())
                                                )
                                            }
                                        } else {
                                            locationName = manualName
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            locationName = manualName
                        }
                    }
                }
            }

            if (lat != null && lon != null) {
                // Use OkHttp to call Open-Meteo with retries
                val url = "https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true&timezone=auto"
                var attempt = 0
                val maxAttempts = 2
                while (attempt <= maxAttempts) {
                    try {
                        val req = Request.Builder().url(url).get().build()
                        val call = httpClient.newCall(req)
                        val resp = call.awaitResponse()
                        resp.use { r ->
                            if (r.isSuccessful) {
                                val body = r.body?.string()
                                if (body != null) {
                                    val json = JSONObject(body)
                                    val current = json.optJSONObject("current_weather")
                                    if (current != null) {
                                        temp = current.optDouble("temperature", Double.NaN).toInt()
                                        precipChance = 0 // Open-Meteo current doesn't include precip probability
                                        if (locationName.isBlank()) locationName = json.optString("timezone", "")
                                        saveWeatherData(temp, precipChance, locationName, provider)
                                        return true
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Forecast fetch failed attempt $attempt")
                        // retry
                    }
                    attempt++
                }
                // If we reach here, network attempts failed -> fallthrough to fallback
            }
        }

        // Fallback / Mock provider or failed network -> simulated values
        if (useCurrent) {
            temp = (-5..25).random()
            precipChance = (0..50).random()
            if (locationName.isBlank()) locationName = "Min plats"
        } else {
            if (manualName.isNotBlank()) {
                val seed = manualName.length
                temp = (seed % 30)
                precipChance = (seed * 10 % 100)
                locationName = manualName
            } else {
                temp = 20
                precipChance = 0
                locationName = "Okänd plats"
            }
        }

        saveWeatherData(temp, precipChance, locationName, provider)
        persistCachesIfNeeded()
        return false
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
