package com.jvigil.hoofmode.data.local.dao

import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import java.time.Instant

/** A template's exercise row joined with the exercise's display name. */
data class TemplateExerciseRow(
    val id: Long,
    val templateId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val orderIndex: Int,
    val targetSets: Int?,
    val targetReps: Int?,
)

/** A schedule item joined with the workout template's name (null for rest items). */
data class ScheduleItemRow(
    val id: Long,
    val scheduleId: Long,
    val orderIndex: Int,
    val type: ScheduleItemType,
    val workoutTemplateId: Long?,
    val workoutTemplateName: String?,
)

/** A session's exercise row joined with the exercise's display name. */
data class SessionExerciseRow(
    val id: Long,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val orderIndex: Int,
    val isMarkedDone: Boolean,
)

/** All-time best set for an exercise: heaviest weight, the reps done at that weight, and when. */
data class BestSetRow(
    val exerciseId: Long,
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val achievedAt: Instant,
)

/** One session's heaviest set for an exercise, used to plot strength progression over time. */
data class WeightPointRow(
    val sessionStart: Instant,
    val maxWeight: Double,
)

/** Total volume (weight × reps) for one ISO-ish week, used for the volume trend chart. */
data class VolumeWeekRow(
    val week: String,
    val weekStart: Instant,
    val totalVolume: Double,
)
