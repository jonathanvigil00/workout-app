package com.jvigil.hoofmode.domain.model

import com.jvigil.hoofmode.data.local.dao.SessionExerciseRow
import com.jvigil.hoofmode.data.local.entity.OrderMode

/**
 * In ENFORCED mode, an exercise is loggable only once every exercise before it (by orderIndex)
 * is marked done. In UNORDERED mode everything is always loggable — "done" only affects display
 * sort order (see [sortedForDisplay]), never whether logging is allowed.
 */
fun isExerciseUnlocked(
    exercises: List<SessionExerciseRow>,
    target: SessionExerciseRow,
    orderMode: OrderMode,
): Boolean {
    if (orderMode == OrderMode.UNORDERED) return true
    return exercises
        .filter { it.orderIndex < target.orderIndex }
        .all { it.isMarkedDone }
}

/**
 * Unordered sessions sink completed exercises to the bottom without mutating their stored
 * orderIndex (requirement 2.4); enforced sessions always render in strict sequence order.
 */
fun sortedForDisplay(exercises: List<SessionExerciseRow>, orderMode: OrderMode): List<SessionExerciseRow> =
    when (orderMode) {
        OrderMode.UNORDERED -> exercises.sortedWith(compareBy({ it.isMarkedDone }, { it.orderIndex }))
        OrderMode.ENFORCED -> exercises.sortedBy { it.orderIndex }
    }
