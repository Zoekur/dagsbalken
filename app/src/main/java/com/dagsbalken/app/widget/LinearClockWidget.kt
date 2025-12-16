package com.dagsbalken.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.glance.currentState
import com.dagsbalken.core.data.CalendarRepository
import com.dagsbalken.core.data.WeatherRepository
import com.dagsbalken.core.data.DayEvent
import com.dagsbalken.core.data.WeatherData
import com.dagsbalken.core.widget.LinearClockPrefs
import com.dagsbalken.core.widget.WidgetConfig
import java.time.LocalTime
import android.util.Log

import com.dagsbalken.app.widget.LinearClockBitmapGenerator

object LinearClockWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    suspend fun updateAll(context: Context) {
        GlanceAppWidgetManager(context)
            .getGlanceIds(LinearClockWidget::class.java)
            .forEach { id ->
                update(context, id)
            }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weatherRepo = WeatherRepository(context)
        val calendarRepo = CalendarRepository(context)

        // Hämta event (detta sker vid uppdatering)
        // Wrappar i try-catch för att undvika krasch om rättigheter saknas
        val events = try {
            calendarRepo.getEventsForToday()
        } catch (e: Exception) {
            Log.e("LinearClockWidget", "Failed to load events", e)
            emptyList<DayEvent>()
        }

        provideContent {
            // Fånga eventuella fel i composition
            try {
                val weatherData by weatherRepo.weatherDataFlow.collectAsState(initial = null)
                val prefs = currentState<androidx.datastore.preferences.core.Preferences>()

                val config = WidgetConfig(
                    font = prefs[LinearClockPrefs.FONT_FAMILY] ?: LinearClockPrefs.DEF_FONT,
                    scale = prefs[LinearClockPrefs.FONT_SCALE] ?: LinearClockPrefs.DEF_SCALE,
                    backgroundColor = prefs[LinearClockPrefs.COLOR_BG] ?: LinearClockPrefs.DEF_BG,
                    textColor = prefs[LinearClockPrefs.COLOR_TEXT] ?: LinearClockPrefs.DEF_TEXT,
                    accentColor = prefs[LinearClockPrefs.COLOR_ACCENT] ?: LinearClockPrefs.DEF_ACCENT,
                    hoursToShow = prefs[LinearClockPrefs.HOURS_TO_SHOW] ?: LinearClockPrefs.DEF_HOURS_TO_SHOW,
                    showClock = prefs[LinearClockPrefs.SHOW_CLOCK] ?: LinearClockPrefs.DEF_SHOW_CLOCK,
                    showEvents = prefs[LinearClockPrefs.SHOW_EVENTS] ?: LinearClockPrefs.DEF_SHOW_EVENTS,
                    showWeather = prefs[LinearClockPrefs.SHOW_WEATHER] ?: LinearClockPrefs.DEF_SHOW_WEATHER,
                    showClothing = prefs[LinearClockPrefs.SHOW_CLOTHING] ?: LinearClockPrefs.DEF_SHOW_CLOTHING,
                    clockSize = prefs[LinearClockPrefs.CLOCK_SIZE] ?: LinearClockPrefs.DEF_CLOCK_SIZE
                )

                LinearClockWidgetContent(weatherData, events, config)
            } catch (e: Exception) {
                Log.e("LinearClockWidget", "Composition error", e)
                // Fallback UI
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Widget Error")
                }
            }
        }
    }
}

@Composable
private fun LinearClockWidgetContent(
    weatherData: WeatherData?,
    events: List<DayEvent>,
    config: WidgetConfig
) {
    val context = LocalContext.current
    val size = LocalSize.current

    // Enkel logik för px-beräkning
    val density = context.resources.displayMetrics.density
    val widthPx = (size.width.value * density).toInt().coerceAtLeast(300)

    // Determine height based on config
    val clockHeightDp = when(config.clockSize) {
        LinearClockPrefs.SIZE_4x2 -> 160.dp
        else -> 80.dp // 4x1 and 2x1
    }

    val heightPx = (clockHeightDp.value * density).toInt().coerceAtLeast(100)

    Column(
        GlanceModifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        // 1. TOP SECTION: Klockan
        if (config.showClock) {
            Box(
                GlanceModifier
                    .fillMaxWidth()
                    .height(clockHeightDp)
                    .padding(2.dp)
            ) {
                // Generate bitmap with correct events visibility
                // If showEvents is false, we pass empty list OR handle it in generator
                // Passing empty list is easiest if we want to hide them.
                val eventsToShow = if (config.showEvents) events else emptyList()

                // Safe generation
                val bitmap = try {
                    LinearClockBitmapGenerator.generate(
                        context = context,
                        width = widthPx,
                        height = heightPx,
                        events = eventsToShow,
                        config = config,
                        currentTime = LocalTime.now()
                    )
                } catch (e: Exception) {
                    Log.e("LinearClockWidget", "Bitmap generation failed", e)
                    null
                }

                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = "Linear Clock",
                        modifier = GlanceModifier.fillMaxSize()
                    )
                } else {
                Text("Clock Error")
                }
            }
            Spacer(GlanceModifier.height(8.dp))
        }

        // 2. BOTTOM SECTION (Weather / Clothing)
        if (config.showWeather || config.showClothing) {
            Row(GlanceModifier.fillMaxWidth().height(120.dp)) {

                // Determine if we show both
                val showBoth = config.showWeather && config.showClothing
                val modifier = if (showBoth) GlanceModifier.defaultWeight() else GlanceModifier.fillMaxWidth()

                if (config.showWeather) {
                    WeatherCard(
                        weatherData = weatherData,
                        modifier = modifier,
                        showClothingIcon = false // Standard weather view
                    )
                }

                if (showBoth) {
                    Spacer(GlanceModifier.width(8.dp))
                }

                if (config.showClothing) {
                    // Clothing card presented as in main app (but limited for widget).
                    // Main app clothing card usually implies advice.
                    // Here we can use the adviceIcon or text if available, but memory says "text descriptions excluded".
                    // However, for "Clothing Card", maybe icon is the key.
                    // If we show "Weather" and "Clothing" separate, maybe Weather shows Sun/Cloud, Clothing shows Jacket/Umbrella?
                    // Currently adviceIcon mixes them.
                    // We will use the same card style but maybe focus on the icon?
                    WeatherCard(
                        weatherData = weatherData,
                        modifier = modifier,
                        showClothingIcon = true,
                        title = "Klädsel"
                    )
                }
            }
        }
    }
}
