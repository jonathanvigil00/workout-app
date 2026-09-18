package com.jvigil.hoofmode.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class WeightUnit { LB, KG }

data class HoofSettings(
    val weightUnit: WeightUnit = WeightUnit.LB,
    val bodyWeightReminderEnabled: Boolean = false,
    val reminderFrequencyDays: Int = 1,
    val restTimerDefaultSeconds: Int = 90,
    val restTimerVibrate: Boolean = true,
)

@Singleton
class SettingsRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val REMINDER_ENABLED = booleanPreferencesKey("body_weight_reminder_enabled")
        val REMINDER_FREQUENCY_DAYS = intPreferencesKey("reminder_frequency_days")
        val REST_TIMER_SECONDS = intPreferencesKey("rest_timer_default_seconds")
        val REST_TIMER_VIBRATE = booleanPreferencesKey("rest_timer_vibrate")
    }

    val settings: Flow<HoofSettings> = dataStore.data.map { prefs ->
        HoofSettings(
            weightUnit = prefs[Keys.WEIGHT_UNIT]?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() } ?: WeightUnit.LB,
            bodyWeightReminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false,
            reminderFrequencyDays = prefs[Keys.REMINDER_FREQUENCY_DAYS] ?: 1,
            restTimerDefaultSeconds = prefs[Keys.REST_TIMER_SECONDS] ?: 90,
            restTimerVibrate = prefs[Keys.REST_TIMER_VIBRATE] ?: true,
        )
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        dataStore.edit { it[Keys.WEIGHT_UNIT] = unit.name }
    }

    suspend fun setBodyWeightReminder(enabled: Boolean, frequencyDays: Int) {
        dataStore.edit {
            it[Keys.REMINDER_ENABLED] = enabled
            it[Keys.REMINDER_FREQUENCY_DAYS] = frequencyDays
        }
    }

    suspend fun setRestTimerDefaults(defaultSeconds: Int, vibrate: Boolean) {
        dataStore.edit {
            it[Keys.REST_TIMER_SECONDS] = defaultSeconds
            it[Keys.REST_TIMER_VIBRATE] = vibrate
        }
    }
}
