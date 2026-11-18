package com.loophole.app

import com.loophole.app.data.Sm2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Issue #10: review scheduling. */
class Sm2Test {

    private val fresh = Sm2.Schedule(reps = 0, intervalDays = 0.0, ease = Sm2.DEFAULT_EASE)

    @Test fun intervalsGrow() {
        val first = Sm2.next(fresh, 5)
        assertEquals(1.0, first.intervalDays, 0.0)
        val second = Sm2.next(first, 5)
        assertEquals(6.0, second.intervalDays, 0.0)
        val third = Sm2.next(second, 5)
        assertEquals(6.0 * second.ease, third.intervalDays, 1e-9)
    }

    @Test fun failureResets() {
        val learned = Sm2.next(Sm2.next(fresh, 5), 5)
        val failed = Sm2.next(learned, Sm2.quality(mistakes = 4))
        assertEquals(0, failed.reps)
        assertEquals(0.0, failed.intervalDays, 0.0)
        assertTrue(failed.ease < learned.ease)
    }

    @Test fun easeHasFloor() {
        var s = fresh
        repeat(20) { s = Sm2.next(s, 3) }
        assertTrue(s.ease >= 1.3)
    }

    @Test fun mistakesMapToQuality() {
        assertEquals(5, Sm2.quality(0))
        assertEquals(4, Sm2.quality(1))
        assertEquals(3, Sm2.quality(2))
        assertEquals(2, Sm2.quality(5))
    }
}
