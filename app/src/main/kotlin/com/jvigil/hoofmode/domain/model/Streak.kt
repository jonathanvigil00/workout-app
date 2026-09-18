package com.jvigil.hoofmode.domain.model

import java.time.LocalDate

/**
 * Current streak: consecutive calendar days (ending today or yesterday) that have at least one
 * completed workout or rest day. Today doesn't break the streak until it's over — if today has
 * no activity yet, counting starts from yesterday instead.
 */
fun computeCurrentStreak(activityDates: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
    var cursor = if (today in activityDates) today else today.minusDays(1)
    var streak = 0
    while (cursor in activityDates) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}
