package com.jvigil.hoofmode.domain.model

import com.jvigil.hoofmode.data.local.dao.SessionExerciseRow
import com.jvigil.hoofmode.data.local.entity.OrderMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionOrderingTest {

    private fun row(id: Long, orderIndex: Int, done: Boolean) =
        SessionExerciseRow(id, sessionId = 1, exerciseId = id, exerciseName = "Ex$id", orderIndex = orderIndex, isMarkedDone = done)

    @Test
    fun `unordered mode is always unlocked regardless of prior completion`() {
        val exercises = listOf(row(1, 0, done = false), row(2, 1, done = false))
        assertTrue(isExerciseUnlocked(exercises, exercises[1], OrderMode.UNORDERED))
    }

    @Test
    fun `enforced mode blocks an exercise until prior ones are done`() {
        val exercises = listOf(row(1, 0, done = false), row(2, 1, done = false))
        assertFalse(isExerciseUnlocked(exercises, exercises[1], OrderMode.ENFORCED))
    }

    @Test
    fun `enforced mode unlocks once all prior exercises are done`() {
        val exercises = listOf(row(1, 0, done = true), row(2, 1, done = false))
        assertTrue(isExerciseUnlocked(exercises, exercises[1], OrderMode.ENFORCED))
    }

    @Test
    fun `enforced mode first exercise is always unlocked`() {
        val exercises = listOf(row(1, 0, done = false), row(2, 1, done = false))
        assertTrue(isExerciseUnlocked(exercises, exercises[0], OrderMode.ENFORCED))
    }

    @Test
    fun `unordered display sinks done exercises to the bottom without touching orderIndex`() {
        val exercises = listOf(row(1, 0, done = true), row(2, 1, done = false), row(3, 2, done = false))
        val sorted = sortedForDisplay(exercises, OrderMode.UNORDERED)
        assertEquals(listOf(2L, 3L, 1L), sorted.map { it.id })
    }

    @Test
    fun `enforced display always stays in sequence order`() {
        val exercises = listOf(row(1, 0, done = true), row(2, 1, done = false))
        val sorted = sortedForDisplay(exercises, OrderMode.ENFORCED)
        assertEquals(listOf(1L, 2L), sorted.map { it.id })
    }
}
