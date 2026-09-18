package com.jvigil.hoofmode.ui.exerciselibrary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.data.local.entity.ExerciseEntity
import com.jvigil.hoofmode.ui.navigation.SELECTED_EXERCISE_ID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    navController: NavHostController,
    viewModel: ExerciseLibraryViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    var muscleGroupFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val muscleGroups = remember(exercises) { exercises.mapNotNull { it.muscleGroup }.distinct().sorted() }
    val visibleExercises = remember(exercises, muscleGroupFilter) {
        if (muscleGroupFilter == null) exercises else exercises.filter { it.muscleGroup == muscleGroupFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercise Library") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Create custom exercise")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search exercises") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (muscleGroups.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    item {
                        FilterChip(
                            selected = muscleGroupFilter == null,
                            onClick = { muscleGroupFilter = null },
                            label = { Text("All") },
                        )
                    }
                    items(muscleGroups) { group ->
                        FilterChip(
                            selected = muscleGroupFilter == group,
                            onClick = { muscleGroupFilter = if (muscleGroupFilter == group) null else group },
                            label = { Text(group) },
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                items(visibleExercises, key = { it.id }) { exercise ->
                    Column(modifier = Modifier.animateItem()) {
                        ExerciseRow(
                            exercise = exercise,
                            onClick = {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(SELECTED_EXERCISE_ID, exercise.id)
                                navController.popBackStack()
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateExerciseDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, muscleGroup, equipment ->
                viewModel.createCustomExercise(name, muscleGroup, equipment) { id ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(SELECTED_EXERCISE_ID, id)
                    navController.popBackStack()
                }
                showCreateDialog = false
            },
        )
    }
}

@Composable
private fun ExerciseRow(exercise: ExerciseEntity, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(exercise.name) },
        supportingContent = {
            val tags = listOfNotNull(exercise.muscleGroup, exercise.equipment)
            if (tags.isNotEmpty()) Text(tags.joinToString(" · "))
        },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    )
}

@Composable
private fun CreateExerciseDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, muscleGroup: String?, equipment: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New exercise") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    value = muscleGroup,
                    onValueChange = { muscleGroup = it },
                    label = { Text("Muscle group (optional)") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = equipment,
                    onValueChange = { equipment = it },
                    label = { Text("Equipment (optional)") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name, muscleGroup, equipment) }, enabled = name.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
