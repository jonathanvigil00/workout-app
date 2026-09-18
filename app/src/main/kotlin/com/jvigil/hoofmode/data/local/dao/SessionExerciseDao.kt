package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jvigil.hoofmode.data.local.entity.SessionExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionExerciseDao {

    @Query(
        """
        SELECT se.id AS id, se.sessionId AS sessionId, se.exerciseId AS exerciseId,
               e.name AS exerciseName, se.orderIndex AS orderIndex, se.isMarkedDone AS isMarkedDone
        FROM session_exercises se
        INNER JOIN exercises e ON e.id = se.exerciseId
        WHERE se.sessionId = :sessionId
        ORDER BY se.orderIndex ASC
        """,
    )
    fun observeForSession(sessionId: Long): Flow<List<SessionExerciseRow>>

    @Query(
        """
        SELECT se.id AS id, se.sessionId AS sessionId, se.exerciseId AS exerciseId,
               e.name AS exerciseName, se.orderIndex AS orderIndex, se.isMarkedDone AS isMarkedDone
        FROM session_exercises se
        INNER JOIN exercises e ON e.id = se.exerciseId
        WHERE se.sessionId = :sessionId
        ORDER BY se.orderIndex ASC
        """,
    )
    suspend fun getForSession(sessionId: Long): List<SessionExerciseRow>

    @Query("SELECT * FROM session_exercises WHERE id = :id")
    suspend fun getById(id: Long): SessionExerciseEntity?

    @Insert
    suspend fun insert(sessionExercise: SessionExerciseEntity): Long

    @Update
    suspend fun update(sessionExercise: SessionExerciseEntity)

    @Update
    suspend fun updateAll(sessionExercises: List<SessionExerciseEntity>)

    @Query("DELETE FROM session_exercises WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun maxOrderIndex(sessionId: Long): Int
}
