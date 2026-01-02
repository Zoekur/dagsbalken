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

    // ThreadLocal to reuse Bitmap buffer and avoid 1MB+ allocation per minute/update.
    // Thrashing may occur if multiple widgets with different sizes update on the same thread,
    // but this still optimizes the common case (single widget or same sizes).
    private val bitmapCache = ThreadLocal<Bitmap>()

    fun generate(
        context: Context,
        width: Int,
        height: Int,
        events: List<DayEvent>,
        config: WidgetConfig,
        currentTime: LocalTime = LocalTime.now()
    ): Bitmap {
        // Reuse Bitmap from ThreadLocal to avoid massive allocation
        val cachedBitmap = bitmapCache.get()
        val bitmap = if (cachedBitmap != null && cachedBitmap.width == width && cachedBitmap.height == height) {
            cachedBitmap.eraseColor(0) // Clear previous content
            cachedBitmap
        } else {
            val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmapCache.set(newBitmap)
            newBitmap
        }

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

        // 1. Draw Background (Future - Right Side)
        paint.color = colorFuture
        paint.style = Paint.Style.FILL // Default after reset is FILL, but explicit is good
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Adjust scale/density based on size mode
        val densityMultiplier = when (config.clockSize) {
            LinearClockPrefs.SIZE_4x2 -> 2.0f // Double height, scale up slightly
            LinearClockPrefs.SIZE_2x1 -> 0.8f // Compact width, scale down slightly? Or just fit.
            else -> 1.0f
        }
        // Actually, height is passed in. If 4x2 is selected, height is ~160dp.
        // We might want larger text/ticks for 4x2.

        // Time Window Logic
        val totalWindowHours = config.hoursToShow.coerceIn(4, 24)
        val windowDurationMinutes = totalWindowHours * 60
        val minutesPerPixel = windowDurationMinutes.toFloat() / width

        val currentMinuteOfDay = currentTime.hour * 60 + currentTime.minute

        val windowStartMinute: Int

        if (totalWindowHours == 24) {
             // Fixed 00:00 to 24:00
             windowStartMinute = 0
        } else {
             // Centered around now
             windowStartMinute = currentMinuteOfDay - (windowDurationMinutes / 2)
        }
        val windowEndMinute = windowStartMinute + windowDurationMinutes

        // 2. Draw Passed Time (Gray overlay)
        val currentX = (currentMinuteOfDay - windowStartMinute) / minutesPerPixel
        if (currentX > 0) {
             paint.color = colorPassed
             val passedWidth = currentX.coerceAtMost(width.toFloat())
             canvas.drawRect(0f, 0f, passedWidth, height.toFloat(), paint)
        }

        // 3. Draw Events
        // Events are filtered in Widget before calling this if showEvents is false.
        // Bolt Optimization: Use indexed loop to avoid Iterator allocation
        for (i in events.indices) {
            val event = events[i]
            val startMin = event.start.hour * 60 + event.start.minute
            val endMin = (event.end?.hour ?: 0) * 60 + (event.end?.minute ?: 0)
            val actualEndMin = if (event.end != null && endMin > startMin) endMin else startMin + 60

            val eventStartPx = (startMin - windowStartMinute) / minutesPerPixel
            val eventWidthPx = (actualEndMin - startMin) / minutesPerPixel

            if (eventStartPx + eventWidthPx > 0 && eventStartPx < width) {
                paint.color = event.color
                paint.alpha = 150 // Slightly transparent

                val left = eventStartPx.coerceAtLeast(0f)
                val right = (eventStartPx + eventWidthPx).coerceAtMost(width.toFloat())

                // Draw bar. Adjust height based on available height.
                // Leave 20% top/bottom padding
                canvas.drawRect(left, height * 0.2f, right, height * 0.8f, paint)

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
            val x = (hourMin - windowStartMinute) / minutesPerPixel

            if (x >= 0 && x <= width) {
                // Draw Tick
                // Longer tick for 4x2?
                val tickHeight = height * 0.3f
                canvas.drawLine(x, 0f, x, tickHeight, paint)

                // Draw Text
                // Text drawing removed as per user request
            }
        }

        // 5. Draw Red Line (Current Time)
        if (currentX >= 0 && currentX <= width) {
            paint.color = colorRedLine
            paint.strokeWidth = 4f * if(densityMultiplier > 1.5) 1.5f else 1f
            canvas.drawLine(currentX, 0f, currentX, height.toFloat(), paint)
        }

        // 6. Border
        paint.color = colorBorder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.style = Paint.Style.FILL // Reset

        return bitmap
    }
}
