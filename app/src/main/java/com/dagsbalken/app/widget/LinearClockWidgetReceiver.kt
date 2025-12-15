package com.dagsbalken.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LinearClockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LinearClockWidget

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleUpdates(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_TICK) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    LinearClockWidget.updateAll(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val ACTION_UPDATE_TICK = "com.dagsbalken.app.widget.ACTION_UPDATE_TICK"
        private const val REQUEST_CODE = 123
        // Update every minute to keep the clock reasonably accurate
        private const val UPDATE_INTERVAL_MILLIS = 60_000L

        fun scheduleUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, LinearClockWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use setRepeating for efficiency. It's inexact on modern Android but ensures
            // the widget updates roughly every minute without waking the device constantly.
            // Using RTC so it relates to wall clock time.
            alarmManager.setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis(),
                UPDATE_INTERVAL_MILLIS,
                pendingIntent
            )
        }

        fun cancelUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, LinearClockWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
