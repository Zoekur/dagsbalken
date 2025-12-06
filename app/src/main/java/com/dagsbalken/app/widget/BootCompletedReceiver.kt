package com.dagsbalken.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dagsbalken.app.workers.WeatherWorker

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WeatherWorker.schedule(context)
        }
    }
}
