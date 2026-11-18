package com.loophole.app.data

import com.loophole.app.chess.Position

/** One ply of a lesson: the move in SAN and a short "why" explanation. */
data class LessonMove(val san: String, val note: String)

data class Lesson(
    val id: String,
    val courseId: String,
    val order: Int,
    val title: String,
    val summary: String,
    val moves: List<LessonMove>,
)

data class Course(
    val id: String,
    val name: String,
    /** "white" or "black": the side the user plays in this course. */
    val side: String,
    val difficulty: String,
    val description: String,
    val order: Int,
    /** The moves that define the opening, used for the preview board. */
    val keyMoves: List<String>,
    val lessons: List<Lesson>,
) {
    val userIsWhite get() = side.equals("white", ignoreCase = true)

    /** Position after the opening's defining moves. */
    val previewPosition: Position by lazy { Position.fromSanMoves(keyMoves) }
}
