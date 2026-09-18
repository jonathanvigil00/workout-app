package com.jvigil.hoofmode.ui.schedulebuilder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.data.local.dao.ScheduleItemRow
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import com.jvigil.hoofmode.ui.components.BackIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleBuilderScreen(
    navController: NavHostController,
    viewModel: ScheduleBuilderViewModel = hiltViewModel(),
) {
    val name by viewModel.name.collectAsState()
    val isActive by viewModel.isActive.collectAsState()
    val items by viewModel.items.collectAsState()
    val templates by viewModel.availableTemplates.collectAsState()
    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule") },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete schedule")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Schedule name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                if (isActive) {
                    AssistChip(onClick = {}, label = { Text("Active schedule") })
                } else {
                    FilledTonalButton(onClick = { viewModel.activate() }) { Text("Set as active") }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(enabled = name.isNotBlank(), onClick = { showAddWorkoutDialog = true }) {
                    Text("+ Workout")
                }
                OutlinedButton(enabled = name.isNotBlank(), onClick = { viewModel.addRestItem() }) {
                    Text("+ Rest Day")
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { item ->
                    ScheduleItemCard(
                        item = item,
                        index = items.indexOf(item),
                        modifier = Modifier.animateItem(),
                        onMoveUp = { viewModel.moveItem(items, items.indexOf(item), -1) },
                        onMoveDown = { viewModel.moveItem(items, items.indexOf(item), 1) },
                        onRemove = { viewModel.removeItem(item.id) },
                    )
                }
            }
        }
    }

    if (showAddWorkoutDialog) {
        AlertDialog(
            onDismissRequest = { showAddWorkoutDialog = false },
            title = { Text("Add workout") },
            text = {
                if (templates.isEmpty()) {
                    Text("No workout templates yet — create one first.")
                } else {
                    LazyColumn {
                        items(templates, key = { it.id }) { template ->
                            ListItem(
                                headlineContent = { Text(template.name) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            TextButton(onClick = {
                                viewModel.addWorkoutItem(template.id)
                                showAddWorkoutDialog = false
                            }) { Text("Add \"${template.name}\"") }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAddWorkoutDialog = false }) { Text("Cancel") } },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this schedule?") },
            text = { Text("This won't affect past session history.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSchedule { navController.popBackStack() } }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ScheduleItemCard(
    item: ScheduleItemRow,
    index: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        ListItem(
            headlineContent = { Text("${index + 1}. ${if (item.type == ScheduleItemType.REST) "Rest Day" else item.workoutTemplateName ?: "Workout"}") },
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
