package com.jvigil.hoofmode.ui.workoutbuilder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.data.local.dao.TemplateExerciseRow
import com.jvigil.hoofmode.data.local.entity.OrderMode
import com.jvigil.hoofmode.ui.components.BackIconButton
import com.jvigil.hoofmode.ui.navigation.SELECTED_EXERCISE_ID
import com.jvigil.hoofmode.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutBuilderScreen(
    navController: NavHostController,
    viewModel: WorkoutBuilderViewModel = hiltViewModel(),
) {
    val name by viewModel.name.collectAsState()
    val orderMode by viewModel.orderMode.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteWarningCount by remember { mutableStateOf(0) }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<Long?>(SELECTED_EXERCISE_ID, null)?.collect { id ->
            if (id != null) {
                viewModel.addExercise(id)
                savedStateHandle.remove<Long>(SELECTED_EXERCISE_ID)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditingExisting) "Edit Workout" else "New Workout") },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },
                actions = {
                    if (viewModel.isEditingExisting) {
                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete workout")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Workout name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Exercise order", modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = orderMode == OrderMode.UNORDERED,
                    onClick = { viewModel.onOrderModeChange(OrderMode.UNORDERED) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("Unordered") }
                SegmentedButton(
                    selected = orderMode == OrderMode.ENFORCED,
                    onClick = { viewModel.onOrderModeChange(OrderMode.ENFORCED) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("Enforced") }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Exercises")
                FilledTonalButton(
                    enabled = name.isNotBlank(),
                    onClick = { navController.navigate(Screen.ExerciseLibrary.route) },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.width(18.dp))
                    Text("Add")
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(exercises, key = { it.id }) { row ->
                    TemplateExerciseCard(
                        row = row,
                        modifier = Modifier.animateItem(),
                        onMoveUp = { viewModel.moveExercise(exercises, exercises.indexOf(row), -1) },
                        onMoveDown = { viewModel.moveExercise(exercises, exercises.indexOf(row), 1) },
                        onRemove = { viewModel.removeExercise(row.id) },
                        onDefaultsChange = { sets, reps -> viewModel.updateDefaults(row, sets, reps) },
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteTemplateDialog(
            viewModel = viewModel,
            onDismiss = { showDeleteDialog = false },
            onConfirmed = {
                viewModel.deleteTemplate { navController.popBackStack() }
            },
        )
    }
}

@Composable
private fun DeleteTemplateDialog(
    viewModel: WorkoutBuilderViewModel,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit,
) {
    var referenceCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { referenceCount = viewModel.scheduleReferenceCount() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this workout?") },
        text = {
            val count = referenceCount
            Text(
                if (count == null) {
                    "Checking schedule usage…"
                } else if (count > 0) {
                    "This workout is used in $count schedule slot(s). Deleting it will remove it from those slots too."
                } else {
                    "This workout isn't used in any schedule."
                },
            )
        },
        confirmButton = { TextButton(onClick = onConfirmed) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun TemplateExerciseCard(
    row: TemplateExerciseRow,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onDefaultsChange: (sets: Int?, reps: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sets by remember(row.id) { mutableStateOf(row.targetSets?.toString() ?: "") }
    var reps by remember(row.id) { mutableStateOf(row.targetReps?.toString() ?: "") }

    Card(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        ListItem(
            headlineContent = { Text(row.exerciseName) },
            supportingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = {
                            sets = it
                            onDefaultsChange(it.toIntOrNull(), reps.toIntOrNull())
                        },
                        label = { Text("Sets") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(90.dp),
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = {
                            reps = it
                            onDefaultsChange(sets.toIntOrNull(), it.toIntOrNull())
                        },
                        label = { Text("Reps") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(90.dp),
                    )
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onMoveUp) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up") }
                    IconButton(onClick = onMoveDown) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down") }
                    IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, contentDescription = "Remove") }
                }
            },
        )
    }
}
