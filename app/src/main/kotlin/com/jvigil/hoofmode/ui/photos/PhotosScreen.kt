package com.jvigil.hoofmode.ui.photos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jvigil.hoofmode.data.local.entity.PhotoTag
import com.jvigil.hoofmode.data.local.entity.ProgressPhotoEntity
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(viewModel: PhotosViewModel = hiltViewModel()) {
    val photos by viewModel.photos.collectAsState()
    var tagFilter by remember { mutableStateOf<PhotoTag?>(null) }
    var pendingFile by remember { mutableStateOf<File?>(null) }
    var viewingPhoto by remember { mutableStateOf<ProgressPhotoEntity?>(null) }
    var compareMode by remember { mutableStateOf(false) }
    var compareSelection by remember { mutableStateOf<List<ProgressPhotoEntity>>(emptyList()) }

    var pendingCaptureFile by remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingFile = pendingCaptureFile
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importFromUri(it) { file -> pendingFile = file } }
    }

    val visiblePhotos = remember(photos, tagFilter) {
        if (tagFilter == null) photos else photos.filter { tagFilter in it.tags }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Photos") },
                actions = {
                    TextButton(onClick = {
                        compareMode = !compareMode
                        compareSelection = emptyList()
                    }) { Text(if (compareMode) "Cancel Compare" else "Compare") }
                },
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = "Import from gallery")
                }
                FloatingActionButton(onClick = {
                    val (file, uri) = viewModel.createCaptureTarget()
                    pendingCaptureFile = file
                    cameraLauncher.launch(uri)
                }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Take photo")
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item {
                    FilterChip(selected = tagFilter == null, onClick = { tagFilter = null }, label = { Text("All") })
                }
                items(PhotoTag.entries) { tag ->
                    FilterChip(
                        selected = tagFilter == tag,
                        onClick = { tagFilter = if (tagFilter == tag) null else tag },
                        label = { Text(tag.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            if (compareMode && compareSelection.size == 2) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    compareSelection.forEach { photo ->
                        AsyncImage(
                            model = File(photo.filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).aspectRatio(0.8f).padding(2.dp),
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                gridItems(visiblePhotos, key = { it.id }) { photo ->
                    val isSelectedForCompare = photo in compareSelection
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .aspectRatio(1f)
                            .clickable {
                                if (compareMode) {
                                    compareSelection = when {
                                        isSelectedForCompare -> compareSelection - photo
                                        compareSelection.size < 2 -> compareSelection + photo
                                        else -> listOf(compareSelection.last(), photo)
                                    }
                                } else {
                                    viewingPhoto = photo
                                }
                            },
                    ) {
                        AsyncImage(
                            model = File(photo.filePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        if (isSelectedForCompare) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                        }
                    }
                }
            }
        }
    }

    pendingFile?.let { file ->
        SavePhotoDialog(
            viewModel = viewModel,
            file = file,
            onDismiss = { pendingFile = null },
            onSaved = { pendingFile = null },
        )
    }

    viewingPhoto?.let { photo ->
        FullPhotoDialog(photo = photo, onDismiss = { viewingPhoto = null }, onDelete = {
            viewModel.deletePhoto(photo)
            viewingPhoto = null
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavePhotoDialog(
    viewModel: PhotosViewModel,
    file: File,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<PhotoTag>()) }
    val bodyWeightEntries by viewModel.bodyWeightEntries.collectAsState()
    var linkedEntryId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save photo") },
        text = {
            Column {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
                Text("Date: ${date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}", modifier = Modifier.padding(top = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { date = date.minusDays(1) }) { Text("-1 day") }
                    TextButton(onClick = { date = LocalDate.now() }) { Text("Today") }
                    TextButton(onClick = { date = date.plusDays(1) }) { Text("+1 day") }
                }

                Text("Tags", modifier = Modifier.padding(top = 8.dp))
                PhotoTag.entries.forEach { tag ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = tag in selectedTags,
                            onCheckedChange = { checked ->
                                selectedTags = if (checked) selectedTags + tag else selectedTags - tag
                            },
                        )
                        Text(tag.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )

                if (bodyWeightEntries.isNotEmpty()) {
                    Text("Link to a weigh-in (optional)", modifier = Modifier.padding(top = 8.dp))
                    val todayEntry = bodyWeightEntries.firstOrNull { it.date == date }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = linkedEntryId != null && linkedEntryId == todayEntry?.id,
                            onCheckedChange = { checked -> linkedEntryId = if (checked) todayEntry?.id else null },
                            enabled = todayEntry != null,
                        )
                        Text(todayEntry?.let { "${it.weight} on ${it.date}" } ?: "No weigh-in logged for this date")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.savePhoto(file, date, selectedTags, note.takeIf { it.isNotBlank() }, linkedEntryId)
                onSaved()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Discard") } },
    )
}

@Composable
private fun FullPhotoDialog(photo: ProgressPhotoEntity, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(photo.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
        },
        text = {
            Column {
                AsyncImage(
                    model = File(photo.filePath),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().aspectRatio(0.8f),
                )
                photo.note?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                }
                if (photo.tags.isNotEmpty()) {
                    Text(photo.tags.joinToString(", ") { it.name.lowercase() }, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("Delete")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
