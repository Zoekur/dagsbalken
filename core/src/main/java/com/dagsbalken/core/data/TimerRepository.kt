package com.dagsbalken.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.timerDataStore by preferencesDataStore(name = "timer_preferences")

class TimerRepository(private val context: Context) {

    companion object {
        private const val TAG = "TimerRepository"
        private val TIMER_TEMPLATES_KEY = stringPreferencesKey("timer_templates")
        private val ACTIVE_TIMERS_KEY = stringPreferencesKey("active_timers")
        private val TIMER_TEMPLATES_BACKUP_KEY = stringPreferencesKey("timer_templates_backup")
        private val ACTIVE_TIMERS_BACKUP_KEY = stringPreferencesKey("active_timers_backup")
    }

    // --- Timer Templates ---

    val timerTemplatesFlow: Flow<List<TimerModel>> = context.timerDataStore.data
        .map { preferences ->
            val jsonString = preferences[TIMER_TEMPLATES_KEY] ?: "[]"
            val backupString = preferences[TIMER_TEMPLATES_BACKUP_KEY]
            TimerSerializer.deserializeTimerTemplatesWithRecovery(jsonString, backupString, throwOnFailure = false)
        }

    suspend fun saveTimerTemplate(timer: TimerModel) {
        context.timerDataStore.edit { preferences ->
            val jsonString = preferences[TIMER_TEMPLATES_KEY] ?: "[]"
            val backupString = preferences[TIMER_TEMPLATES_BACKUP_KEY]
            val initialList = TimerSerializer.deserializeTimerTemplatesWithRecovery(jsonString, backupString, throwOnFailure = true)
            val currentList = initialList.toMutableList()

            val index = currentList.indexOfFirst { it.id == timer.id }
            if (index != -1) {
                currentList[index] = timer
            } else {
                currentList.add(timer)
            }
            
            val serialized = TimerSerializer.serializeTimerTemplates(currentList)
            // Update backup with the last known good state before writing new data
            preferences[TIMER_TEMPLATES_BACKUP_KEY] = TimerSerializer.serializeTimerTemplates(initialList)
            preferences[TIMER_TEMPLATES_KEY] = serialized
        }
    }

    suspend fun deleteTimerTemplate(timerId: String) {
        context.timerDataStore.edit { preferences ->
            val jsonString = preferences[TIMER_TEMPLATES_KEY] ?: "[]"
            val backupString = preferences[TIMER_TEMPLATES_BACKUP_KEY]
            val initialList = TimerSerializer.deserializeTimerTemplatesWithRecovery(jsonString, backupString, throwOnFailure = true)
            val currentList = initialList.toMutableList()

            currentList.removeAll { it.id == timerId }
            
            val serialized = TimerSerializer.serializeTimerTemplates(currentList)
            // Update backup with the last known good state before writing new data
            preferences[TIMER_TEMPLATES_BACKUP_KEY] = TimerSerializer.serializeTimerTemplates(initialList)
            preferences[TIMER_TEMPLATES_KEY] = serialized
        }
    }

    // --- Active Timers (CustomBlocks) ---

    val activeTimersFlow: Flow<List<CustomBlock>> = context.timerDataStore.data
        .map { preferences ->
            val jsonString = preferences[ACTIVE_TIMERS_KEY] ?: "[]"
            val backupString = preferences[ACTIVE_TIMERS_BACKUP_KEY]
            TimerSerializer.deserializeActiveTimersWithRecovery(jsonString, backupString, throwOnFailure = false)
        }

    suspend fun addActiveTimer(block: CustomBlock) {
        context.timerDataStore.edit { preferences ->
            val jsonString = preferences[ACTIVE_TIMERS_KEY] ?: "[]"
            val backupString = preferences[ACTIVE_TIMERS_BACKUP_KEY]
            val initialList = TimerSerializer.deserializeActiveTimersWithRecovery(jsonString, backupString, throwOnFailure = true)
            val currentList = initialList.toMutableList()

            currentList.add(block)
            
            val serialized = TimerSerializer.serializeActiveTimers(currentList)
            // Update backup with the last known good state before writing new data
            preferences[ACTIVE_TIMERS_BACKUP_KEY] = TimerSerializer.serializeActiveTimers(initialList)
            preferences[ACTIVE_TIMERS_KEY] = serialized
        }
    }

    suspend fun removeActiveTimer(blockId: String) {
        context.timerDataStore.edit { preferences ->
            val jsonString = preferences[ACTIVE_TIMERS_KEY] ?: "[]"
            val backupString = preferences[ACTIVE_TIMERS_BACKUP_KEY]
            val initialList = TimerSerializer.deserializeActiveTimersWithRecovery(jsonString, backupString, throwOnFailure = true)
            val currentList = initialList.toMutableList()

            currentList.removeAll { it.id == blockId }
            
            val serialized = TimerSerializer.serializeActiveTimers(currentList)
            // Update backup with the last known good state before writing new data
            preferences[ACTIVE_TIMERS_BACKUP_KEY] = TimerSerializer.serializeActiveTimers(initialList)
            preferences[ACTIVE_TIMERS_KEY] = serialized
        }
    }
}
