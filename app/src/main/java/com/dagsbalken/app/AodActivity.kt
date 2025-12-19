package com.dagsbalken.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dagsbalken.app.ui.aod.AodScreen
import com.dagsbalken.app.ui.settings.AppPreferences

class AodActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Hide system UI for immersive mode
        // Note: EdgeToEdge is not enabled in this project as per memories, but for AOD we want full black
        // However, standard WindowManager flags can help.

        val prefs = AppPreferences(this)

        setContent {
            val color by prefs.aodColor.collectAsState(initial = -65536)
            val opacity by prefs.aodOpacity.collectAsState(initial = 0.5f)

            AodScreen(
                color = color,
                opacity = opacity,
                onExit = {
                    finish()
                }
            )
        }
    }
}
