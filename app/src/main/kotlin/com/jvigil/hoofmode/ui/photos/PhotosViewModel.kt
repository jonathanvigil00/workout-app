package com.jvigil.hoofmode.ui.photos

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvigil.hoofmode.data.local.entity.BodyWeightEntryEntity
import com.jvigil.hoofmode.data.local.entity.PhotoTag
import com.jvigil.hoofmode.data.local.entity.ProgressPhotoEntity
import com.jvigil.hoofmode.data.repository.BodyWeightRepository
import com.jvigil.hoofmode.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    bodyWeightRepository: BodyWeightRepository,
) : ViewModel() {

    val photos: StateFlow<List<ProgressPhotoEntity>> =
        photoRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bodyWeightEntries: StateFlow<List<BodyWeightEntryEntity>> =
        bodyWeightRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createCaptureTarget(): Pair<File, Uri> = photoRepository.createCaptureTarget()

    fun importFromUri(uri: Uri, onImported: (File) -> Unit) {
        viewModelScope.launch { onImported(photoRepository.importFromUri(uri)) }
    }

    fun savePhoto(file: File, date: LocalDate, tags: Set<PhotoTag>, note: String?, linkedBodyWeightEntryId: Long?) {
        viewModelScope.launch { photoRepository.savePhoto(file, date, tags, note, linkedBodyWeightEntryId) }
    }

    fun updatePhoto(photo: ProgressPhotoEntity) {
        viewModelScope.launch { photoRepository.updatePhoto(photo) }
    }

    fun deletePhoto(photo: ProgressPhotoEntity) {
        viewModelScope.launch { photoRepository.deletePhoto(photo) }
    }
}
