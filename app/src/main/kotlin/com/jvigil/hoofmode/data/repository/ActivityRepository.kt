package com.jvigil.hoofmode.data.repository

import com.jvigil.hoofmode.data.local.dao.ActivityLogDao
import com.jvigil.hoofmode.data.local.entity.ActivityType
import com.jvigil.hoofmode.domain.model.DayActivity
import com.jvigil.hoofmode.domain.model.computeCurrentStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(private val dao: ActivityLogDao) {

    /** One entry per day with any completion — a workout takes precedence over a same-day rest completion for display. */
    fun observeDayActivitiesInRange(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, DayActivity>> =
        dao.observeActivityDetailsInRange(from, to).map { rows ->
            rows.groupBy { it.date }.mapValues { (_, dayRows) ->
                val workoutRows = dayRows.filter { it.type == ActivityType.WORKOUT }
                if (workoutRows.isNotEmpty()) {
                    DayActivity(
                        type = ActivityType.WORKOUT,
                        label = workoutRows.joinToString(", ") { it.workoutName ?: "Ad-Hoc Workout" },
                        singleSessionId = workoutRows.singleOrNull()?.sessionId,
                    )
                } else {
                    DayActivity(ActivityType.REST, "Rest Day")
                }
            }
        }

    /** Days before this never count as "missed" — nothing was being tracked yet. */
    fun observeEarliestActivityDate(): Flow<LocalDate?> = dao.observeEarliestDate()

    fun observeCurrentStreak(): Flow<Int> =
        dao.observeAllDates().map { dates -> computeCurrentStreak(dates.toSet()) }

    fun observeWorkoutsThisMonth(monthStart: LocalDate, monthEndExclusive: LocalDate): Flow<Int> =
        dao.observeWorkoutsThisMonth(monthStart, monthEndExclusive)
}
