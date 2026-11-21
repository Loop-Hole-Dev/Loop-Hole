package com.loophole.app

import com.loophole.app.chess.Position
import com.loophole.app.data.CourseJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Issue #4: every bundled course parses and every lesson move is legal and explained. */
class CourseContentTest {

    private val courses = File("src/main/assets/courses").listFiles { f -> f.extension == "json" }!!
        .map { CourseJson.parse(it.readText()) }

    @Test fun catalogHasCourses() {
        assertTrue(courses.size >= 3)
        assertEquals(courses.size, courses.map { it.id }.toSet().size)
        assertEquals(
            "lesson ids must be unique",
            courses.sumOf { it.lessons.size },
            courses.flatMap { c -> c.lessons.map { it.id } }.toSet().size,
        )
    }

    @Test fun everyLessonIsLegalAndAnnotated() {
        for (course in courses) {
            assertTrue(course.side == "white" || course.side == "black")
            Position.fromSanMoves(course.keyMoves)
            for (lesson in course.lessons) {
                var p = Position.start()
                for ((ply, step) in lesson.moves.withIndex()) {
                    val move = p.parseSan(step.san)
                    assertNotNull("${lesson.id} ply ${ply + 1}: illegal ${step.san} in ${p.toFen()}", move)
                    assertEquals("${lesson.id}: SAN should be canonical", step.san, p.san(move!!))
                    assertFalse("${lesson.id} ply ${ply + 1}: missing note", step.note.isBlank())
                    p = p.play(move)
                }
                val userMoves = lesson.moves.indices.count { (it % 2 == 0) == course.userIsWhite }
                assertTrue("${lesson.id}: too short", userMoves >= 4)
                assertEquals("${lesson.id}: should open with the course's first move",
                    course.keyMoves.first(), lesson.moves.first().san)
            }
        }
    }
}
