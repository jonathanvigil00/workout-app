package com.jvigil.hoofmode.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import com.jvigil.hoofmode.data.repository.ActiveScheduleState
import com.jvigil.hoofmode.data.repository.ActivityRepository
import com.jvigil.hoofmode.data.repository.ScheduleRepository
import com.jvigil.hoofmode.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val sessionRepository: SessionRepository,
    activityRepository: ActivityRepository,
) : ViewModel() {

    val activeState: StateFlow<ActiveScheduleState?> =
        scheduleRepository.observeActiveScheduleState()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * True once today's occurrence has already been completed — the Home screen holds on a
     * "completed" state instead of immediately revealing the newly-advanced pointer item; the
     * next item only surfaces after the calendar date rolls over.
     */
    val isCompletedToday: StateFlow<Boolean> = activeState
        .map { it?.schedule?.lastCompletionDate == LocalDate.now() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentStreak: StateFlow<Int> =
        activityRepository.observeCurrentStreak().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Guards against re-firing the auto-complete call while its DB write is still in flight. */
    private var autoCompletingItemId: Long? = null

    init {
        viewModelScope.launch {
            activeState.collect { state ->
                val schedule = state?.schedule ?: return@collect
                val current = state.currentItem ?: return@collect
                val alreadyCompletedToday = schedule.lastCompletionDate == LocalDate.now()
                if (!alreadyCompletedToday && current.type == ScheduleItemType.REST && autoCompletingItemId != current.id) {
                    autoCompletingItemId = current.id
                    scheduleRepository.completeCurrentItem(schedule.id)
                }
            }
        }
    }

    fun startOrResumeCurrentWorkout(scheduleItemId: Long, onReady: (Long) -> Unit) {
        viewModelScope.launch { onReady(sessionRepository.getOrStartSessionForScheduleItem(scheduleItemId)) }
    }

    fun completeCurrentItem(scheduleId: Long) {
        viewModelScope.launch { scheduleRepository.completeCurrentItem(scheduleId) }
    }

    fun startAdHocSession(onReady: (Long) -> Unit) {
        viewModelScope.launch { onReady(sessionRepository.startAdHocSession()) }
    }
}
