package com.jvigil.hoofmode.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.jvigil.hoofmode.data.local.dao.ProgressPhotoDao
import com.jvigil.hoofmode.data.local.entity.PhotoTag
import com.jvigil.hoofmode.data.local.entity.ProgressPhotoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Photos are written only to the app's private files dir (requirement 4.3) — never to
 * MediaStore/the public gallery. [FileProvider] is used solely to hand the camera intent a
 * writable content:// Uri that resolves back into that same private file.
 */
@Singleton
class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photoDao: ProgressPhotoDao,
) {
    private val photosDir: File
        get() = File(context.filesDir, "photos").apply { mkdirs() }

    fun observeAll(): Flow<List<ProgressPhotoEntity>> = photoDao.observeAll()

    suspend fun getById(id: Long): ProgressPhotoEntity? = photoDao.getById(id)

    /** Creates a new private file and a content:// Uri for the camera intent to write into. */
    fun createCaptureTarget(): Pair<File, Uri> {
        val file = File(photosDir, "${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return file to uri
    }

    /** Copies a gallery-picked image into private storage so the app never depends on an external Uri's lifetime. */
    suspend fun importFromUri(sourceUri: Uri): File {
        val file = File(photosDir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }

    suspend fun savePhoto(
        file: File,
        date: LocalDate,
        tags: Set<PhotoTag>,
        note: String?,
        linkedBodyWeightEntryId: Long?,
    ): Long =
        photoDao.insert(
            ProgressPhotoEntity(
                filePath = file.absolutePath,
                date = date,
                tags = tags,
                note = note,
                linkedBodyWeightEntryId = linkedBodyWeightEntryId,
            ),
        )

    suspend fun updatePhoto(photo: ProgressPhotoEntity) = photoDao.update(photo)

    suspend fun deletePhoto(photo: ProgressPhotoEntity) {
        photoDao.delete(photo)
        runCatching { File(photo.filePath).delete() }
    }
}
