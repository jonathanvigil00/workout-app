package com.jvigil.hoofmode.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakTest {

    private val today = LocalDate.of(2026, 9, 8)

    @Test
    fun `no activity is a zero streak`() {
        assertEquals(0, computeCurrentStreak(emptySet(), today))
    }

    @Test
    fun `consecutive days including today count fully`() {
        val dates = setOf(today, today.minusDays(1), today.minusDays(2))
        assertEquals(3, computeCurrentStreak(dates, today))
    }

    @Test
    fun `today with no activity yet does not break a streak still active as of yesterday`() {
        val dates = setOf(today.minusDays(1), today.minusDays(2))
        assertEquals(2, computeCurrentStreak(dates, today))
    }

    @Test
    fun `a gap before yesterday stops the count`() {
        val dates = setOf(today, today.minusDays(1), today.minusDays(3))
        assertEquals(2, computeCurrentStreak(dates, today))
    }

    @Test
    fun `a gap two days ago with no activity yesterday or today is a zero streak`() {
        val dates = setOf(today.minusDays(3))
        assertEquals(0, computeCurrentStreak(dates, today))
    }
}
