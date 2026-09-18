package com.jvigil.hoofmode.ui.schedulebuilder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.entity.ScheduleEntity
import com.jvigil.hoofmode.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleListViewModel @Inject constructor(
    private val repository: ScheduleRepository,
) : ViewModel() {
    val schedules: StateFlow<List<ScheduleEntity>> =
        repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createSchedule(onCreated: (Long) -> Unit) {
        viewModelScope.launch { onCreated(repository.createSchedule("New Schedule")) }
    }
}
