package com.jvigil.hoofmode.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class ActivityType { WORKOUT, REST }

/**
 * One row per completed workout or rest day, used to drive the calendar/history markers and
 * streak calculation without unioning [WorkoutSessionEntity] with a separate rest-completion
 * table. A [WORKOUT] row disappears if its session is deleted; a [REST] row survives edits to
 * the schedule it came from (the schedule item link just goes null) since it's a historical fact.
 */
@Entity(
    tableName = "activity_log",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ScheduleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleItemId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("date"), Index("sessionId"), Index("scheduleItemId")],
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: ActivityType,
    val sessionId: Long?,
    val scheduleItemId: Long?,
)
