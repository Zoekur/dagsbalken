package com.dagsbalken.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Internal receiver for handling clock ticks.
 * This receiver is NOT exported and can only be triggered by the app itself (AlarmManager).
 * This prevents external apps from flooding the widget update mechanism (DoS risk).
 */
class LinearClockTickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Since this receiver is not exported, we trust the intent came from us (AlarmManager).
        // Trigger widget update.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                LinearClockWidget.updateAll(context)
            } catch (e: Exception) {
                android.util.Log.e("LinearClockTickReceiver", "Widget update failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 123
        // Update every minute to keep the clock reasonably accurate
        private const val UPDATE_INTERVAL_MILLIS = 60_000L

        fun scheduleUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // Explicit intent to this class
            val intent = Intent(context, LinearClockTickReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use setRepeating for efficiency.
            alarmManager.setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis(),
                UPDATE_INTERVAL_MILLIS,
                pendingIntent
            )
        }

        fun cancelUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, LinearClockTickReceiver::class.java)
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
