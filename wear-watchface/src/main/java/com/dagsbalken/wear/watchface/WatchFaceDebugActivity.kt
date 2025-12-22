package com.dagsbalken.wear.watchface

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.ComponentActivity

/**
 * Debug screen that shows some basic info so we can confirm the watch-face APK is installed
 * and that this module is the one running on the emulator.
 */
class WatchFaceDebugActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasWatchFeature = packageManager.hasSystemFeature("android.hardware.type.watch")
        val items = listOf(
            "package=$packageName",
            "service=${DagsbalkenWatchFaceService::class.java.name}",
            "hasWatchFeature=$hasWatchFeature"
        )

        val listView = ListView(this)
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        setContentView(listView)
    }
}
