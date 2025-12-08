package com.example.core.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalendarRepository(private val context: Context) {

    suspend fun getEventsForToday(): List<DayEvent> = withContext(Dispatchers.IO) {
        // Kontrollera behörighet först
        if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList<DayEvent>()
        }

        val events = mutableListOf<DayEvent>()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.DISPLAY_COLOR
        )

        // Sätt upp tid för "idag" med java.time
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val startOfDayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDayMillis = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        // Bygg query
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startOfDayMillis)
        ContentUris.appendId(builder, endOfDayMillis)

        try {
            context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val beginIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val colorIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DISPLAY_COLOR)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx)
                    val title = cursor.getString(titleIdx) ?: "No Title"
                    val begin = cursor.getLong(beginIdx)
                    val end = cursor.getLong(endIdx)
                    val color = cursor.getInt(colorIdx)

                    val startTime = Instant.ofEpochMilli(begin).atZone(zoneId).toLocalTime()
                    val endTime = Instant.ofEpochMilli(end).atZone(zoneId).toLocalTime()

                    events.add(
                        DayEvent(
                            id = id,
                            title = title,
                            start = startTime,
                            end = endTime,
                            color = Color(color)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Returnera tom lista eller logga fel vid problem
        }
        events
    }
}
