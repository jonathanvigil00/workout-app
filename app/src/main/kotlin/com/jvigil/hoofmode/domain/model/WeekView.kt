package com.jvigil.hoofmode.domain.model

import com.jvigil.hoofmode.data.local.dao.ScheduleItemRow
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The rotation item at a given offset from the current pointer (0 = current), looping the
 * sequence indefinitely in either direction. Used to project what a future day *would* be if the
 * user keeps completing one item per day — not a guarantee, since progression only actually
 * advances on explicit completion.
 */
fun itemAtOffset(items: List<ScheduleItemRow>, currentItemId: Long?, offset: Int): ScheduleItemRow? {
    if (items.isEmpty()) return null
    val sorted = items.sortedBy { it.orderIndex }
    val startIndex = sorted.indexOfFirst { it.id == currentItemId }.takeIf { it >= 0 } ?: 0
    val size = sorted.size
    val index = ((startIndex + offset) % size + size) % size
    return sorted[index]
}

/** One day in the Home week strip — either what actually happened, or (for a future day) a projection. */
sealed interface WeekDayEntry {
    val date: LocalDate

    data class Actual(override val date: LocalDate, val day: CalendarDay) : WeekDayEntry

    data class Projected(override val date: LocalDate, val itemType: ScheduleItemType?, val label: String?) : WeekDayEntry
}

/**
 * Builds a full week (any 7 consecutive dates) for the Home screen: days up to and including
 * today show real history (same classification as the History calendar); days after today show
 * a projected rotation item instead, since the schedule isn't bound to calendar days — only
 * completion advances it. The projection assumes one completion per day starting from whichever
 * day is next up (today if it isn't done yet, otherwise tomorrow).
 */
fun buildWeekDayEntries(
    weekDates: List<LocalDate>,
    activitiesByDate: Map<LocalDate, DayActivity>,
    earliestActivityDate: LocalDate?,
    today: LocalDate,
    isCompletedToday: Boolean,
    scheduleItems: List<ScheduleItemRow>,
    currentItemId: Long?,
): List<WeekDayEntry> = weekDates.map { date ->
    if (!date.isAfter(today)) {
        WeekDayEntry.Actual(date, buildCalendarDay(date, activitiesByDate[date], earliestActivityDate, today))
    } else {
        val daysAhead = ChronoUnit.DAYS.between(today, date).toInt()
        val projectedIndex = if (isCompletedToday) daysAhead - 1 else daysAhead
        val item = itemAtOffset(scheduleItems, currentItemId, projectedIndex)
        val label = item?.let { if (it.type == ScheduleItemType.WORKOUT) it.workoutTemplateName ?: "Workout" else "Rest Day" }
        WeekDayEntry.Projected(date, item?.type, label)
    }
}
