package com.dagsbalken.app.timers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TIMER_ALARM) return
        val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: return

        TimerNotifications.ensureChannel(context)
        TimerNotifications.showTimerDoneNotification(context, timerId)
    }

    companion object {
        const val ACTION_TIMER_ALARM = "com.dagsbalken.app.action.TIMER_ALARM"
        const val EXTRA_TIMER_ID = "extra_timer_id"
    }
}

