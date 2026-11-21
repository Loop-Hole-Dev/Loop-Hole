package com.loophole.app.data

import kotlin.math.max

/**
 * Issue #10: SM-2 spaced repetition (the SuperMemo-2 algorithm used by Anki).
 * Each learned lesson is one review card.
 */
object Sm2 {

    const val DEFAULT_EASE = 2.5
    private const val MIN_EASE = 1.3

    data class Schedule(val reps: Int, val intervalDays: Double, val ease: Double)

    /** Converts mistakes in a review into an SM-2 quality grade (0..5). Below 3 counts as a fail. */
    fun quality(mistakes: Int) = when (mistakes) {
        0 -> 5
        1 -> 4
        2 -> 3
        else -> 2
    }

    fun next(current: Schedule, quality: Int): Schedule {
        val q = quality.coerceIn(0, 5)
        if (q < 3) return Schedule(reps = 0, intervalDays = 0.0, ease = max(MIN_EASE, current.ease - 0.2))
        val reps = current.reps + 1
        val interval = when (reps) {
            1 -> 1.0
            2 -> 6.0
            else -> current.intervalDays * current.ease
        }
        val ease = max(MIN_EASE, current.ease + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
        return Schedule(reps, interval, ease)
    }
}
