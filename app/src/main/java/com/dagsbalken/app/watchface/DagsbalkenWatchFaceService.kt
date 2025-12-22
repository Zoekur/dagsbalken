package com.dagsbalken.app.watchface

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.dagsbalken.app.R
import com.dagsbalken.core.data.CalendarRepository
import com.dagsbalken.core.data.DayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dagsbalken watch face service for Galaxy Watch.
 * Shows a horizontal curved timeline with day/night gradient and events.
 */
class DagsbalkenWatchFaceService : WatchFaceService() {

    companion object {
        // User style setting IDs
        const val THEME_STYLE_SETTING = "theme_setting"
        const val AOD_POSITION_SETTING = "aod_position_setting"
        const val SHOW_EVENTS_SETTING = "show_events_setting"
        
        // Theme options
        const val THEME_COLD = "cold"
        const val THEME_WARM = "warm"
        const val THEME_COLD_HC = "cold_hc"
        const val THEME_WARM_HC = "warm_hc"
        
        // AOD position options
        const val AOD_POS_TOP = "top"
        const val AOD_POS_MIDDLE = "middle"
        const val AOD_POS_BOTTOM = "bottom"
    }

    override fun createUserStyleSchema(): UserStyleSchema {
        // Theme setting
        val themeSetting = UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id(THEME_STYLE_SETTING),
            resources,
            R.string.theme_setting_name,
            R.string.theme_setting_description,
            null,
            listOf(
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(THEME_COLD),
                    resources,
                    R.string.theme_cold,
                    R.string.theme_cold,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(THEME_WARM),
                    resources,
                    R.string.theme_warm,
                    R.string.theme_warm,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(THEME_COLD_HC),
                    resources,
                    R.string.theme_cold_hc,
                    R.string.theme_cold_hc,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(THEME_WARM_HC),
                    resources,
                    R.string.theme_warm_hc,
                    R.string.theme_warm_hc,
                    null
                )
            ),
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS
        )
        
        // AOD Position setting
        val aodPositionSetting = UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id(AOD_POSITION_SETTING),
            resources,
            R.string.aod_position_setting_name,
            R.string.aod_position_setting_description,
            null,
            listOf(
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(AOD_POS_TOP),
                    resources,
                    R.string.aod_position_top,
                    R.string.aod_position_top,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(AOD_POS_MIDDLE),
                    resources,
                    R.string.aod_position_middle,
                    R.string.aod_position_middle,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(AOD_POS_BOTTOM),
                    resources,
                    R.string.aod_position_bottom,
                    R.string.aod_position_bottom,
                    null
                )
            ),
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS
        )
        
        // Show events toggle
        val showEventsSetting = UserStyleSetting.BooleanUserStyleSetting(
            UserStyleSetting.Id(SHOW_EVENTS_SETTING),
            resources,
            R.string.show_events_setting_name,
            R.string.show_events_setting_description,
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            true
        )
        
        return UserStyleSchema(listOf(themeSetting, aodPositionSetting, showEventsSetting))
    }

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        // Create the renderer
        val renderer = DagsbalkenRenderer(
            context = this,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            userStyleRepository = currentUserStyleRepository
        )

        // Create the watch face
        return WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,
            renderer = renderer
        )
    }

    private class DagsbalkenRenderer(
        private val context: WatchFaceService,
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        private val userStyleRepository: CurrentUserStyleRepository
    ) : Renderer.CanvasRenderer2<DagsbalkenRenderer.DagsbalkenSharedAssets>(
        surfaceHolder = surfaceHolder,
        currentUserStyleRepository = userStyleRepository,
        watchState = watchState,
        canvasType = CanvasType.HARDWARE,
        interactiveDrawModeUpdateDelayMillis = 60_000L, // Update every minute
        clearWithBackgroundTintBeforeRenderingHighlightLayer = false
    ) {
        
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val events = AtomicReference<List<DayEvent>>(emptyList())
        private var lastEventReloadHour = -1
        
        class DagsbalkenSharedAssets : SharedAssets {
            override fun onDestroy() {}
        }

        override suspend fun createSharedAssets(): DagsbalkenSharedAssets {
            // Load calendar events
            loadEvents()
            return DagsbalkenSharedAssets()
        }

        private fun loadEvents() {
            scope.launch(Dispatchers.IO) {
                try {
                    val calendarRepo = CalendarRepository(context)
                    val loadedEvents = calendarRepo.getEventsForToday()
                    events.set(loadedEvents)
                } catch (e: Exception) {
                    // Handle permission issues gracefully
                    events.set(emptyList())
                }
            }
        }

        override fun render(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: DagsbalkenSharedAssets
        ) {
            // Reload events once per hour (throttled)
            val currentHour = zonedDateTime.hour
            if (currentHour != lastEventReloadHour) {
                lastEventReloadHour = currentHour
                loadEvents()
            }
            
            // Draw the watch face
            val isAmbient = renderParameters.drawMode == androidx.wear.watchface.DrawMode.AMBIENT
            drawWatchFace(canvas, bounds, zonedDateTime, isAmbient)
        }

        override fun renderHighlightLayer(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: DagsbalkenSharedAssets
        ) {
            // No highlight layer needed
        }

        private fun drawWatchFace(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, isAmbient: Boolean) {
            val centerX = bounds.exactCenterX()
            val centerY = bounds.exactCenterY()
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()
            
            // Get user style settings
            val selectedTheme = when (userStyleRepository.userStyle.value[
                UserStyleSetting.Id(THEME_STYLE_SETTING)
            ]?.toString()) {
                THEME_WARM -> ThemeColors.Warm
                THEME_COLD_HC -> ThemeColors.ColdHighContrast
                THEME_WARM_HC -> ThemeColors.WarmHighContrast
                else -> ThemeColors.Cold
            }
            
            val aodPosition = when (userStyleRepository.userStyle.value[
                UserStyleSetting.Id(AOD_POSITION_SETTING)
            ]?.toString()) {
                AOD_POS_TOP -> 0.3f
                AOD_POS_BOTTOM -> 0.7f
                else -> 0.5f // Middle
            }
            
            val showEvents = userStyleRepository.userStyle.value[
                UserStyleSetting.Id(SHOW_EVENTS_SETTING)
            ]?.toString() != "false"
            
            // Background
            val paint = Paint().apply {
                isAntiAlias = !isAmbient
                color = if (isAmbient) 0xFF000000.toInt() else selectedTheme.background
            }
            canvas.drawRect(bounds, paint)

            // Calculate time progress
            val currentHour = zonedDateTime.hour
            val currentMinute = zonedDateTime.minute
            val currentMinutes = currentHour * 60 + currentMinute
            val totalMinutes = 24 * 60
            val progress = currentMinutes.toFloat() / totalMinutes

            if (isAmbient) {
                // Simplified AOD mode - just time display
                drawAmbientMode(canvas, bounds, zonedDateTime, selectedTheme, aodPosition, paint)
            } else {
                // Full interactive mode - timeline with gradient and events
                drawInteractiveMode(canvas, bounds, zonedDateTime, selectedTheme, showEvents, paint)
            }
        }
        
        private fun drawAmbientMode(
            canvas: Canvas, 
            bounds: Rect, 
            zonedDateTime: ZonedDateTime,
            theme: ThemeColors,
            verticalPosition: Float,
            paint: Paint
        ) {
            val centerX = bounds.exactCenterX()
            val centerY = bounds.height() * verticalPosition
            
            // Draw simple timeline bar at bottom
            val barHeight = 40f
            val barTop = bounds.height() * 0.8f
            val barBottom = barTop + barHeight
            
            paint.color = 0xFF333333.toInt()
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, barTop, bounds.width().toFloat(), barBottom, paint)
            
            // Draw progress on timeline
            val currentMinutes = zonedDateTime.hour * 60 + zonedDateTime.minute
            val progress = currentMinutes.toFloat() / (24 * 60)
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawRect(0f, barTop, bounds.width() * progress, barBottom, paint)
            
            // Draw current time indicator line
            val currentX = bounds.width() * progress
            paint.strokeWidth = 4f
            canvas.drawLine(currentX, barTop - 20f, currentX, barBottom, paint)
            
            // Draw digital time in center
            paint.style = Paint.Style.FILL
            paint.textSize = 56f
            paint.textAlign = Paint.Align.CENTER
            paint.color = 0xFFFFFFFF.toInt()
            
            val timeText = String.format(Locale.getDefault(), "%02d:%02d", 
                zonedDateTime.hour, zonedDateTime.minute)
            canvas.drawText(timeText, centerX, centerY, paint)
        }
        
        private fun drawInteractiveMode(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            theme: ThemeColors,
            showEvents: Boolean,
            paint: Paint
        ) {
            val centerX = bounds.exactCenterX()
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()
            
            // Timeline bar parameters
            val timelineHeight = 100f
            val timelineTop = height * 0.65f
            val timelineBottom = timelineTop + timelineHeight
            
            val currentMinutes = zonedDateTime.hour * 60 + zonedDateTime.minute
            val totalMinutes = 24 * 60
            val progress = currentMinutes.toFloat() / totalMinutes
            
            // Draw day/night gradient background on timeline
            paint.shader = LinearGradient(
                0f, 0f, width, 0f,
                intArrayOf(
                    theme.nightColor,
                    theme.dayColor,
                    theme.nightColor
                ),
                floatArrayOf(0f, 0.5f, 1.0f),
                Shader.TileMode.CLAMP
            )
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, timelineTop, width, timelineBottom, paint)
            paint.shader = null
            
            // Draw "passed time" overlay (semi-transparent gray)
            paint.color = 0x88333333.toInt()
            canvas.drawRect(0f, timelineTop, width * progress, timelineBottom, paint)
            
            // Draw calendar events
            if (showEvents) {
                val currentEvents = events.get()
                currentEvents.forEach { event ->
                    val eventStartMinutes = event.start.hour * 60 + event.start.minute
                    val eventEnd = event.end
                    val eventEndMinutes = if (eventEnd != null) {
                        eventEnd.hour * 60 + eventEnd.minute
                    } else {
                        eventStartMinutes + 60
                    }
                    
                    val startX = (eventStartMinutes.toFloat() / totalMinutes) * width
                    val endX = (eventEndMinutes.toFloat() / totalMinutes) * width
                    
                    paint.color = event.color
                    paint.alpha = 200
                    paint.style = Paint.Style.FILL
                    
                    // Draw event block with padding
                    val eventTop = timelineTop + timelineHeight * 0.15f
                    val eventBottom = timelineBottom - timelineHeight * 0.15f
                    canvas.drawRect(startX, eventTop, endX, eventBottom, paint)
                    paint.alpha = 255
                }
            }
            
            // Draw hour markers
            paint.color = 0xFFFFFFFF.toInt()
            paint.strokeWidth = 2f
            paint.alpha = 150
            for (hour in 0..23 step 3) {
                val x = (hour * 60f / totalMinutes) * width
                canvas.drawLine(x, timelineTop, x, timelineTop + 20f, paint)
                canvas.drawLine(x, timelineBottom - 20f, x, timelineBottom, paint)
            }
            paint.alpha = 255
            
            // Draw current time indicator (red line)
            val currentX = width * progress
            paint.color = theme.accentColor
            paint.strokeWidth = 6f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(currentX, timelineTop - 30f, currentX, timelineBottom + 10f, paint)
            
            // Draw current time dot above timeline
            paint.style = Paint.Style.FILL
            canvas.drawCircle(currentX, timelineTop - 50f, 16f, paint)
            
            // Draw digital time above timeline
            paint.color = 0xFFFFFFFF.toInt()
            paint.textSize = 64f
            paint.textAlign = Paint.Align.CENTER
            paint.style = Paint.Style.FILL
            
            val timeText = String.format(Locale.getDefault(), "%02d:%02d", 
                zonedDateTime.hour, zonedDateTime.minute)
            canvas.drawText(timeText, centerX, height * 0.45f, paint)
            
            // Draw date below time
            paint.textSize = 24f
            val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
            val dateText = String.format(
                Locale.getDefault(),
                "%s %d",
                zonedDateTime.format(monthFormatter),
                zonedDateTime.dayOfMonth
            )
            canvas.drawText(dateText, centerX, height * 0.45f + 35f, paint)
            
            // Draw sunrise/sunset indicators if in typical time ranges
            drawSunMoonIndicators(canvas, width, timelineTop, timelineBottom, totalMinutes, paint)
        }
        
        private fun drawSunMoonIndicators(
            canvas: Canvas,
            width: Float,
            timelineTop: Float,
            timelineBottom: Float,
            totalMinutes: Int,
            paint: Paint
        ) {
            // Sunrise around 08:00 (480 minutes)
            val sunriseMinutes = 480f
            val sunriseX = (sunriseMinutes / totalMinutes) * width
            
            // Sunset around 20:00 (1200 minutes)
            val sunsetMinutes = 1200f
            val sunsetX = (sunsetMinutes / totalMinutes) * width
            
            val indicatorY = timelineTop - 10f
            val indicatorRadius = 12f
            
            // Sunrise (yellow/orange circle)
            paint.color = 0xFFFFB300.toInt()
            paint.style = Paint.Style.FILL
            canvas.drawCircle(sunriseX, indicatorY, indicatorRadius, paint)
            
            // Sunset (orange/red circle)
            paint.color = 0xFFFF6D00.toInt()
            canvas.drawCircle(sunsetX, indicatorY, indicatorRadius, paint)
        }
        
        // Theme colors data class
        data class ThemeColors(
            val background: Int,
            val nightColor: Int,
            val dayColor: Int,
            val accentColor: Int
        ) {
            companion object {
                val Cold = ThemeColors(
                    background = 0xFF000000.toInt(),
                    nightColor = 0xFF1A237E.toInt(), // Deep Blue
                    dayColor = 0xFF4FC3F7.toInt(),   // Light Blue
                    accentColor = 0xFFFF0000.toInt()
                )
                
                val Warm = ThemeColors(
                    background = 0xFF000000.toInt(),
                    nightColor = 0xFFBF360C.toInt(), // Deep Orange
                    dayColor = 0xFFFFEB3B.toInt(),   // Yellow
                    accentColor = 0xFFFF0000.toInt()
                )
                
                val ColdHighContrast = ThemeColors(
                    background = 0xFF000000.toInt(),
                    nightColor = 0xFF000000.toInt(),
                    dayColor = 0xFF00FFFF.toInt(),   // Cyan
                    accentColor = 0xFFFFFF00.toInt() // Yellow
                )
                
                val WarmHighContrast = ThemeColors(
                    background = 0xFF000000.toInt(),
                    nightColor = 0xFF000000.toInt(),
                    dayColor = 0xFFFFFF00.toInt(),   // Yellow
                    accentColor = 0xFFFF6D00.toInt() // Orange
                )
            }
        }
    }
}
