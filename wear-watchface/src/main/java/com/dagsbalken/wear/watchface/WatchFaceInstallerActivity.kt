package com.dagsbalken.wear.watchface

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.wallpaper.WallpaperService
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

/**
 * Minimal launcher activity for the Wear watchface module.
 *
 * Purpose:
 * - Makes the APK installable/runnable via Android Studio (avoids "Default Activity not found").
 * - Provides a simple path for the user to open watch face settings and select this watch face.
 */
class WatchFaceInstallerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val padding = (resources.displayMetrics.density * 20).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val text = TextView(this).apply {
            setText(R.string.installer_message)
            gravity = Gravity.CENTER
        }

        val openWatchFaces = Button(this).apply {
            setText(R.string.open_watch_faces)
            setOnClickListener {
                val opened = tryOpenWatchFacePicker()
                if (!opened) {
                    openAppDetails()
                }
            }
        }

        val appInfo = Button(this).apply {
            setText(R.string.app_info)
            setOnClickListener { openAppDetails() }
        }

        root.addView(text)
        root.addView(openWatchFaces)
        root.addView(appInfo)
        setContentView(root)
    }

    private fun tryOpenWatchFacePicker(): Boolean {
        // NOTE: Some wallpaper/watchface related actions are not exposed as Intent constants.
        // Use action strings directly for widest compatibility.

        val candidates = listOf(
            // Standard wallpaper pickers
            Intent(Intent.ACTION_SET_WALLPAPER),

            // Live wallpaper related actions (strings are stable even if constants are missing)
            Intent("android.service.wallpaper.LIVE_WALLPAPER_CHOOSER"),
            Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER"),

            // Some devices react to starting the wallpaper service interface.
            Intent(WallpaperService.SERVICE_INTERFACE),

            // Last resort: return to home so the user can long-press and pick watch face manually.
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        )

        for (intent in candidates) {
            val ok = runCatching { startActivity(intent) }.isSuccess
            if (ok) return true
        }

        return false
    }

    private fun openAppDetails() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}
