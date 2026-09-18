package com.jvigil.hoofmode.domain.model

import com.jvigil.hoofmode.data.local.entity.ActivityType
import java.time.LocalDate

enum class CalendarDayStatus { WORKOUT, REST, MISSED, NONE }

/**
 * What was completed on a given day, if anything — a workout carries its resolved label
 * (template name or "Ad-Hoc Workout") and, when the day had exactly one session, that
 * session's id so the calendar can jump straight to it instead of guessing by date.
 */
data class DayActivity(val type: ActivityType, val label: String, val singleSessionId: Long? = null)

data class CalendarDay(
    val date: LocalDate,
    val status: CalendarDayStatus,
    val label: String?,
    val sessionId: Long? = null,
)

/**
 * Classifies one calendar day for the History view. A day only counts as [CalendarDayStatus.MISSED]
 * if it falls strictly before today AND on/after the first day anything was ever logged — days
 * before tracking began, and today/future days, stay neutral rather than being flagged as missed.
 */
fun buildCalendarDay(
    date: LocalDate,
    activity: DayActivity?,
    earliestActivityDate: LocalDate?,
    today: LocalDate = LocalDate.now(),
): CalendarDay {
    if (activity != null) {
        val status = if (activity.type == ActivityType.WORKOUT) CalendarDayStatus.WORKOUT else CalendarDayStatus.REST
        return CalendarDay(date, status, activity.label, activity.singleSessionId)
    }
    val missed = earliestActivityDate != null && date.isBefore(today) && !date.isBefore(earliestActivityDate)
    return CalendarDay(date, if (missed) CalendarDayStatus.MISSED else CalendarDayStatus.NONE, null)
}
