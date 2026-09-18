package com.jvigil.hoofmode.ui.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jvigil.hoofmode.ui.components.SimpleLineChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(viewModel: ChartsViewModel = hiltViewModel()) {
    val timeRange by viewModel.timeRange.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val selectedExerciseId by viewModel.selectedExerciseId.collectAsState()
    val bodyWeightPoints by viewModel.bodyWeightPoints.collectAsState()
    val strengthPoints by viewModel.strengthPoints.collectAsState()
    val volumePoints by viewModel.volumePoints.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Charts") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ChartTimeRange.entries) { range ->
                        FilterChip(
                            selected = timeRange == range,
                            onClick = { viewModel.onTimeRangeChange(range) },
                            label = { Text(range.label) },
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Body Weight", style = MaterialTheme.typography.titleMedium)
                        SimpleLineChart(points = bodyWeightPoints, emptyMessage = "Log a few weigh-ins to see this chart.")
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Strength Progression", style = MaterialTheme.typography.titleMedium)
                        ExerciseSelector(
                            exercises = exercises,
                            selectedId = selectedExerciseId,
                            onSelect = viewModel::onExerciseSelected,
                        )
                        if (selectedExerciseId == null) {
                            Text("Pick an exercise to see its progression.", modifier = Modifier.padding(16.dp))
                        } else {
                            SimpleLineChart(points = strengthPoints, emptyMessage = "No logged sets for this exercise yet.")
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Weekly Volume", style = MaterialTheme.typography.titleMedium)
                        SimpleLineChart(points = volumePoints, emptyMessage = "Log some sets to see weekly volume.")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseSelector(
    exercises: List<com.jvigil.hoofmode.data.local.entity.ExerciseEntity>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = exercises.firstOrNull { it.id == selectedId }?.name ?: "Select exercise"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name) },
                    onClick = {
                        onSelect(exercise.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
