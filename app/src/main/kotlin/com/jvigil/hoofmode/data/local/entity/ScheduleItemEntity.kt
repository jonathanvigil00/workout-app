package com.jvigil.hoofmode.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ScheduleItemType { WORKOUT, REST }

@Entity(
    tableName = "schedule_items",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutTemplateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("scheduleId"), Index("workoutTemplateId")],
)
data class ScheduleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: Long,
    val orderIndex: Int,
    val type: ScheduleItemType,
    val workoutTemplateId: Long?,
)
