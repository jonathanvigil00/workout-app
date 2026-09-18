package com.jvigil.hoofmode.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class PhotoTag { FRONT, SIDE, BACK, FLEXED, RELAXED }

@Entity(
    tableName = "progress_photos",
    foreignKeys = [
        ForeignKey(
            entity = BodyWeightEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedBodyWeightEntryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("linkedBodyWeightEntryId"), Index("date")],
)
data class ProgressPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val date: LocalDate,
    val tags: Set<PhotoTag>,
    val note: String?,
    val linkedBodyWeightEntryId: Long?,
)
