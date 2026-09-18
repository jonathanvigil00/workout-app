package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jvigil.hoofmode.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Insert
    suspend fun insert(exercise: ExerciseEntity): Long

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Query("SELECT COUNT(*) FROM exercises WHERE name = :name COLLATE NOCASE")
    suspend fun countByName(name: String): Int
}
