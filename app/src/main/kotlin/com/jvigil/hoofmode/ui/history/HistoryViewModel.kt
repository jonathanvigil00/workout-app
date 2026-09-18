package com.jvigil.hoofmode.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.entity.WorkoutSessionEntity
import com.jvigil.hoofmode.data.repository.ActivityRepository
import com.jvigil.hoofmode.data.repository.SessionRepository
import com.jvigil.hoofmode.domain.model.CalendarDay
import com.jvigil.hoofmode.domain.model.buildCalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    activityRepository: ActivityRepository,
    sessionRepository: SessionRepository,
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val dayActivitiesInMonth = _currentMonth.flatMapLatest { month ->
        activityRepository.observeDayActivitiesInRange(month.atDay(1), month.plusMonths(1).atDay(1))
    }

    val calendarDays: StateFlow<List<CalendarDay>> = combine(
        _currentMonth,
        dayActivitiesInMonth,
        activityRepository.observeEarliestActivityDate(),
    ) { month, activitiesByDate, earliestDate ->
        val today = LocalDate.now()
        (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            buildCalendarDay(date, activitiesByDate[date], earliestDate, today)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStreak: StateFlow<Int> =
        activityRepository.observeCurrentStreak().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val workoutsThisMonth: StateFlow<Int> = _currentMonth
        .flatMapLatest { month ->
            activityRepository.observeWorkoutsThisMonth(month.atDay(1), month.plusMonths(1).atDay(1))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allSessions: StateFlow<List<WorkoutSessionEntity>> =
        sessionRepository.observeAllSessions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun goToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }
}
