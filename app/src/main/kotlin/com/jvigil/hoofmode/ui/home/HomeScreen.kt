package com.jvigil.hoofmode.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.data.local.entity.ActivityType
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import com.jvigil.hoofmode.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val activeState by viewModel.activeState.collectAsState()
    val isCompletedToday by viewModel.isCompletedToday.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoof Mode") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Manage")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Workout templates") }, onClick = {
                            showMenu = false
                            navController.navigate(Screen.WorkoutList.route)
                        })
                        DropdownMenuItem(text = { Text("Schedules") }, onClick = {
                            showMenu = false
                            navController.navigate(Screen.ScheduleList.route)
                        })
                        DropdownMenuItem(text = { Text("PR history") }, onClick = {
                            showMenu = false
                            navController.navigate(Screen.PrHistory.route)
                        })
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Current streak: $streak day${if (streak == 1) "" else "s"}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            val state = activeState
            if (state == null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No active schedule", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Build a schedule and set it active to see what's up next here.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else if (state.currentItem == null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.schedule.name, style = MaterialTheme.typography.titleMedium)
                        Text("This schedule has no items yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (isCompletedToday) {
                val completedType = state.schedule.lastCompletionType
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.schedule.name, style = MaterialTheme.typography.labelLarge)
                        Text(
                            if (completedType == ActivityType.REST) "Rest Day Completed" else "Workout Completed",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            "Nice work. What's up next unlocks after midnight.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            } else {
                val item = state.currentItem
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.schedule.name, style = MaterialTheme.typography.labelLarge)
                        Text(
                            if (item.type == ScheduleItemType.REST) "Rest Day" else item.workoutTemplateName ?: "Workout",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (item.type == ScheduleItemType.REST) {
                            Text(
                                "Marking as completed…",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.startOrResumeCurrentWorkout(item.id) { sessionId ->
                                            navController.navigate(Screen.ActiveSession.route(sessionId))
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                ) { Text("Start Workout") }
                                OutlinedButton(
                                    onClick = { viewModel.completeCurrentItem(state.schedule.id) },
                                    modifier = Modifier.weight(1f),
                                ) { Text("Complete Workout") }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    viewModel.startAdHocSession { sessionId ->
                        navController.navigate(Screen.ActiveSession.route(sessionId))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Start Ad-Hoc Workout") }
        }
    }
}
