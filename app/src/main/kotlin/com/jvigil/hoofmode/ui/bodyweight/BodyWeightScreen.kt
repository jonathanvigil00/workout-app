package com.jvigil.hoofmode.ui.bodyweight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jvigil.hoofmode.data.local.entity.BodyWeightEntryEntity
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyWeightScreen(viewModel: BodyWeightViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    val unit by viewModel.weightUnit.collectAsState()
    var editingEntry by remember { mutableStateOf<BodyWeightEntryEntity?>(null) }

    var quickWeight by remember { mutableStateOf("") }
    var quickNote by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Body Weight") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Log today's weight", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = quickWeight,
                            onValueChange = { quickWeight = it },
                            label = { Text("Weight (${unit.name.lowercase()})") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = {
                            quickWeight.toDoubleOrNull()?.let {
                                viewModel.logToday(it, quickNote)
                                quickWeight = ""
                                quickNote = ""
                            }
                        }) { Text("Log") }
                    }
                    OutlinedTextField(
                        value = quickNote,
                        onValueChange = { quickNote = it },
                        label = { Text("Note (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
                items(entries, key = { it.id }) { entry ->
                    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d")
                    Column(modifier = Modifier.animateItem()) {
                        ListItem(
                            headlineContent = { Text("${entry.weight} ${unit.name.lowercase()} — ${entry.date.format(formatter)}") },
                            supportingContent = entry.note?.takeIf { it.isNotBlank() }?.let { { Text(it) } },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { editingEntry = entry }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { viewModel.deleteEntry(entry) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                    }
                                }
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onSave = { updated ->
                viewModel.updateEntry(updated)
                editingEntry = null
            },
        )
    }
}

@Composable
private fun EditEntryDialog(
    entry: BodyWeightEntryEntity,
    onDismiss: () -> Unit,
    onSave: (BodyWeightEntryEntity) -> Unit,
) {
    var weight by remember { mutableStateOf(entry.weight.toString()) }
    var note by remember { mutableStateOf(entry.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                weight.toDoubleOrNull()?.let { onSave(entry.copy(weight = it, note = note.takeIf { n -> n.isNotBlank() })) }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
