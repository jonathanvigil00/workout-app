package com.jvigil.hoofmode.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.entity.ExerciseEntity
import com.jvigil.hoofmode.data.repository.BodyWeightRepository
import com.jvigil.hoofmode.data.repository.ExerciseRepository
import com.jvigil.hoofmode.data.repository.SessionRepository
import com.jvigil.hoofmode.ui.components.ChartPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class ChartTimeRange(val label: String, val days: Long?) {
    DAYS_30("30 days", 30),
    DAYS_90("90 days", 90),
    ALL_TIME("All time", null),
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val bodyWeightRepository: BodyWeightRepository,
    private val sessionRepository: SessionRepository,
    exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _timeRange = MutableStateFlow(ChartTimeRange.DAYS_90)
    val timeRange: StateFlow<ChartTimeRange> = _timeRange.asStateFlow()

    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    val selectedExerciseId: StateFlow<Long?> = _selectedExerciseId.asStateFlow()

    val exercises: StateFlow<List<ExerciseEntity>> =
        exerciseRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    val bodyWeightPoints: StateFlow<List<ChartPoint>> = _timeRange
        .flatMapLatest { range ->
            val since = range.days?.let { LocalDate.now().minusDays(it) } ?: LocalDate.of(2000, 1, 1)
            bodyWeightRepository.observeSince(since)
        }
        .map { entries ->
            entries.map { ChartPoint(x = it.date.toEpochDay().toFloat(), label = it.date.format(dateFormatter), y = it.weight.toFloat(), valueText = "${it.weight}") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val strengthPoints: StateFlow<List<ChartPoint>> = combine(_timeRange, _selectedExerciseId) { range, exerciseId -> range to exerciseId }
        .flatMapLatest { (range, exerciseId) ->
            if (exerciseId == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                val since = cutoffInstant(range)
                sessionRepository.observeWeightProgression(exerciseId, since)
            }
        }
        .map { rows ->
            rows.map {
                val date = it.sessionStart.atZone(ZoneId.systemDefault()).toLocalDate()
                ChartPoint(x = date.toEpochDay().toFloat(), label = date.format(dateFormatter), y = it.maxWeight.toFloat(), valueText = "${it.maxWeight}")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val volumePoints: StateFlow<List<ChartPoint>> = _timeRange
        .flatMapLatest { range -> sessionRepository.observeWeeklyVolume(cutoffInstant(range)) }
        .map { rows ->
            rows.mapIndexed { index, row ->
                val date = row.weekStart.atZone(ZoneId.systemDefault()).toLocalDate()
                ChartPoint(x = index.toFloat(), label = date.format(dateFormatter), y = row.totalVolume.toFloat(), valueText = "${row.totalVolume.toInt()}")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun cutoffInstant(range: ChartTimeRange): Instant =
        range.days?.let { Instant.now().minus(it, ChronoUnit.DAYS) } ?: Instant.EPOCH

    fun onTimeRangeChange(range: ChartTimeRange) {
        _timeRange.value = range
    }

    fun onExerciseSelected(exerciseId: Long?) {
        _selectedExerciseId.value = exerciseId
    }
}
