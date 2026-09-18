package com.jvigil.hoofmode.domain.prdetection

import org.junit.Assert.assertEquals
import org.junit.Test

class PrDetectorTest {

    @Test
    fun `no prior history is never a PR`() {
        assertEquals(PrResult.NONE, PrDetector.evaluate(newWeight = 135.0, priorBestWeight = null))
    }

    @Test
    fun `heavier weight is a new PR`() {
        assertEquals(PrResult.NEW_PR, PrDetector.evaluate(newWeight = 140.0, priorBestWeight = 135.0))
    }

    @Test
    fun `equal weight is not a new PR`() {
        assertEquals(PrResult.NONE, PrDetector.evaluate(newWeight = 135.0, priorBestWeight = 135.0))
    }

    @Test
    fun `lighter weight is not a new PR`() {
        assertEquals(PrResult.NONE, PrDetector.evaluate(newWeight = 130.0, priorBestWeight = 135.0))
    }

    @Test
    fun `more reps at a lower weight is never a PR`() {
        // Heaviest-weight-only comparison: reps are not a factor at all.
        assertEquals(PrResult.NONE, PrDetector.evaluate(newWeight = 95.0, priorBestWeight = 135.0))
    }
}
