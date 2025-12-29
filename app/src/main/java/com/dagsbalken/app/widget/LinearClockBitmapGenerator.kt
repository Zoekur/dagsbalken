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
import java.util.concurrent.ConcurrentHashMap

object LinearClockBitmapGenerator {

    // ThreadLocal to reuse Paint object and avoid allocation every minute per widget
    // Paint is not thread-safe, so ThreadLocal ensures safety if multiple updates happen concurrently on different threads.
    private val paintCache = ThreadLocal.withInitial {
        Paint()
    }

    // Cache for Typeface objects to avoid repeated JNI lookups/creation
    // Bolt Optimization: Use nested map to avoid string allocation for composite keys ("font-style")
    // Map: FontFamily -> (Style -> Typeface)
    private val typefaceCache = ConcurrentHashMap<String, ConcurrentHashMap<Int, Typeface>>()

    fun generate(
        context: Context,
        width: Int,
        height: Int,
        events: List<DayEvent>,
        config: WidgetConfig,
        currentTime: LocalTime = LocalTime.now()
    ): Bitmap {
        // Guard against zero dimensions which cause crash in bitmap creation
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Colors from config
        val colorFuture = config.backgroundColor
        val colorPassed = 0xFFE5E7EB.toInt() // Light gray for passed
        val colorRedLine = config.accentColor
        val colorBorder = config.textColor

        // Reuse Paint object from ThreadLocal to prevent allocation
        val paint = paintCache.get()!!
        paint.reset() // Reset to default settings to avoid state leaking from previous uses
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        // Adjust scale/density based on size mode
        val densityMultiplier = when (config.clockSize) {
            LinearClockPrefs.SIZE_4x2 -> 2.0f // Double height, scale up slightly
            LinearClockPrefs.SIZE_2x1 -> 0.8f // Compact width, scale down slightly? Or just fit.
            else -> 1.0f
        }

        // Time Window Logic
        val totalWindowHours = config.hoursToShow.coerceIn(4, 24)
        val windowDurationMinutes = totalWindowHours * 60

        // Bolt Optimization: Calculate pixels per minute (multiplication factor) instead of minutes per pixel (divisor)
        // Multiplication is faster than division in loops.
        // Old: x = delta / (minutes / width)
        // New: x = delta * (width / minutes)
        val pixelsPerMinute = safeWidth.toFloat() / windowDurationMinutes.toFloat()

        val currentMinuteOfDay = currentTime.hour * 60 + currentTime.minute

        val windowStartMinute: Int = if (totalWindowHours == 24) {
             // Fixed 00:00 to 24:00
             0
        } else {
             // Centered around now
             currentMinuteOfDay - (windowDurationMinutes / 2)
        }
        val windowEndMinute = windowStartMinute + windowDurationMinutes

        val currentX = (currentMinuteOfDay - windowStartMinute) * pixelsPerMinute

        // Bolt Optimization: Draw separate rects for Passed and Future to avoid overdraw (filling pixels twice)
        // 1. Draw Passed Time (Left Side)
        if (currentX > 0) {
             paint.color = colorPassed
             val passedWidth = currentX.coerceAtMost(safeWidth.toFloat())
             canvas.drawRect(0f, 0f, passedWidth, safeHeight.toFloat(), paint)
        }

        // 2. Draw Future Time (Right Side)
        if (currentX < safeWidth) {
             paint.color = colorFuture
             val futureStart = currentX.coerceAtLeast(0f)
             canvas.drawRect(futureStart, 0f, safeWidth.toFloat(), safeHeight.toFloat(), paint)
        }

        // 3. Draw Events
        // Events are filtered in Widget before calling this if showEvents is false.
        // Bolt Optimization: Use indexed loop to avoid Iterator allocation
        for (i in events.indices) {
            val event = events[i]
            val startMin = event.start.hour * 60 + event.start.minute
            val endMin = (event.end?.hour ?: 0) * 60 + (event.end?.minute ?: 0)
            val actualEndMin = if (event.end != null && endMin > startMin) endMin else startMin + 60

            val eventStartPx = (startMin - windowStartMinute) * pixelsPerMinute
            val eventWidthPx = (actualEndMin - startMin) * pixelsPerMinute

            if (eventStartPx + eventWidthPx > 0 && eventStartPx < safeWidth) {
                paint.color = event.color
                paint.alpha = 150 // Slightly transparent

                val left = eventStartPx.coerceAtLeast(0f)
                val right = (eventStartPx + eventWidthPx).coerceAtMost(safeWidth.toFloat())

                // Draw bar. Adjust height based on available height.
                // Leave 20% top/bottom padding
                canvas.drawRect(left, safeHeight * 0.2f, right, safeHeight * 0.8f, paint)

                paint.alpha = 255
            }
        }

        // 4. Draw Hour Ticks and Text
        paint.color = colorBorder
        paint.strokeWidth = 2f * if(densityMultiplier > 1.5) 1.5f else 1f
        paint.textSize = 24f * config.scale * densityMultiplier

        // Bolt Optimization: Cached Typeface lookup using nested maps to avoid string allocation
        val style = Typeface.BOLD
        val fontStyles = typefaceCache.computeIfAbsent(config.font) {
            ConcurrentHashMap<Int, Typeface>()
        }
        paint.typeface = fontStyles.computeIfAbsent(style) { s ->
            Typeface.create(config.font, s)
        }

        paint.textAlign = Paint.Align.CENTER

        // Find first visible hour
        val firstHour = (windowStartMinute / 60) - 1
        val lastHour = (windowEndMinute / 60) + 1

        for (h in firstHour..lastHour) {
            val hourMin = h * 60
            val x = (hourMin - windowStartMinute) * pixelsPerMinute

            if (x >= 0 && x <= safeWidth) {
                // Draw Tick
                // Longer tick for 4x2?
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
