package com.dagsbalken.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.dagsbalken.core.data.DayEvent
import com.dagsbalken.core.widget.LinearClockPrefs
import com.dagsbalken.core.widget.WidgetConfig
import java.time.LocalTime
import kotlin.math.max

object LinearClockBitmapGenerator {

    fun generate(
        context: Context,
        width: Int,
        height: Int,
        events: List<DayEvent>,
        config: WidgetConfig,
        currentTime: LocalTime = LocalTime.now()
    ): Bitmap {
        // Ensure valid dimensions to prevent crashes and division by zero
        val safeWidth = max(width, 1)
        val safeHeight = max(height, 1)

        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Colors from config
        val colorFuture = config.backgroundColor
        val colorPassed = 0xFFE5E7EB.toInt() // Light gray for passed
        val colorRedLine = config.accentColor
        val colorBorder = config.textColor

        // Paint setup (Allocating here is necessary as Paint is not thread-safe)
        val paint = Paint().apply {
            isAntiAlias = true
        }

        // 1. Draw Background (Future - Right Side)
        paint.color = colorFuture
        canvas.drawRect(0f, 0f, safeWidth.toFloat(), safeHeight.toFloat(), paint)

        // Adjust scale/density based on size mode
        val densityMultiplier = when (config.clockSize) {
            LinearClockPrefs.SIZE_4x2 -> 2.0f
            LinearClockPrefs.SIZE_2x1 -> 0.8f
            else -> 1.0f
        }

        // Time Window Logic
        val totalWindowHours = config.hoursToShow.coerceIn(4, 24)
        val windowDurationMinutes = totalWindowHours * 60
        // Prevent division by zero
        val minutesPerPixel = windowDurationMinutes.toFloat() / safeWidth.toFloat()

        val currentMinuteOfDay = currentTime.hour * 60 + currentTime.minute

        val windowStartMinute: Int = if (totalWindowHours == 24) {
             0 // Fixed 00:00 to 24:00
        } else {
             // Centered around now
             currentMinuteOfDay - (windowDurationMinutes / 2)
        }
        val windowEndMinute = windowStartMinute + windowDurationMinutes

        // 2. Draw Passed Time (Gray overlay)
        val currentX = (currentMinuteOfDay - windowStartMinute) / minutesPerPixel
        if (currentX > 0) {
             paint.color = colorPassed
             val passedWidth = currentX.coerceAtMost(safeWidth.toFloat())
             canvas.drawRect(0f, 0f, passedWidth, safeHeight.toFloat(), paint)
        }

        // 3. Draw Events
        events.forEach { event ->
            val startMin = event.start.hour * 60 + event.start.minute
            val endMin = (event.end?.hour ?: 0) * 60 + (event.end?.minute ?: 0)
            val actualEndMin = if (event.end != null && endMin > startMin) endMin else startMin + 60

            val eventStartPx = (startMin - windowStartMinute) / minutesPerPixel
            val eventWidthPx = (actualEndMin - startMin) / minutesPerPixel

            if (eventStartPx + eventWidthPx > 0 && eventStartPx < safeWidth) {
                paint.color = event.color
                paint.alpha = 150 // Slightly transparent

                val left = eventStartPx.coerceAtLeast(0f)
                val right = (eventStartPx + eventWidthPx).coerceAtMost(safeWidth.toFloat())

                // Draw bar. Adjust height based on available height.
                canvas.drawRect(left, safeHeight * 0.2f, right, safeHeight * 0.8f, paint)

                paint.alpha = 255
            }
        }

        // 4. Draw Hour Ticks
        paint.color = colorBorder
        paint.strokeWidth = 2f * if(densityMultiplier > 1.5) 1.5f else 1f
        paint.textSize = 24f * config.scale * densityMultiplier

        try {
             paint.typeface = Typeface.create(config.font, Typeface.BOLD)
        } catch (e: Exception) {
             paint.typeface = Typeface.DEFAULT_BOLD
        }
        paint.textAlign = Paint.Align.CENTER

        // Find first visible hour
        val firstHour = (windowStartMinute / 60) - 1
        val lastHour = (windowEndMinute / 60) + 1

        for (h in firstHour..lastHour) {
            val hourMin = h * 60
            val x = (hourMin - windowStartMinute) / minutesPerPixel

            if (x >= 0 && x <= safeWidth) {
                val tickHeight = safeHeight * 0.3f
                canvas.drawLine(x, 0f, x, tickHeight, paint)
            }
        }

        // 5. Draw Red Line (Current Time)
        if (currentX >= 0 && currentX <= safeWidth) {
            paint.color = colorRedLine
            paint.strokeWidth = 4f * if(densityMultiplier > 1.5) 1.5f else 1f
            canvas.drawLine(currentX, 0f, currentX, safeHeight.toFloat(), paint)
        }

        // 6. Border
        paint.color = colorBorder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawRect(0f, 0f, safeWidth.toFloat(), safeHeight.toFloat(), paint)
        paint.style = Paint.Style.FILL // Reset

        return bitmap
    }
}
