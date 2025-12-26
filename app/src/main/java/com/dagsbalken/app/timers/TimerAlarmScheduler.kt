package com.dagsbalken.app.timers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object TimerAlarmScheduler {

    fun schedule(context: Context, timerId: String, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pi = buildPendingIntent(context, timerId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            @Suppress("DEPRECATION")
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    fun cancel(context: Context, timerId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildPendingIntent(context, timerId))
    }

    private fun buildPendingIntent(context: Context, timerId: String): PendingIntent {
        val intent = Intent(context, TimerAlarmReceiver::class.java)
            .setAction(TimerAlarmReceiver.ACTION_TIMER_ALARM)
            .putExtra(TimerAlarmReceiver.EXTRA_TIMER_ID, timerId)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            timerId.hashCode(),
            intent,
            flags
        )
    }
}

