package com.jvigil.hoofmode.ui.sessiondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.dao.SessionExerciseRow
import com.jvigil.hoofmode.data.local.entity.SetEntryEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutSessionEntity
import com.jvigil.hoofmode.data.repository.SessionRepository
import com.jvigil.hoofmode.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val sessionId: Long = checkNotNull(savedStateHandle[Screen.SessionDetail.ARG_SESSION_ID])

    val session: StateFlow<WorkoutSessionEntity?> =
        sessionRepository.observeSession(sessionId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sessionExercises: StateFlow<List<SessionExerciseRow>> =
        sessionRepository.observeSessionExercises(sessionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setsFlow(sessionExerciseId: Long): Flow<List<SetEntryEntity>> = sessionRepository.observeSets(sessionExerciseId)

    fun deleteSession(onDone: () -> Unit) {
        viewModelScope.launch {
            session.value?.let { sessionRepository.deleteSession(it) }
            onDone()
        }
    }

    fun repeatSession(onReady: (Long) -> Unit) {
        viewModelScope.launch { onReady(sessionRepository.repeatSession(sessionId)) }
    }
}
