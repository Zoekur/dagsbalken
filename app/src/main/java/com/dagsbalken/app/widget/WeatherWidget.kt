package com.dagsbalken.app.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.fillMaxSize
import com.dagsbalken.core.data.WeatherRepository
import com.dagsbalken.app.widget.WeatherCard

object WeatherWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weatherRepo = WeatherRepository(context)

        provideContent {
            val weatherData by weatherRepo.weatherDataFlow.collectAsState(initial = null)

            WeatherCard(
                weatherData = weatherData,
                modifier = GlanceModifier.fillMaxSize(),
                showClothingIcon = false
            )
        }
    }
}
