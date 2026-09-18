package com.jvigil.hoofmode.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.prefs.HoofSettings
import com.jvigil.hoofmode.data.prefs.SettingsRepository
import com.jvigil.hoofmode.data.prefs.WeightUnit
import com.jvigil.hoofmode.data.reminder.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val settings: StateFlow<HoofSettings> =
        settingsRepository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HoofSettings())

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { settingsRepository.setWeightUnit(unit) }
    }

    fun setBodyWeightReminder(enabled: Boolean, frequencyDays: Int) {
        viewModelScope.launch {
            settingsRepository.setBodyWeightReminder(enabled, frequencyDays)
            if (enabled) reminderScheduler.schedule(frequencyDays) else reminderScheduler.cancel()
        }
    }

    fun setRestTimerDefaults(defaultSeconds: Int, vibrate: Boolean) {
        viewModelScope.launch { settingsRepository.setRestTimerDefaults(defaultSeconds, vibrate) }
    }
}
