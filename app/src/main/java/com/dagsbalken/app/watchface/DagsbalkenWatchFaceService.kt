package com.dagsbalken.app.watchface

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import com.dagsbalken.core.data.CalendarRepository
import com.dagsbalken.core.data.DayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

/**
 * Dagsbalken watch face service for Galaxy Watch.
 * Shows a circular 24-hour timeline with events.
 */
class DagsbalkenWatchFaceService : WatchFaceService() {

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
            currentUserStyleRepository = currentUserStyleRepository
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
        currentUserStyleRepository: CurrentUserStyleRepository
    ) : Renderer.CanvasRenderer2<DagsbalkenRenderer.DagsbalkenSharedAssets>(
        surfaceHolder = surfaceHolder,
        currentUserStyleRepository = currentUserStyleRepository,
        watchState = watchState,
        canvasType = CanvasType.HARDWARE,
        interactiveDrawModeUpdateDelayMillis = 60_000L, // Update every minute
        clearWithBackgroundTintBeforeRenderingHighlightLayer = false
    ) {
        
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private var events: List<DayEvent> = emptyList()
        
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
                    events = calendarRepo.getEventsForToday()
                } catch (e: Exception) {
                    // Handle permission issues gracefully
                    events = emptyList()
                }
            }
        }

        override fun render(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: DagsbalkenSharedAssets
        ) {
            // Reload events every hour
            if (zonedDateTime.minute == 0) {
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
            
            // Background
            val paint = Paint().apply {
                isAntiAlias = !isAmbient // Disable anti-aliasing in ambient mode for battery saving
                color = 0xFF000000.toInt()
            }
            canvas.drawRect(bounds, paint)

            // Calculate time progress
            val currentHour = zonedDateTime.hour
            val currentMinute = zonedDateTime.minute
            val currentMinutes = currentHour * 60 + currentMinute
            val totalMinutes = 24 * 60
            val progress = currentMinutes.toFloat() / totalMinutes

            // Draw circular timeline (outer ring)
            val ringRadius = minOf(width, height) / 2f * 0.85f
            val ringWidth = 20f
            
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = ringWidth
            paint.color = if (isAmbient) 0xFF666666.toInt() else 0xFF444444.toInt()
            canvas.drawCircle(centerX, centerY, ringRadius, paint)

            // Draw progress arc (representing elapsed time today)
            // In ambient mode, use white/gray color scheme
            paint.color = if (isAmbient) 0xFFFFFFFF.toInt() else 0xFF6200EE.toInt()
            paint.strokeCap = Paint.Cap.ROUND
            val sweepAngle = 360f * progress
            canvas.drawArc(
                centerX - ringRadius,
                centerY - ringRadius,
                centerX + ringRadius,
                centerY + ringRadius,
                -90f, // Start at top (12 o'clock)
                sweepAngle,
                false,
                paint
            )

            // Draw calendar events only in interactive mode (not in ambient)
            if (!isAmbient) {
                paint.strokeCap = Paint.Cap.BUTT
                events.forEach { event ->
                    val eventStartMinutes = event.start.hour * 60 + event.start.minute
                    val eventEnd = event.end
                    val eventEndMinutes = if (eventEnd != null) {
                        eventEnd.hour * 60 + eventEnd.minute
                    } else {
                        eventStartMinutes + 60 // Default 1 hour if no end time
                    }
                    
                    val startAngle = (eventStartMinutes.toFloat() / totalMinutes * 360f) - 90f
                    val eventSweep = ((eventEndMinutes - eventStartMinutes).toFloat() / totalMinutes * 360f)
                    
                    paint.color = event.color
                    paint.alpha = 180
                    paint.strokeWidth = ringWidth * 0.6f
                    canvas.drawArc(
                        centerX - ringRadius,
                        centerY - ringRadius,
                        centerX + ringRadius,
                        centerY + ringRadius,
                        startAngle,
                        eventSweep,
                        false,
                        paint
                    )
                    paint.alpha = 255
                }
            }

            // Draw hour markers (12, 6, 9, 15, 18, 21)
            paint.strokeWidth = 3f
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = 0xFFFFFFFF.toInt()
            for (hour in listOf(0, 6, 9, 12, 15, 18, 21)) {
                val angle = (hour * 15f - 90f) * Math.PI.toFloat() / 180f
                val innerRadius = ringRadius - ringWidth / 2f - 15f
                val outerRadius = ringRadius - ringWidth / 2f + 15f
                
                val x1 = centerX + innerRadius * kotlin.math.cos(angle)
                val y1 = centerY + innerRadius * kotlin.math.sin(angle)
                val x2 = centerX + outerRadius * kotlin.math.cos(angle)
                val y2 = centerY + outerRadius * kotlin.math.sin(angle)
                
                canvas.drawLine(x1, y1, x2, y2, paint)
            }

            // Draw digital time in center
            paint.style = Paint.Style.FILL
            paint.textSize = 60f
            paint.textAlign = Paint.Align.CENTER
            paint.color = 0xFFFFFFFF.toInt()
            
            val timeText = String.format("%02d:%02d", currentHour, currentMinute)
            canvas.drawText(timeText, centerX, centerY, paint)
            
            // Draw date below time (only in interactive mode)
            if (!isAmbient) {
                paint.textSize = 24f
                val dateText = String.format(
                    "%s %d",
                    zonedDateTime.month.toString().substring(0, 3).lowercase().replaceFirstChar { it.uppercase() },
                    zonedDateTime.dayOfMonth
                )
                canvas.drawText(dateText, centerX, centerY + 35f, paint)
            }

            // Draw current time indicator (red/white dot on the ring)
            if (!isAmbient) {
                val angle = (currentHour * 15f + currentMinute * 0.25f - 90f) * Math.PI.toFloat() / 180f
                val dotX = centerX + ringRadius * kotlin.math.cos(angle)
                val dotY = centerY + ringRadius * kotlin.math.sin(angle)
                
                paint.style = Paint.Style.FILL
                paint.color = 0xFFFF0000.toInt()
                canvas.drawCircle(dotX, dotY, 12f, paint)
            }
        }
    }
}
