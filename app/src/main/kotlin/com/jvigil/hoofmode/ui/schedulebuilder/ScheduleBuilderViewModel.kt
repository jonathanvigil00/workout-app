package com.jvigil.hoofmode.ui.schedulebuilder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.dao.ScheduleItemRow
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateEntity
import com.jvigil.hoofmode.data.repository.ScheduleRepository
import com.jvigil.hoofmode.data.repository.WorkoutTemplateRepository
import com.jvigil.hoofmode.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ScheduleBuilderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scheduleRepository: ScheduleRepository,
    private val templateRepository: WorkoutTemplateRepository,
) : ViewModel() {

    val scheduleId: Long = checkNotNull(savedStateHandle[Screen.ScheduleBuilder.ARG_SCHEDULE_ID])

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    val items: StateFlow<List<ScheduleItemRow>> =
        scheduleRepository.observeItemsForSchedule(scheduleId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableTemplates: StateFlow<List<WorkoutTemplateEntity>> =
        templateRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            scheduleRepository.getById(scheduleId)?.let {
                _name.value = it.name
                _isActive.value = it.isActive
            }
        }
        viewModelScope.launch {
            _name.debounce(400).collect { value ->
                if (value.isNotBlank()) {
                    scheduleRepository.getById(scheduleId)?.let { scheduleRepository.renameSchedule(it, value.trim()) }
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
    }

    fun addWorkoutItem(templateId: Long) {
        viewModelScope.launch { scheduleRepository.addItem(scheduleId, ScheduleItemType.WORKOUT, templateId) }
    }

    fun addRestItem() {
        viewModelScope.launch { scheduleRepository.addItem(scheduleId, ScheduleItemType.REST, null) }
    }

    fun removeItem(itemId: Long) {
        viewModelScope.launch { scheduleRepository.removeItem(itemId) }
    }

    fun moveItem(list: List<ScheduleItemRow>, index: Int, delta: Int) {
        val target = index + delta
        if (target < 0 || target >= list.size) return
        val reordered = list.toMutableList().apply { add(target, removeAt(index)) }
        viewModelScope.launch { scheduleRepository.reorderItems(scheduleId, reordered.map { it.id }) }
    }

    fun activate() {
        viewModelScope.launch {
            scheduleRepository.activateSchedule(scheduleId)
            _isActive.value = true
        }
    }

    fun deleteSchedule(onDone: () -> Unit) {
        viewModelScope.launch {
            scheduleRepository.getById(scheduleId)?.let { scheduleRepository.deleteSchedule(it) }
            onDone()
        }
    }
}
