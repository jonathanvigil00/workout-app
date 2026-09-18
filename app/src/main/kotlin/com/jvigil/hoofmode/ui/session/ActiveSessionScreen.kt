package com.jvigil.hoofmode.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.data.local.dao.SessionExerciseRow
import com.jvigil.hoofmode.data.local.entity.OrderMode
import com.jvigil.hoofmode.data.local.entity.SetEntryEntity
import com.jvigil.hoofmode.domain.model.isExerciseUnlocked
import com.jvigil.hoofmode.domain.model.sortedForDisplay
import com.jvigil.hoofmode.domain.prdetection.PrResult
import com.jvigil.hoofmode.ui.components.BackIconButton
import com.jvigil.hoofmode.ui.navigation.SELECTED_EXERCISE_ID
import com.jvigil.hoofmode.ui.navigation.Screen
import com.jvigil.hoofmode.ui.resttimer.RestTimerState
import com.jvigil.hoofmode.ui.theme.HoofPrBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    navController: NavHostController,
    viewModel: ActiveSessionViewModel = hiltViewModel(),
) {
    val exercises by viewModel.sessionExercises.collectAsState()
    val orderMode by viewModel.orderMode.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val session by viewModel.session.collectAsState()
    val restTimerState by viewModel.restTimerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<Long?>(SELECTED_EXERCISE_ID, null)?.collect { id ->
            if (id != null) {
                viewModel.addExercise(id)
                savedStateHandle.remove<Long>(SELECTED_EXERCISE_ID)
            }
        }
    }

    val sorted = remember(exercises, orderMode) { sortedForDisplay(exercises, orderMode) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (session?.isAdHoc == true) "Ad-Hoc Workout" else "Workout Session") },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },
                actions = {
                    TextButton(onClick = { viewModel.endSession { navController.popBackStack() } }) {
                        Text("End Session")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).animateContentSize()) {
            if (restTimerState is RestTimerState.Running) {
                val running = restTimerState as RestTimerState.Running
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Rest: ${running.secondsRemaining}s", style = MaterialTheme.typography.labelLarge)
                        LinearProgressIndicator(
                            progress = { running.secondsRemaining / running.totalSeconds.toFloat() },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Session notes") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )

            FilledTonalButton(
                onClick = { navController.navigate(Screen.ExerciseLibrary.route) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            ) { Text("+ Add Exercise") }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sorted, key = { it.id }) { row ->
                    val unlocked = isExerciseUnlocked(exercises, row, orderMode)
                    ExerciseSessionCard(
                        row = row,
                        unlocked = unlocked,
                        viewModel = viewModel,
                        modifier = Modifier.animateItem(),
                        onMoveUp = { viewModel.moveExercise(sorted, sorted.indexOf(row), -1) },
                        onMoveDown = { viewModel.moveExercise(sorted, sorted.indexOf(row), 1) },
                        onRemove = { viewModel.removeExercise(row.id) },
                        onPr = {
                            scope.launch { snackbarHostState.showSnackbar("New PR on ${row.exerciseName}!") }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseSessionCard(
    row: SessionExerciseRow,
    unlocked: Boolean,
    viewModel: ActiveSessionViewModel,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    onPr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bestSet by remember(row.exerciseId) { viewModel.bestSetFlow(row.exerciseId) }.collectAsState(initial = null)
    val sets by remember(row.id) { viewModel.setsFlow(row.id) }.collectAsState(initial = emptyList())

    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp).animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row {
                    Checkbox(checked = row.isMarkedDone, onCheckedChange = { viewModel.setMarkedDone(row.id, it) })
                    Text(row.exerciseName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                }
                Row {
                    IconButton(onClick = onMoveUp) { Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up") }
                    IconButton(onClick = onMoveDown) { Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down") }
                    IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, contentDescription = "Remove") }
                }
            }

            if (!unlocked) {
                Text("Locked until prior exercises are marked done.", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            bestSet?.let {
                Text(
                    "Best ever: ${it.weight}× ${it.reps} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            sets.forEach { set ->
                SetRow(set = set, onUpdate = { viewModel.updateSet(it) }, onDelete = { viewModel.deleteSet(set.id) })
            }

            AddSetRow(
                onLog = { weight, reps, rpe, note ->
                    viewModel.logSet(row.id, weight, reps, rpe, note) { result ->
                        if (result == PrResult.NEW_PR) onPr()
                    }
                },
            )
        }
    }
}

@Composable
private fun SetRow(set: SetEntryEntity, onUpdate: (SetEntryEntity) -> Unit, onDelete: () -> Unit) {
    var weight by remember(set.id) { mutableStateOf(set.weight.toString()) }
    var reps by remember(set.id) { mutableStateOf(set.reps.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Set ${set.setNumber}", modifier = Modifier.width(52.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                it.toDoubleOrNull()?.let { w -> onUpdate(set.copy(weight = w)) }
            },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(80.dp),
        )
        OutlinedTextField(
            value = reps,
            onValueChange = {
                reps = it
                it.toIntOrNull()?.let { r -> onUpdate(set.copy(reps = r)) }
            },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(70.dp),
        )
        AnimatedVisibility(visible = set.isPR, enter = scaleIn() + fadeIn()) {
            Text("PR", color = HoofPrBadge, style = MaterialTheme.typography.labelLarge)
        }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = "Delete set") }
    }
}

@Composable
private fun AddSetRow(onLog: (weight: Double, reps: Int, rpe: Int?, note: String?) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(90.dp),
        )
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            label = { Text("Reps") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(80.dp),
        )
        FilledTonalButton(
            onClick = {
                val w = weight.toDoubleOrNull()
                val r = reps.toIntOrNull()
                if (w != null && r != null) {
                    onLog(w, r, null, null)
                    weight = ""
                    reps = ""
                }
            },
        ) { Text("Log Set") }
    }
}
