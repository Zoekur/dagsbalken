package com.dagsbalken.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.dagsbalken.core.workers.WeatherWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    // Schedule Weather Worker unconditionally (as per original logic)
                    WeatherWorker.schedule(context)

                    // Only schedule widget updates if there are active widgets
                    val manager = GlanceAppWidgetManager(context)
                    val widgetIds = manager.getGlanceIds(LinearClockWidget::class.java)
                    if (widgetIds.isNotEmpty()) {
                        LinearClockTickReceiver.scheduleUpdates(context)
                        // Cleanup legacy alarms on boot
                        LinearClockWidgetReceiver.cancelLegacyUpdates(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
