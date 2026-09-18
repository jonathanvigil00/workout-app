package com.jvigil.hoofmode.data.repository

import com.jvigil.hoofmode.data.local.dao.TemplateExerciseRow
import com.jvigil.hoofmode.data.local.dao.WorkoutTemplateDao
import com.jvigil.hoofmode.data.local.entity.OrderMode
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateEntity
import com.jvigil.hoofmode.data.local.entity.WorkoutTemplateExerciseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutTemplateRepository @Inject constructor(private val templateDao: WorkoutTemplateDao) {

    fun observeAll(): Flow<List<WorkoutTemplateEntity>> = templateDao.observeAll()

    fun observeById(id: Long): Flow<WorkoutTemplateEntity?> = templateDao.observeById(id)

    suspend fun getById(id: Long): WorkoutTemplateEntity? = templateDao.getById(id)

    fun observeExercises(templateId: Long): Flow<List<TemplateExerciseRow>> =
        templateDao.observeExercisesForTemplate(templateId)

    suspend fun createTemplate(name: String, orderMode: OrderMode): Long =
        templateDao.insert(WorkoutTemplateEntity(name = name, orderMode = orderMode))

    suspend fun updateTemplate(template: WorkoutTemplateEntity) = templateDao.update(template)

    /** Returns how many schedule slots reference this template, so the UI can warn before deleting. */
    suspend fun countScheduleReferences(templateId: Long): Int = templateDao.countScheduleReferences(templateId)

    /** Cascades to its template-exercise rows and any schedule slots referencing it (2.2). */
    suspend fun deleteTemplate(template: WorkoutTemplateEntity) = templateDao.delete(template)

    suspend fun addExercise(templateId: Long, exerciseId: Long, targetSets: Int?, targetReps: Int?): Long {
        val nextOrder = templateDao.maxOrderIndex(templateId) + 1
        return templateDao.insertTemplateExercise(
            WorkoutTemplateExerciseEntity(
                templateId = templateId,
                exerciseId = exerciseId,
                orderIndex = nextOrder,
                targetSets = targetSets,
                targetReps = targetReps,
            ),
        )
    }

    suspend fun updateExerciseDefaults(row: WorkoutTemplateExerciseEntity) = templateDao.updateTemplateExercise(row)

    suspend fun removeExercise(templateExerciseId: Long) = templateDao.deleteTemplateExercise(templateExerciseId)

    suspend fun reorderExercises(orderedTemplateExerciseIds: List<Long>) {
        orderedTemplateExerciseIds.forEachIndexed { index, id ->
            templateDao.getTemplateExerciseById(id)?.let { templateDao.updateTemplateExercise(it.copy(orderIndex = index)) }
        }
    }
}
