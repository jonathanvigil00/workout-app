package com.jvigil.hoofmode.ui.sessiondetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.jvigil.hoofmode.data.local.dao.SessionExerciseRow
import com.jvigil.hoofmode.ui.components.BackIconButton
import com.jvigil.hoofmode.ui.navigation.Screen
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    navController: NavHostController,
    viewModel: SessionDetailViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsState()
    val exercises by viewModel.sessionExercises.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Detail") },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },
                actions = {
                    IconButton(onClick = {
                        viewModel.repeatSession { sessionId -> navController.navigate(Screen.ActiveSession.route(sessionId)) }
                    }) { Icon(Icons.Filled.Refresh, contentDescription = "Repeat workout") }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete session")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            session?.let { s ->
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
                Text(
                    s.startTime.atZone(ZoneId.systemDefault()).format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (s.isAdHoc) Text("Ad-hoc", style = MaterialTheme.typography.bodyMedium)
                s.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
                items(exercises, key = { it.id }) { row ->
                    SessionDetailExerciseCard(row, viewModel)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this session?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSession { navController.popBackStack() } }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SessionDetailExerciseCard(row: SessionExerciseRow, viewModel: SessionDetailViewModel) {
    val sets by remember(row.id) { viewModel.setsFlow(row.id) }.collectAsState(initial = emptyList())

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(row.exerciseName, style = MaterialTheme.typography.titleMedium)
            sets.forEach { set ->
                Text(
                    "Set ${set.setNumber}: ${set.weight} × ${set.reps}" +
                        (set.rpe?.let { " @RPE $it" } ?: "") +
                        (if (set.isPR) " — PR" else ""),
                    style = MaterialTheme.typography.bodyMedium,
                )
                set.note?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
