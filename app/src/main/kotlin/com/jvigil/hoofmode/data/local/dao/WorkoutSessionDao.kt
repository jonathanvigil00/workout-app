package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jvigil.hoofmode.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface WorkoutSessionDao {

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun observeAll(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE sourceScheduleItemId = :scheduleItemId AND endTime IS NULL LIMIT 1")
    suspend fun getOpenSessionForScheduleItem(scheduleItemId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE startTime >= :from AND startTime < :to ORDER BY startTime ASC")
    fun observeInRange(from: Instant, to: Instant): Flow<List<WorkoutSessionEntity>>

    @Insert
    suspend fun insert(session: WorkoutSessionEntity): Long

    @Update
    suspend fun update(session: WorkoutSessionEntity)

    @Delete
    suspend fun delete(session: WorkoutSessionEntity)
}
