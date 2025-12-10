package com.dagsbalken.app.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.dagsbalken.core.data.WeatherData

@Composable
fun WeatherCard(
    weatherData: WeatherData?,
    modifier: GlanceModifier,
    showClothingIcon: Boolean,
    title: String? = null
) {
    Box(
        modifier
            .fillMaxHeight()
            .cornerRadius(16.dp)
            .padding(2.dp)
            //.background(Color.White) // Glance doesn't support background color directly on Box like this easily without drawable usually, but ImageProvider or built-in style.
            // Assuming the container app handles theming or we use a drawable if needed.
            // For now rely on transparent/default look or add background image.
    ) {
        Row(
            GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (weatherData != null && weatherData.isDataLoaded) {
                if (title != null) {
                    // Optional title if space permits, but usually widget is minimal.
                    // Text(title, style = TextStyle(fontWeight = FontWeight.Bold))
                    // Spacer(GlanceModifier.width(8.dp))
                }

                if (!showClothingIcon) {
                     // Standard Weather: Temp + Condition Icon (if available separately, else adviceIcon)
                     // Using adviceIcon as placeholder for condition if real condition icon isn't separate.
                     Text(
                        text = "${weatherData.temperatureCelsius}°",
                        style = TextStyle(fontSize = TextUnit(48.dp.value, TextUnitType.Sp), fontWeight = FontWeight.Bold)
                    )
                    Spacer(GlanceModifier.width(24.dp))
                }

                // Icon
                // If showClothingIcon is true, we ideally want the clothing advice icon.
                // If false (Weather), we want weather condition (Sun/Cloud).
                // The current Repository seems to provide `adviceIcon` which might be the only visual we have.
                Text(
                    text = weatherData.adviceIcon,
                    style = TextStyle(fontSize = TextUnit(32.dp.value, TextUnitType.Sp))
                )
            } else {
                Text("Laddar...", style = TextStyle(fontSize = TextUnit(16.dp.value, TextUnitType.Sp)))
            }
        }
    }
}
