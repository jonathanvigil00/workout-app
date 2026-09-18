package com.jvigil.hoofmode.data.repository

import com.jvigil.hoofmode.data.local.dao.ExerciseDao
import com.jvigil.hoofmode.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(private val exerciseDao: ExerciseDao) {

    fun observeAll(): Flow<List<ExerciseEntity>> = exerciseDao.observeAll()

    fun search(query: String): Flow<List<ExerciseEntity>> =
        if (query.isBlank()) exerciseDao.observeAll() else exerciseDao.search(query.trim())

    suspend fun getById(id: Long): ExerciseEntity? = exerciseDao.getById(id)

    /** Custom exercises behave identically to built-in ones everywhere (requirement 8.3-8.4). */
    suspend fun createCustom(name: String, muscleGroup: String?, equipment: String?): Long =
        exerciseDao.insert(ExerciseEntity(name = name.trim(), muscleGroup = muscleGroup, equipment = equipment, isCustom = true))
}
