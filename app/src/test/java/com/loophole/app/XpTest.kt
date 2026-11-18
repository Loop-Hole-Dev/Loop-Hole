package com.loophole.app

import com.loophole.app.data.Xp
import org.junit.Assert.assertEquals
import org.junit.Test

/** Issue #13: level thresholds. */
class XpTest {
    @Test fun levels() {
        assertEquals(1, Xp.levelFor(0))
        assertEquals(1, Xp.levelFor(99))
        assertEquals(2, Xp.levelFor(100))
        assertEquals(2, Xp.levelFor(299))
        assertEquals(3, Xp.levelFor(300))
        assertEquals(5, Xp.levelFor(1000))
    }

    @Test fun firstLessonWithNoMistakesReachesLevelTwo() {
        assertEquals(2, Xp.levelFor(Xp.LESSON_FIRST_TIME + Xp.PERFECT_BONUS + Xp.REVIEW + Xp.PERFECT_BONUS))
    }
}
