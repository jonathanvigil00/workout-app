package com.jvigil.hoofmode.ui.workoutbuilder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateEntity
import com.jvigil.hoofmode.data.repository.WorkoutTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    repository: WorkoutTemplateRepository,
) : ViewModel() {
    val templates: StateFlow<List<WorkoutTemplateEntity>> =
        repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
