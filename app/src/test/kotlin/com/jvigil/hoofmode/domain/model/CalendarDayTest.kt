package com.jvigil.hoofmode.domain.model

import com.jvigil.hoofmode.data.local.entity.ActivityType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalendarDayTest {

    private val today = LocalDate.of(2026, 9, 9)

    @Test
    fun `a day with a completed workout is WORKOUT with its label`() {
        val result = buildCalendarDay(
            date = today.minusDays(1),
            activity = DayActivity(ActivityType.WORKOUT, "Push Day A"),
            earliestActivityDate = today.minusDays(10),
            today = today,
        )
        assertEquals(CalendarDayStatus.WORKOUT, result.status)
        assertEquals("Push Day A", result.label)
    }

    @Test
    fun `a day with a completed rest day is REST`() {
        val result = buildCalendarDay(
            date = today.minusDays(1),
            activity = DayActivity(ActivityType.REST, "Rest Day"),
            earliestActivityDate = today.minusDays(10),
            today = today,
        )
        assertEquals(CalendarDayStatus.REST, result.status)
    }

    @Test
    fun `a past gap day after tracking began is MISSED`() {
        val result = buildCalendarDay(
            date = today.minusDays(2),
            activity = null,
            earliestActivityDate = today.minusDays(10),
            today = today,
        )
        assertEquals(CalendarDayStatus.MISSED, result.status)
    }

    @Test
    fun `today with no activity yet is NOT missed since the day isn't over`() {
        val result = buildCalendarDay(
            date = today,
            activity = null,
            earliestActivityDate = today.minusDays(10),
            today = today,
        )
        assertEquals(CalendarDayStatus.NONE, result.status)
    }

    @Test
    fun `a future day is never missed`() {
        val result = buildCalendarDay(
            date = today.plusDays(3),
            activity = null,
            earliestActivityDate = today.minusDays(10),
            today = today,
        )
        assertEquals(CalendarDayStatus.NONE, result.status)
    }

    @Test
    fun `a gap day before tracking ever began is not missed`() {
        val result = buildCalendarDay(
            date = today.minusDays(20),
            activity = null,
            earliestActivityDate = today.minusDays(10),
            today = today,
        )
        assertEquals(CalendarDayStatus.NONE, result.status)
    }

    @Test
    fun `no activity ever logged means nothing is ever missed`() {
        val result = buildCalendarDay(
            date = today.minusDays(1),
            activity = null,
            earliestActivityDate = null,
            today = today,
        )
        assertEquals(CalendarDayStatus.NONE, result.status)
    }

    @Test
    fun `the earliest tracked day itself can be missed if nothing was logged that day`() {
        val earliest = today.minusDays(5)
        val result = buildCalendarDay(
            date = earliest,
            activity = null,
            earliestActivityDate = earliest,
            today = today,
        )
        assertEquals(CalendarDayStatus.MISSED, result.status)
    }
}
