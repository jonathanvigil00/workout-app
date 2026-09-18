package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jvigil.hoofmode.data.local.entity.SetEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface SetEntryDao {

    @Query("SELECT * FROM set_entries WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    fun observeForSessionExercise(sessionExerciseId: Long): Flow<List<SetEntryEntity>>

    @Query("SELECT * FROM set_entries WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    suspend fun getForSessionExercise(sessionExerciseId: Long): List<SetEntryEntity>

    @Insert
    suspend fun insert(set: SetEntryEntity): Long

    @Update
    suspend fun update(set: SetEntryEntity)

    @Query("DELETE FROM set_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COALESCE(MAX(setNumber), 0) FROM set_entries WHERE sessionExerciseId = :sessionExerciseId")
    suspend fun maxSetNumber(sessionExerciseId: Long): Int

    /** The heaviest weight ever logged for an exercise, or null if it has no history. */
    @Query(
        """
        SELECT MAX(s.weight) FROM set_entries s
        INNER JOIN session_exercises sex ON sex.id = s.sessionExerciseId
        WHERE sex.exerciseId = :exerciseId
        """,
    )
    suspend fun getMaxWeight(exerciseId: Long): Double?

    /** Heaviest weight ever logged for an exercise, ignoring one specific set (used when editing that set). */
    @Query(
        """
        SELECT MAX(s.weight) FROM set_entries s
        INNER JOIN session_exercises sex ON sex.id = s.sessionExerciseId
        WHERE sex.exerciseId = :exerciseId AND s.id != :excludeSetId
        """,
    )
    suspend fun getMaxWeightExcluding(exerciseId: Long, excludeSetId: Long): Double?

    /** All-time best set for one exercise: weight, reps at that weight, and when it happened. */
    @Query(
        """
        SELECT ex.id AS exerciseId, ex.name AS exerciseName, s.weight AS weight, s.reps AS reps,
               ws.startTime AS achievedAt
        FROM set_entries s
        INNER JOIN session_exercises sex ON sex.id = s.sessionExerciseId
        INNER JOIN exercises ex ON ex.id = sex.exerciseId
        INNER JOIN workout_sessions ws ON ws.id = sex.sessionId
        WHERE sex.exerciseId = :exerciseId
        ORDER BY s.weight DESC, ws.startTime ASC
        LIMIT 1
        """,
    )
    fun observeBestSet(exerciseId: Long): Flow<BestSetRow?>

    /** All-time best set per exercise, across every exercise that has logged history. */
    @Query(
        """
        SELECT ex.id AS exerciseId, ex.name AS exerciseName, s.weight AS weight, s.reps AS reps,
               ws.startTime AS achievedAt
        FROM set_entries s
        INNER JOIN session_exercises sex ON sex.id = s.sessionExerciseId
        INNER JOIN exercises ex ON ex.id = sex.exerciseId
        INNER JOIN workout_sessions ws ON ws.id = sex.sessionId
        WHERE s.id = (
            SELECT s2.id FROM set_entries s2
            INNER JOIN session_exercises sex2 ON sex2.id = s2.sessionExerciseId
            INNER JOIN workout_sessions ws2 ON ws2.id = sex2.sessionId
            WHERE sex2.exerciseId = sex.exerciseId
            ORDER BY s2.weight DESC, ws2.startTime ASC
            LIMIT 1
        )
        GROUP BY ex.id
        ORDER BY ex.name ASC
        """,
    )
    fun observeAllBestSets(): Flow<List<BestSetRow>>

    /** Heaviest set per session for one exercise, oldest first — feeds the strength progression chart. */
    @Query(
        """
        SELECT ws.startTime AS sessionStart, MAX(s.weight) AS maxWeight
        FROM set_entries s
        INNER JOIN session_exercises sex ON sex.id = s.sessionExerciseId
        INNER JOIN workout_sessions ws ON ws.id = sex.sessionId
        WHERE sex.exerciseId = :exerciseId AND ws.startTime >= :from
        GROUP BY ws.id
        ORDER BY ws.startTime ASC
        """,
    )
    fun observeWeightProgression(exerciseId: Long, from: Instant): Flow<List<WeightPointRow>>

    /** Total volume (weight × reps) per week, oldest first — feeds the volume trend chart. */
    @Query(
        """
        SELECT strftime('%Y-%W', datetime(ws.startTime / 1000, 'unixepoch')) AS week,
               MIN(ws.startTime) AS weekStart,
               SUM(s.weight * s.reps) AS totalVolume
        FROM set_entries s
        INNER JOIN session_exercises sex ON sex.id = s.sessionExerciseId
        INNER JOIN workout_sessions ws ON ws.id = sex.sessionId
        WHERE ws.startTime >= :from
        GROUP BY week
        ORDER BY week ASC
        """,
    )
    fun observeWeeklyVolume(from: Instant): Flow<List<VolumeWeekRow>>
}
