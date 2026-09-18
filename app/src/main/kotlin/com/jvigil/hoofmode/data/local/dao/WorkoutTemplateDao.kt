package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {

    @Query("SELECT * FROM workout_templates ORDER BY name ASC")
    fun observeAll(): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getById(id: Long): WorkoutTemplateEntity?

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutTemplateEntity?>

    @Insert
    suspend fun insert(template: WorkoutTemplateEntity): Long

    @Update
    suspend fun update(template: WorkoutTemplateEntity)

    @Delete
    suspend fun delete(template: WorkoutTemplateEntity)

    @Query(
        """
        SELECT wte.id AS id, wte.templateId AS templateId, wte.exerciseId AS exerciseId,
               e.name AS exerciseName, wte.orderIndex AS orderIndex,
               wte.targetSets AS targetSets, wte.targetReps AS targetReps
        FROM workout_template_exercises wte
        INNER JOIN exercises e ON e.id = wte.exerciseId
        WHERE wte.templateId = :templateId
        ORDER BY wte.orderIndex ASC
        """,
    )
    fun observeExercisesForTemplate(templateId: Long): Flow<List<TemplateExerciseRow>>

    @Query(
        """
        SELECT wte.id AS id, wte.templateId AS templateId, wte.exerciseId AS exerciseId,
               e.name AS exerciseName, wte.orderIndex AS orderIndex,
               wte.targetSets AS targetSets, wte.targetReps AS targetReps
        FROM workout_template_exercises wte
        INNER JOIN exercises e ON e.id = wte.exerciseId
        WHERE wte.templateId = :templateId
        ORDER BY wte.orderIndex ASC
        """,
    )
    suspend fun getExercisesForTemplate(templateId: Long): List<TemplateExerciseRow>

    @Insert
    suspend fun insertTemplateExercise(templateExercise: WorkoutTemplateExerciseEntity): Long

    @Update
    suspend fun updateTemplateExercise(templateExercise: WorkoutTemplateExerciseEntity)

    @Query("SELECT * FROM workout_template_exercises WHERE id = :id")
    suspend fun getTemplateExerciseById(id: Long): WorkoutTemplateExerciseEntity?

    @Query("DELETE FROM workout_template_exercises WHERE id = :id")
    suspend fun deleteTemplateExercise(id: Long)

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM workout_template_exercises WHERE templateId = :templateId")
    suspend fun maxOrderIndex(templateId: Long): Int

    @Query("SELECT COUNT(*) FROM schedule_items WHERE workoutTemplateId = :templateId")
    suspend fun countScheduleReferences(templateId: Long): Int
}
