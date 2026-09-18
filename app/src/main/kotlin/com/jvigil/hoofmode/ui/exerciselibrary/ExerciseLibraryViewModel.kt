package com.jvigil.hoofmode.ui.exerciselibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.entity.ExerciseEntity
import com.jvigil.hoofmode.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val repository: ExerciseRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val exercises: StateFlow<List<ExerciseEntity>> = _query
        .debounce(150)
        .flatMapLatest { repository.search(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun createCustomExercise(name: String, muscleGroup: String?, equipment: String?, onCreated: (Long) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createCustom(name, muscleGroup?.takeIf { it.isNotBlank() }, equipment?.takeIf { it.isNotBlank() })
            onCreated(id)
        }
    }
}
