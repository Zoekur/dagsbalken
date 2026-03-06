package com.dagsbalken.core.schedule

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Repository for persisting and retrieving user-defined timeline symbol schedules.
 * Shared between the main app and widgets.
 */
interface TimelineSymbolScheduleRepository {
    val scheduleFlow: Flow<TimelineSymbolSchedule>

    suspend fun getCurrent(): TimelineSymbolSchedule

    suspend fun update(transform: (TimelineSymbolSchedule) -> TimelineSymbolSchedule)
}

private const val DATASTORE_FILE_NAME = "timeline_symbol_schedule.json"

/**
 * JSON serializer for [TimelineSymbolSchedule] using kotlinx.serialization.
 */
object TimelineSymbolScheduleSerializer : Serializer<TimelineSymbolSchedule> {
    override val defaultValue: TimelineSymbolSchedule = TimelineSymbolSchedule()

    override suspend fun readFrom(input: InputStream): TimelineSymbolSchedule {
        return try {
            val text = input.readBytes().decodeToString()
            if (text.isBlank()) return defaultValue
            Json.decodeFromString(TimelineSymbolSchedule.serializer(), text)
        } catch (e: SerializationException) {
            defaultValue
        } catch (e: IllegalArgumentException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: TimelineSymbolSchedule, output: OutputStream) {
        val json = Json.encodeToString(TimelineSymbolSchedule.serializer(), t)
        output.write(json.encodeToByteArray())
    }
}

private val Context.timelineScheduleDataStore: DataStore<TimelineSymbolSchedule> by dataStore(
    fileName = DATASTORE_FILE_NAME,
    serializer = TimelineSymbolScheduleSerializer
)

class TimelineSymbolScheduleRepositoryImpl(
    private val context: Context
) : TimelineSymbolScheduleRepository {

    private val dataStore: DataStore<TimelineSymbolSchedule>
        get() = context.timelineScheduleDataStore

    override val scheduleFlow: Flow<TimelineSymbolSchedule>
        get() = dataStore.data

    override suspend fun getCurrent(): TimelineSymbolSchedule = dataStore.data.first()

    override suspend fun update(transform: (TimelineSymbolSchedule) -> TimelineSymbolSchedule) {
        dataStore.updateData { current -> transform(current) }
    }
}
