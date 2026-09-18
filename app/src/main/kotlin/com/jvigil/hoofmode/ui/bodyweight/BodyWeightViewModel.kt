package com.jvigil.hoofmode.ui.bodyweight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.entity.BodyWeightEntryEntity
import com.jvigil.hoofmode.data.prefs.SettingsRepository
import com.jvigil.hoofmode.data.prefs.WeightUnit
import com.jvigil.hoofmode.data.repository.BodyWeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class BodyWeightViewModel @Inject constructor(
    private val repository: BodyWeightRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val entries: StateFlow<List<BodyWeightEntryEntity>> =
        repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightUnit: StateFlow<WeightUnit> =
        settingsRepository.settings.map { it.weightUnit }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.LB)

    fun logToday(weight: Double, note: String?) {
        viewModelScope.launch { repository.logEntry(LocalDate.now(), weight, note?.takeIf { it.isNotBlank() }) }
    }

    fun updateEntry(entry: BodyWeightEntryEntity) {
        viewModelScope.launch { repository.updateEntry(entry) }
    }

    fun deleteEntry(entry: BodyWeightEntryEntity) {
        viewModelScope.launch { repository.deleteEntry(entry) }
    }
}
