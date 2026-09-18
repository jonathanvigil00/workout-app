package com.jvigil.hoofmode.ui.session

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.dao.BestSetRow
import com.jvigil.hoofmode.data.local.dao.SessionExerciseRow
import com.jvigil.hoofmode.data.local.entity.OrderMode
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import com.jvigil.hoofmode.data.local.entity.SetEntryEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutSessionEntity
import com.jvigil.hoofmode.data.prefs.SettingsRepository
import com.jvigil.hoofmode.data.repository.ScheduleRepository
import com.jvigil.hoofmode.data.repository.SessionRepository
import com.jvigil.hoofmode.data.repository.WorkoutTemplateRepository
import com.jvigil.hoofmode.domain.prdetection.PrResult
import com.jvigil.hoofmode.ui.navigation.Screen
import com.jvigil.hoofmode.ui.resttimer.RestTimerController
import com.jvigil.hoofmode.ui.resttimer.RestTimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val templateRepository: WorkoutTemplateRepository,
    private val scheduleRepository: ScheduleRepository,
    settingsRepository: SettingsRepository,
    @ApplicationContext context: Context,
) : ViewModel() {

    val sessionId: Long = checkNotNull(savedStateHandle[Screen.ActiveSession.ARG_SESSION_ID])

    val session: StateFlow<WorkoutSessionEntity?> =
        sessionRepository.observeSession(sessionId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sessionExercises: StateFlow<List<SessionExerciseRow>> =
        sessionRepository.observeSessionExercises(sessionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _orderMode = MutableStateFlow(OrderMode.UNORDERED)
    val orderMode: StateFlow<OrderMode> = _orderMode.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    val weightUnit = settingsRepository.settings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null,
    )

    val restTimer = RestTimerController(context)
    val restTimerState: StateFlow<RestTimerState> =
        restTimer.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RestTimerState.Idle)

    private var defaultRestSeconds = 90
    private var restTimerVibrate = true

    init {
        viewModelScope.launch {
            val s = session.filterNotNull().first()
            _notes.value = s.notes ?: ""
            val scheduleItemId = s.sourceScheduleItemId
            if (scheduleItemId != null) {
                val item = scheduleRepository.getItemById(scheduleItemId)
                if (item?.type == ScheduleItemType.WORKOUT && item.workoutTemplateId != null) {
                    templateRepository.getById(item.workoutTemplateId)?.let { _orderMode.value = it.orderMode }
                }
            }
        }
        viewModelScope.launch {
            _notes.debounce(500).collect { sessionRepository.updateSessionNotes(sessionId, it.ifBlank { null }) }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect {
                defaultRestSeconds = it.restTimerDefaultSeconds
                restTimerVibrate = it.restTimerVibrate
            }
        }
    }

    fun onNotesChange(value: String) {
        _notes.value = value
    }

    fun bestSetFlow(exerciseId: Long): Flow<BestSetRow?> = sessionRepository.observeBestSet(exerciseId)

    fun setsFlow(sessionExerciseId: Long): Flow<List<SetEntryEntity>> = sessionRepository.observeSets(sessionExerciseId)

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch { sessionRepository.addExerciseToSession(sessionId, exerciseId) }
    }

    fun removeExercise(sessionExerciseId: Long) {
        viewModelScope.launch { sessionRepository.removeExerciseFromSession(sessionExerciseId) }
    }

    fun moveExercise(list: List<SessionExerciseRow>, index: Int, delta: Int) {
        val target = index + delta
        if (target < 0 || target >= list.size) return
        val reordered = list.toMutableList().apply { add(target, removeAt(index)) }
        viewModelScope.launch { sessionRepository.reorderSessionExercises(reordered.map { it.id }) }
    }

    fun setMarkedDone(sessionExerciseId: Long, done: Boolean) {
        viewModelScope.launch { sessionRepository.setExerciseMarkedDone(sessionExerciseId, done) }
    }

    fun logSet(sessionExerciseId: Long, weight: Double, reps: Int, rpe: Int?, note: String?, onResult: (PrResult) -> Unit) {
        viewModelScope.launch {
            val result = sessionRepository.logSet(sessionExerciseId, weight, reps, rpe, note)
            onResult(result)
            restTimer.start(viewModelScope, defaultRestSeconds, restTimerVibrate)
        }
    }

    fun updateSet(set: SetEntryEntity) {
        viewModelScope.launch { sessionRepository.updateSet(set) }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { sessionRepository.deleteSet(setId) }
    }

    fun startRestTimer() {
        restTimer.start(viewModelScope, defaultRestSeconds, restTimerVibrate)
    }

    fun cancelRestTimer() = restTimer.cancel()

    /**
     * Ending a session tied to the active schedule's current item must go through the same
     * pointer-advancing path as Home's "Complete Workout" — otherwise the schedule never
     * advances and a later tap on "Complete Workout" creates a second session/activity-log
     * entry for the same day. Ad-hoc sessions have no pointer to advance, so they just end.
     */
    fun endSession(onDone: () -> Unit) {
        viewModelScope.launch {
            val scheduleItemId = (session.value ?: sessionRepository.getSession(sessionId))?.sourceScheduleItemId
            val scheduleId = scheduleItemId?.let { scheduleRepository.getItemById(it)?.scheduleId }
            if (scheduleId != null) {
                scheduleRepository.completeCurrentItem(scheduleId)
            } else {
                sessionRepository.endSession(sessionId)
            }
            onDone()
        }
    }
}
