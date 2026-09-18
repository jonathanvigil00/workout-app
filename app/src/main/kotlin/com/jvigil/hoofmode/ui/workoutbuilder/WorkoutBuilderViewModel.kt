package com.jvigil.hoofmode.ui.workoutbuilder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.dao.TemplateExerciseRow
import com.jvigil.hoofmode.data.local.entity.OrderMode
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateExerciseEntity
import com.jvigil.hoofmode.data.repository.WorkoutTemplateRepository
import com.jvigil.hoofmode.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class WorkoutBuilderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WorkoutTemplateRepository,
) : ViewModel() {

    val isEditingExisting: Boolean
    private val _templateId = MutableStateFlow<Long?>(null)
    val templateId: StateFlow<Long?> = _templateId.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _orderMode = MutableStateFlow(OrderMode.UNORDERED)
    val orderMode: StateFlow<OrderMode> = _orderMode.asStateFlow()

    val exercises: StateFlow<List<TemplateExerciseRow>> = _templateId
        .filterNotNull()
        .flatMapLatest { repository.observeExercises(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val existingId = savedStateHandle.get<String>(Screen.WorkoutBuilder.ARG_TEMPLATE_ID)?.toLongOrNull()
        isEditingExisting = existingId != null
        if (existingId != null) {
            _templateId.value = existingId
            viewModelScope.launch {
                repository.getById(existingId)?.let {
                    _name.value = it.name
                    _orderMode.value = it.orderMode
                }
            }
        }
        viewModelScope.launch {
            _name.debounce(400).collect { persistOrCreate() }
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onOrderModeChange(mode: OrderMode) {
        _orderMode.value = mode
        viewModelScope.launch { persistOrCreate() }
    }

    private suspend fun persistOrCreate() {
        if (_name.value.isBlank()) return
        val id = _templateId.value
        if (id == null) {
            _templateId.value = repository.createTemplate(_name.value.trim(), _orderMode.value)
        } else {
            repository.updateTemplate(WorkoutTemplateEntity(id = id, name = _name.value.trim(), orderMode = _orderMode.value))
        }
    }

    fun addExercise(exerciseId: Long) {
        val id = _templateId.value ?: return
        viewModelScope.launch { repository.addExercise(id, exerciseId, targetSets = null, targetReps = null) }
    }

    fun updateDefaults(row: TemplateExerciseRow, targetSets: Int?, targetReps: Int?) {
        val id = _templateId.value ?: return
        viewModelScope.launch {
            repository.updateExerciseDefaults(
                WorkoutTemplateExerciseEntity(
                    id = row.id,
                    templateId = id,
                    exerciseId = row.exerciseId,
                    orderIndex = row.orderIndex,
                    targetSets = targetSets,
                    targetReps = targetReps,
                ),
            )
        }
    }

    fun removeExercise(templateExerciseId: Long) {
        viewModelScope.launch { repository.removeExercise(templateExerciseId) }
    }

    fun moveExercise(list: List<TemplateExerciseRow>, index: Int, delta: Int) {
        val target = index + delta
        if (target < 0 || target >= list.size) return
        val reordered = list.toMutableList().apply { add(target, removeAt(index)) }
        viewModelScope.launch { repository.reorderExercises(reordered.map { it.id }) }
    }

    suspend fun scheduleReferenceCount(): Int = _templateId.value?.let { repository.countScheduleReferences(it) } ?: 0

    fun deleteTemplate(onDone: () -> Unit) {
        val id = _templateId.value ?: return
        viewModelScope.launch {
            repository.getById(id)?.let { repository.deleteTemplate(it) }
            onDone()
        }
    }
}
