package com.jvigil.hoofmode.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceScheduleItemId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("sourceScheduleItemId"), Index("startTime")],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Instant,
    val endTime: Instant?,
    val notes: String?,
    val sourceScheduleItemId: Long?,
    val isAdHoc: Boolean,
)
