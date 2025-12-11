package com.dagsbalken.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.dagsbalken.app.ui.settings.ThemePreferences
import com.dagsbalken.app.ui.theme.DagsbalkenTheme
import com.dagsbalken.app.ui.theme.ThemeOption
import com.dagsbalken.core.widget.LinearClockPrefs
import com.dagsbalken.core.widget.WidgetConfig
import kotlinx.coroutines.launch

class LinearClockConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hämta widget ID
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            val themePreferences = remember { ThemePreferences(applicationContext) }
            val themeOption by themePreferences.themeOptionFlow().collectAsState(initial = ThemeOption.Cold)

            // Lokalt state för konfigurationen
            var config by remember { mutableStateOf(WidgetConfig()) }
            val scope = rememberCoroutineScope()

            DagsbalkenTheme(themeOption = themeOption) {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Widget-inställningar", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))

                        // COMPONENTS VISIBILITY
                        Text("Visa komponenter", style = MaterialTheme.typography.titleMedium)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.showClock, onCheckedChange = { config = config.copy(showClock = it) })
                            Text("Klocka (Tidslinje)")
                        }

                        if (config.showClock) {
                             Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                                Checkbox(checked = config.showEvents, onCheckedChange = { config = config.copy(showEvents = it) })
                                Text("Kalenderhändelser")
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.showWeather, onCheckedChange = { config = config.copy(showWeather = it) })
                            Text("Väder")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = config.showClothing, onCheckedChange = { config = config.copy(showClothing = it) })
                            Text("Klädsel")
                        }

                        Spacer(Modifier.height(16.dp))

                        // CLOCK SIZE
                        if (config.showClock) {
                            Text("Klockstorlek", style = MaterialTheme.typography.titleMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = config.clockSize == LinearClockPrefs.SIZE_4x1,
                                    onClick = { config = config.copy(clockSize = LinearClockPrefs.SIZE_4x1) }
                                )
                                Text("4x1 (Standard)")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = config.clockSize == LinearClockPrefs.SIZE_4x2,
                                    onClick = { config = config.copy(clockSize = LinearClockPrefs.SIZE_4x2) }
                                )
                                Text("4x2 (Hög)")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = config.clockSize == LinearClockPrefs.SIZE_2x1,
                                    onClick = { config = config.copy(clockSize = LinearClockPrefs.SIZE_2x1) }
                                )
                                Text("2x1 (Kompakt)")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // FÄRGVÄLJARE (Exempel)
                        Text("Bakgrundsfärg", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0xFFFFFFFF.toInt(), 0xFF111827.toInt()).forEach { c ->
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .background(Color(c))
                                        .clickable { config = config.copy(backgroundColor = c) }
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // SPARA-KNAPP
                        Button(
                            onClick = {
                                scope.launch {
                                    val context = this@LinearClockConfigActivity
                                    val manager = GlanceAppWidgetManager(context)
                                    val glanceId = manager.getGlanceIdBy(appWidgetId)

                                    // updateAppWidgetState requires a GlanceId; check and act only if non-null
                                    glanceId?.let { id ->
                                        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                                            prefs.toMutablePreferences().apply {
                                                this[LinearClockPrefs.FONT_FAMILY] = config.font
                                                this[LinearClockPrefs.FONT_SCALE] = config.scale
                                                this[LinearClockPrefs.COLOR_BG] = config.backgroundColor
                                                this[LinearClockPrefs.COLOR_TEXT] = config.textColor
                                                this[LinearClockPrefs.COLOR_ACCENT] = config.accentColor
                                                this[LinearClockPrefs.HOURS_TO_SHOW] = config.hoursToShow

                                                this[LinearClockPrefs.SHOW_CLOCK] = config.showClock
                                                this[LinearClockPrefs.SHOW_EVENTS] = config.showEvents
                                                this[LinearClockPrefs.SHOW_WEATHER] = config.showWeather
                                                this[LinearClockPrefs.SHOW_CLOTHING] = config.showClothing
                                                this[LinearClockPrefs.CLOCK_SIZE] = config.clockSize
                                            }
                                        }
                                        LinearClockWidget.update(context, id)
                                    }

                                    val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                    setResult(RESULT_OK, result)
                                    finish()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Spara") }
                    }
                }
            }
        }
    }
}
