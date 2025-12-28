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
        LinearClockTickReceiver.scheduleUpdates(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        LinearClockTickReceiver.cancelUpdates(context)
        // Attempt to cancel legacy updates if any exist
        cancelLegacyUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Migration: If we receive the legacy ACTION_UPDATE_TICK (from an old alarm that wasn't cancelled),
        // we should migrate the user to the new non-exported receiver system immediately.
        if (intent.action == ACTION_UPDATE_TICK) {
            // 1. Schedule the new secure updates
            LinearClockTickReceiver.scheduleUpdates(context)
            // 2. Cancel the old insecure updates so this receiver isn't triggered again
            cancelLegacyUpdates(context)
            // 3. Perform the update one last time to ensure continuity
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

        // Kept for legacy cleanup
        fun cancelLegacyUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, LinearClockWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
