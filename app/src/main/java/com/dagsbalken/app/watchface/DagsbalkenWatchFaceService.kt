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
import java.time.ZonedDateTime

/**
 * Dagsbalken watch face service for Galaxy Watch.
 * Shows a linear 24-hour timeline with events.
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
        
        class DagsbalkenSharedAssets : SharedAssets {
            override fun onDestroy() {}
        }

        override suspend fun createSharedAssets(): DagsbalkenSharedAssets {
            return DagsbalkenSharedAssets()
        }

        override fun render(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: DagsbalkenSharedAssets
        ) {
            // Draw the watch face
            drawWatchFace(canvas, bounds, zonedDateTime)
        }

        override fun renderHighlightLayer(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: DagsbalkenSharedAssets
        ) {
            // No highlight layer needed
        }

        private fun drawWatchFace(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
            val centerX = bounds.exactCenterX()
            val centerY = bounds.exactCenterY()
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()
            
            // Background
            val paint = Paint().apply {
                isAntiAlias = true
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
            paint.color = 0xFF444444.toInt()
            canvas.drawCircle(centerX, centerY, ringRadius, paint)

            // Draw progress arc
            paint.color = 0xFF6200EE.toInt()
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

            // Draw hour markers
            paint.strokeWidth = 3f
            paint.color = 0xFFFFFFFF.toInt()
            for (hour in 0 until 24 step 3) {
                val angle = (hour * 15f - 90f) * Math.PI.toFloat() / 180f
                val innerRadius = ringRadius - ringWidth / 2f - 10f
                val outerRadius = ringRadius - ringWidth / 2f + 10f
                
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
            canvas.drawText(timeText, centerX, centerY + 20f, paint)

            // Draw current time indicator (red dot on the ring)
            val angle = (currentHour * 15f + currentMinute * 0.25f - 90f) * Math.PI.toFloat() / 180f
            val dotX = centerX + ringRadius * kotlin.math.cos(angle)
            val dotY = centerY + ringRadius * kotlin.math.sin(angle)
            
            paint.style = Paint.Style.FILL
            paint.color = 0xFFFF0000.toInt()
            canvas.drawCircle(dotX, dotY, 12f, paint)
        }
    }
}
