package com.jvigil.hoofmode.ui.workoutbuilder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.ui.components.BackIconButton
import com.jvigil.hoofmode.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    navController: NavHostController,
    viewModel: WorkoutListViewModel = hiltViewModel(),
) {
    val templates by viewModel.templates.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Templates") },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.WorkoutBuilder.routeForNew()) }) {
                Icon(Icons.Filled.Add, contentDescription = "New workout")
            }
        },
    ) { padding ->
        if (templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No workout templates yet. Tap + to build one.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(templates, key = { it.id }) { template ->
                    Column(modifier = Modifier.animateItem()) {
                        ListItem(
                            headlineContent = { Text(template.name) },
                            supportingContent = { Text(template.orderMode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.fillMaxWidth().clickable {
                                navController.navigate(Screen.WorkoutBuilder.routeForEdit(template.id))
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
