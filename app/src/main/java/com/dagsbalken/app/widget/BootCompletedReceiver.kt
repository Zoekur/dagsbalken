package com.dagsbalken.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.dagsbalken.app.timers.TimerAlarmScheduler
import com.dagsbalken.core.data.TimerRepository
import com.dagsbalken.core.workers.WeatherWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    // Schedule Weather Worker unconditionally (as per original logic)
                    WeatherWorker.schedule(context)

                    // Reschedule timer alarms (active timers) after reboot
                    val timerRepository = TimerRepository(context)
                    val activeTimers = timerRepository.activeTimersFlow
                        .map { it }
                        .firstOrNull()
                        .orEmpty()

                    activeTimers.forEach { timer ->
                        val startedAt = timer.metadata["startedAtEpochMillis"]?.toLongOrNull() ?: return@forEach
                        val startMinutes = timer.startTime.hour * 60 + timer.startTime.minute
                        val endMinutes = timer.endTime.hour * 60 + timer.endTime.minute
                        val totalMinutes = if (endMinutes >= startMinutes) {
                            endMinutes - startMinutes
                        } else {
                            endMinutes + 24 * 60 - startMinutes
                        }.coerceAtLeast(1)

                        val triggerAtMillis = startedAt + (totalMinutes * 60_000L)
                        if (triggerAtMillis > System.currentTimeMillis()) {
                            TimerAlarmScheduler.schedule(context, timer.id, triggerAtMillis)
                        }
                    }

                    // Only schedule widget updates if there are active widgets
                    val manager = GlanceAppWidgetManager(context)
                    val widgetIds = manager.getGlanceIds(LinearClockWidget::class.java)
                    if (widgetIds.isNotEmpty()) {
                        LinearClockWidgetReceiver.scheduleUpdates(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
