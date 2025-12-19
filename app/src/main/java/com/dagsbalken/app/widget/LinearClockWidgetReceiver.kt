package com.dagsbalken.app.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

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

    // Custom onReceive removed; ACTION_UPDATE_TICK is now handled by LinearClockTickReceiver

    companion object {
        // Delegate to the new internal receiver
        fun scheduleUpdates(context: Context) {
            LinearClockTickReceiver.scheduleUpdates(context)
        }

        fun cancelUpdates(context: Context) {
            LinearClockTickReceiver.cancelUpdates(context)
        }
    }
}
