package com.dagsbalken.app.service

import android.service.dreams.DreamService
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import com.dagsbalken.app.ui.aod.AodScreen
import com.dagsbalken.app.ui.settings.AppPreferences

class AodDreamService : DreamService(), androidx.lifecycle.LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val internalViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // Implement required ViewModelStoreOwner property
    override val viewModelStore: ViewModelStore
        get() = internalViewModelStore

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Setup interactive mode to allow touch events (for double tap exit)
        isInteractive = true
        // Hide system UI (fullscreen)
        isFullscreen = true

        // Setup Lifecycle owners for Compose
        window.decorView.let { view ->
            view.setViewTreeLifecycleOwner(this)
            view.setViewTreeViewModelStoreOwner(this)
            view.setViewTreeSavedStateRegistryOwner(this)
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val prefs = AppPreferences(applicationContext)

        setContentView(ComposeView(this).apply {
            setContent {
                val color by prefs.aodColor.collectAsState(initial = -65536)
                val opacity by prefs.aodOpacity.collectAsState(initial = 0.5f)
                // Position in percent from top of screen (0-100)
                val positionPercent by prefs.aodPositionPercent.collectAsState(initial = 5f)

                AodScreen(
                    color = color,
                    opacity = opacity,
                    positionPercent = positionPercent,
                    onExit = {
                        finish()
                    }
                )
            }
        })
    }

    override fun onDetachedFromWindow() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onDetachedFromWindow()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        internalViewModelStore.clear()
        super.onDestroy()
    }
}
