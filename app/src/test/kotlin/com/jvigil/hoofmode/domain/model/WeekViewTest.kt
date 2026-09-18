package com.jvigil.hoofmode.domain.model

import com.jvigil.hoofmode.data.local.dao.ScheduleItemRow
import com.jvigil.hoofmode.data.local.entity.ActivityType
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class WeekViewTest {

    private fun item(id: Long, orderIndex: Int, name: String?) = ScheduleItemRow(
        id = id,
        scheduleId = 1,
        orderIndex = orderIndex,
        type = if (name == null) ScheduleItemType.REST else ScheduleItemType.WORKOUT,
        workoutTemplateId = if (name == null) null else id,
        workoutTemplateName = name,
    )

    // --- itemAtOffset ---

    @Test
    fun `empty schedule has no item at any offset`() {
        assertNull(itemAtOffset(emptyList(), currentItemId = 1L, offset = 0))
    }

    @Test
    fun `offset wraps forward past the end of the sequence`() {
        val items = listOf(item(1, 0, "Push"), item(2, 1, null))
        assertEquals("Push", itemAtOffset(items, currentItemId = 2L, offset = 1)?.workoutTemplateName)
    }

    @Test
    fun `negative offset wraps backward`() {
        val items = listOf(item(1, 0, "Push"), item(2, 1, null), item(3, 2, "Pull"))
        assertEquals("Pull", itemAtOffset(items, currentItemId = 1L, offset = -1)?.workoutTemplateName)
    }

    // --- buildWeekDayEntries ---

    private val today = LocalDate.of(2026, 9, 17)
    private val schedule = listOf(item(1, 0, "Push"), item(2, 1, null), item(3, 2, "Pull"))

    @Test
    fun `a past day with logged activity is Actual with that label`() {
        val yesterday = today.minusDays(1)
        val entries = buildWeekDayEntries(
            weekDates = listOf(yesterday),
            activitiesByDate = mapOf(yesterday to DayActivity(ActivityType.WORKOUT, "Push")),
            earliestActivityDate = yesterday,
            today = today,
            isCompletedToday = false,
            scheduleItems = schedule,
            currentItemId = 1L,
        )
        val entry = entries.single() as WeekDayEntry.Actual
        assertEquals(CalendarDayStatus.WORKOUT, entry.day.status)
        assertEquals("Push", entry.day.label)
    }

    @Test
    fun `today not yet completed is Actual and reflects real history status`() {
        val entries = buildWeekDayEntries(
            weekDates = listOf(today),
            activitiesByDate = emptyMap(),
            earliestActivityDate = today.minusDays(5),
            today = today,
            isCompletedToday = false,
            scheduleItems = schedule,
            currentItemId = 1L,
        )
        val entry = entries.single() as WeekDayEntry.Actual
        assertEquals(CalendarDayStatus.NONE, entry.day.status)
    }

    @Test
    fun `tomorrow projects one step past current when today is not yet completed`() {
        // Today's pending target IS the current item (Push, id 1), so tomorrow is the next
        // item after it (Rest) — not a repeat of today's own target.
        val tomorrow = today.plusDays(1)
        val entries = buildWeekDayEntries(
            weekDates = listOf(tomorrow),
            activitiesByDate = emptyMap(),
            earliestActivityDate = null,
            today = today,
            isCompletedToday = false,
            scheduleItems = schedule,
            currentItemId = 1L,
        )
        val entry = entries.single() as WeekDayEntry.Projected
        assertEquals("Rest Day", entry.label)
    }

    @Test
    fun `tomorrow projects the item after current when today is already completed`() {
        // Pointer already advanced to Rest (id 2) after completing Push today, so tomorrow's
        // projection should be the pointer's current item itself, not one step past it.
        val tomorrow = today.plusDays(1)
        val entries = buildWeekDayEntries(
            weekDates = listOf(tomorrow),
            activitiesByDate = mapOf(today to DayActivity(ActivityType.WORKOUT, "Push")),
            earliestActivityDate = today,
            today = today,
            isCompletedToday = true,
            scheduleItems = schedule,
            currentItemId = 2L,
        )
        val entry = entries.single() as WeekDayEntry.Projected
        assertEquals("Rest Day", entry.label)
    }

    @Test
    fun `a projected rest day has the Rest Day label`() {
        // Today already completed and the pointer now sits on Rest (id 2), so tomorrow's
        // projection is the pointer's own item, at offset 0.
        val tomorrow = today.plusDays(1)
        val entries = buildWeekDayEntries(
            weekDates = listOf(tomorrow),
            activitiesByDate = mapOf(today to DayActivity(ActivityType.WORKOUT, "Push")),
            earliestActivityDate = today,
            today = today,
            isCompletedToday = true,
            scheduleItems = schedule,
            currentItemId = 2L,
        )
        val entry = entries.single() as WeekDayEntry.Projected
        assertEquals("Rest Day", entry.label)
    }

    @Test
    fun `projection loops the rotation across multiple future days`() {
        val dates = (1..3).map { today.plusDays(it.toLong()) }
        val entries = buildWeekDayEntries(
            weekDates = dates,
            activitiesByDate = emptyMap(),
            earliestActivityDate = null,
            today = today,
            isCompletedToday = false,
            scheduleItems = schedule,
            currentItemId = 1L,
        )
        val labels = entries.map { (it as WeekDayEntry.Projected).label }
        assertEquals(listOf("Rest Day", "Pull", "Push"), labels)
    }

    @Test
    fun `no active schedule yields a blank projection instead of crashing`() {
        val tomorrow = today.plusDays(1)
        val entries = buildWeekDayEntries(
            weekDates = listOf(tomorrow),
            activitiesByDate = emptyMap(),
            earliestActivityDate = null,
            today = today,
            isCompletedToday = false,
            scheduleItems = emptyList(),
            currentItemId = null,
        )
        val entry = entries.single() as WeekDayEntry.Projected
        assertNull(entry.label)
        assertNull(entry.itemType)
    }
}
