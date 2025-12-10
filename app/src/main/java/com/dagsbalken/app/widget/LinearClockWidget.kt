package com.dagsbalken.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
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

import com.dagsbalken.app.widget.LinearClockBitmapGenerator

object LinearClockWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weatherRepo = WeatherRepository(context)
        val calendarRepo = CalendarRepository(context)

        // Hämta event (detta sker vid uppdatering)
        val events = calendarRepo.getEventsForToday()

        provideContent {
            val weatherData by weatherRepo.weatherDataFlow.collectAsState(initial = null)
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()

            val config = WidgetConfig(
                font = prefs[LinearClockPrefs.FONT_FAMILY] ?: LinearClockPrefs.DEF_FONT,
                scale = prefs[LinearClockPrefs.FONT_SCALE] ?: LinearClockPrefs.DEF_SCALE,
                backgroundColor = prefs[LinearClockPrefs.COLOR_BG] ?: LinearClockPrefs.DEF_BG,
                textColor = prefs[LinearClockPrefs.COLOR_TEXT] ?: LinearClockPrefs.DEF_TEXT,
                accentColor = prefs[LinearClockPrefs.COLOR_ACCENT] ?: LinearClockPrefs.DEF_ACCENT,
                hoursToShow = prefs[LinearClockPrefs.HOURS_TO_SHOW] ?: LinearClockPrefs.DEF_HOURS_TO_SHOW
            )

            LinearClockWidgetContent(weatherData, events, config)
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

    // Enkel logik för px-beräkning (kan behöva justeras för exakt precision)
    val density = context.resources.displayMetrics.density
    val widthPx = (size.width.value * density).toInt().coerceAtLeast(300)
    val heightPx = (80 * density).toInt().coerceAtLeast(100)

    Column(
        GlanceModifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        // 1. TOP SECTION: Klockan
        Box(
            GlanceModifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(2.dp)
        ) {
            // OBS: Se till att LinearClockBitmapGenerator finns!
            val bitmap = LinearClockBitmapGenerator.generate(
                context = context,
                width = widthPx,
                height = heightPx,
                events = events,
                config = config,
                currentTime = LocalTime.now()
            )

            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Linear Clock",
                modifier = GlanceModifier.fillMaxSize()
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        // 2. BOTTOM SECTION
        Row(GlanceModifier.fillMaxWidth().height(120.dp)) {

            // 2.1 Weather Box
            Box(
                GlanceModifier
                    .defaultWeight() // RÄTTAT: Inga argument här!
                    .fillMaxHeight()
                    .cornerRadius(16.dp)
                    .padding(2.dp)
            ) {
                Column(
                    GlanceModifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (weatherData != null && weatherData.isDataLoaded) {
                        Text(
                            text = "${'$'}{weatherData.temperatureCelsius}°C",
                            style = TextStyle(fontSize = TextUnit(24.dp.value, TextUnitType.Sp), fontWeight = FontWeight.Bold)
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = weatherData.adviceText,
                            style = TextStyle(fontSize = TextUnit(12.dp.value, TextUnitType.Sp)),
                            maxLines = 4
                        )
                    } else {
                        Text("Laddar...")
                    }
                }
            }

            Spacer(GlanceModifier.width(8.dp))

            // 2.2 Clothing Box
            Box(
                GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .cornerRadius(16.dp)
                    .padding(2.dp)
            ) {
                Column(
                    GlanceModifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (weatherData != null && weatherData.isDataLoaded) {
                        // Show simple emoji/icon instead of unresolved image resource
                        Text(text = weatherData.adviceIcon)
                    } else {
                        Text("...")
                    }
                }
            }
        }
    }
}
