package com.jvigil.hoofmode.domain.prdetection

enum class PrResult { NONE, NEW_PR }

/**
 * Heaviest-weight-only PR comparison (requirement 5.2). Reps are never factored in, and an
 * exercise with no prior history can't produce a PR on its first-ever set.
 */
object PrDetector {
    fun evaluate(newWeight: Double, priorBestWeight: Double?): PrResult {
        if (priorBestWeight == null) return PrResult.NONE
        return if (newWeight > priorBestWeight) PrResult.NEW_PR else PrResult.NONE
    }
}
