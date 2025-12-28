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
 * Internal receiver for handling minute ticks for the Linear Clock Widget.
 * This is NOT exported to prevent external apps from flooding the update mechanism.
 */
class LinearClockTickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
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
        private const val UPDATE_INTERVAL_MILLIS = 60_000L

        fun scheduleUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, LinearClockTickReceiver::class.java).apply {
                action = ACTION_UPDATE_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis(),
                UPDATE_INTERVAL_MILLIS,
                pendingIntent
            )
        }

        fun cancelUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, LinearClockTickReceiver::class.java).apply {
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
