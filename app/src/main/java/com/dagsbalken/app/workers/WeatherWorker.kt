package com.dagsbalken.app.workers

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dagsbalken.app.data.WeatherRepository
import com.dagsbalken.app.widget.LinearClockWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WeatherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val repository = WeatherRepository(context)

            // Fetch settings to determine location source
            val settings = repository.locationSettingsFlow.first()

            // Mock weather fetching logic
            val temp: Int
            val precipChance: Int

            if (settings.useCurrentLocation) {
                // Simulating GPS location weather (random but consistent for "local")
                temp = (-5..25).random()
                precipChance = (0..50).random()
            } else {
                 // Simulating Manual location weather
                 if (settings.manualLocationName.isNotBlank()) {
                     // Generate "deterministic" random based on name length to simulate different weather for different cities
                     val seed = settings.manualLocationName.length
                     temp = (seed % 30)
                     precipChance = (seed * 10 % 100)
                 } else {
                     temp = 20
                     precipChance = 0
                 }
            }

            repository.saveWeatherData(temp, precipChance)

            // Update all widgets
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(LinearClockWidget::class.java)

            glanceIds.forEach { glanceId ->
                // Trigger widget update.
                LinearClockWidget.update(context, glanceId)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "weather_worker_periodic"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WeatherWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
