package com.jvigil.hoofmode.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jvigil.hoofmode.data.local.entity.ActivityLogEntity
import com.jvigil.hoofmode.data.local.entity.ActivityType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ActivityLogDao {

    @Insert
    suspend fun insert(entry: ActivityLogEntity): Long

    /**
     * One row per completion in range, with the workout's template name resolved (null for an
     * ad-hoc session or a rest day) — the calendar groups these by date to build each day's label.
     */
    @Query(
        """
        SELECT al.date AS date, al.type AS type, al.sessionId AS sessionId,
               wt.name AS workoutName, ws.isAdHoc AS isAdHoc
        FROM activity_log al
        LEFT JOIN workout_sessions ws ON ws.id = al.sessionId
        LEFT JOIN schedule_items si ON si.id = ws.sourceScheduleItemId
        LEFT JOIN workout_templates wt ON wt.id = si.workoutTemplateId
        WHERE al.date >= :from AND al.date < :to
        ORDER BY al.date ASC
        """,
    )
    fun observeActivityDetailsInRange(from: LocalDate, to: LocalDate): Flow<List<ActivityDetailRow>>

    @Query("SELECT DISTINCT date FROM activity_log ORDER BY date DESC")
    fun observeAllDates(): Flow<List<LocalDate>>

    /** The earliest logged activity — days before this never count as "missed" since tracking hadn't started yet. */
    @Query("SELECT MIN(date) FROM activity_log")
    fun observeEarliestDate(): Flow<LocalDate?>

    @Query(
        """
        SELECT COUNT(DISTINCT date) FROM activity_log
        WHERE type = 'WORKOUT' AND date >= :monthStart AND date < :monthEnd
        """,
    )
    fun observeWorkoutsThisMonth(monthStart: LocalDate, monthEnd: LocalDate): Flow<Int>
}

data class ActivityDetailRow(
    val date: LocalDate,
    val type: ActivityType,
    val sessionId: Long?,
    val workoutName: String?,
    val isAdHoc: Boolean?,
)
