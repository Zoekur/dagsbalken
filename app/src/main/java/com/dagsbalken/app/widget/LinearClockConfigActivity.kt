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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.dagsbalken.app.ui.settings.ThemePreferences
import com.dagsbalken.app.ui.theme.DagsbalkenTheme
import com.dagsbalken.app.ui.theme.ThemeOption
import kotlinx.coroutines.launch

// Datamodell för configen
data class WidgetConfig(
    val font: String = LinearClockPrefs.DEF_FONT,
    val scale: Float = LinearClockPrefs.DEF_SCALE,
    val backgroundColor: Int = LinearClockPrefs.DEF_BG,
    val textColor: Int = LinearClockPrefs.DEF_TEXT,
    val accentColor: Int = LinearClockPrefs.DEF_ACCENT,
    val hoursToShow: Int = LinearClockPrefs.DEF_HOURS_TO_SHOW
)

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
            val themeOption by themePreferences.themeOptionFlow().collectAsState(initial = ThemeOption.NordicCalm)

            // Lokalt state för konfigurationen
            var config by remember { mutableStateOf(WidgetConfig()) }
            val scope = rememberCoroutineScope()

            DagsbalkenTheme(themeOption = themeOption, dynamicColorEnabled = false) {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Widget-inställningar", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))

                        // FÄRGVÄLJARE (Exempel)
                        Text("Bakgrundsfärg")
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

                                    if (glanceId != null) {
                                        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                                            prefs.toMutablePreferences().apply {
                                                this[LinearClockPrefs.FONT_FAMILY] = config.font
                                                this[LinearClockPrefs.FONT_SCALE] = config.scale
                                                this[LinearClockPrefs.COLOR_BG] = config.backgroundColor
                                                this[LinearClockPrefs.COLOR_TEXT] = config.textColor
                                                this[LinearClockPrefs.COLOR_ACCENT] = config.accentColor
                                                this[LinearClockPrefs.HOURS_TO_SHOW] = config.hoursToShow
                                            }
                                        }
                                        LinearClockWidget.update(context, glanceId)
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
