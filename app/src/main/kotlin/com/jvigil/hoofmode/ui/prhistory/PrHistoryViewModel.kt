package com.jvigil.hoofmode.ui.prhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.dao.BestSetRow
import com.jvigil.hoofmode.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PrHistoryViewModel @Inject constructor(
    sessionRepository: SessionRepository,
) : ViewModel() {
    val bestSets: StateFlow<List<BestSetRow>> =
        sessionRepository.observeAllBestSets().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
