package com.dagsbalken.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.dagsbalken.app.data.DayEvent
import java.time.LocalTime

object LinearClockBitmapGenerator {

    fun generate(
        context: Context,
        width: Int,
        height: Int,
        events: List<DayEvent>,
        currentTime: LocalTime = LocalTime.now()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Colors
        val colorFuture = 0xFFFFFFFF.toInt() // White
        val colorPassed = 0xFFB7EA27.toInt() // Green
        val colorRedLine = 0xFFEF4444.toInt() // Red
        val colorBorder = 0xFF000000.toInt() // Black

        // Paint setup
        val paint = Paint().apply {
            isAntiAlias = true
        }

        // 1. Draw Background (Future - Right Side)
        paint.color = colorFuture
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 2. Draw Passed Time (Green - Left Side)
        // Since the widget is zoomed and centered on 'now', everything to the left is passed.
        paint.color = colorPassed
        canvas.drawRect(0f, 0f, width / 2f, height.toFloat(), paint)

        // Time Window Logic
        // The widget shows 2 hours before and 2 hours after 'now'
        val zoomHours = 2
        val windowDurationMinutes = zoomHours * 2 * 60 // 240 minutes
        val minutesPerPixel = windowDurationMinutes.toFloat() / width

        // Window Range (in minutes from start of day)
        val currentMinuteOfDay = currentTime.hour * 60 + currentTime.minute
        val windowStartMinute = currentMinuteOfDay - (windowDurationMinutes / 2)
        val windowEndMinute = windowStartMinute + windowDurationMinutes

        // 3. Draw Events (Removed as per request)

        // 4. Draw Hour Ticks and Text
        paint.color = colorBorder
        paint.strokeWidth = 2f
        paint.textSize = 24f // Fixed size for widget text
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER

        // Find first visible hour
        // windowStartMinute might be negative (e.g. 23:00 yesterday), so we floor div correctly
        val firstHour = (windowStartMinute / 60) - 1
        val lastHour = (windowEndMinute / 60) + 1

        for (h in firstHour..lastHour) {
            val hourMin = h * 60
            val x = (hourMin - windowStartMinute) / minutesPerPixel

            if (x >= 0 && x <= width) {
                // Draw Tick
                canvas.drawLine(x, 0f, x, height * 0.3f, paint)

                // Draw Text
                // Normalize negative hours or hours > 24 to 0-23 range
                val hourText = "${(h % 24 + 24) % 24}"
                canvas.drawText(hourText, x, height * 0.6f + 12f, paint)
            }
        }

        // 5. Draw Red Line (Current Time - Center)
        paint.color = colorRedLine
        paint.strokeWidth = 4f
        canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint)

        // 6. Border
        paint.color = colorBorder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.style = Paint.Style.FILL // Reset

        return bitmap
    }
}
