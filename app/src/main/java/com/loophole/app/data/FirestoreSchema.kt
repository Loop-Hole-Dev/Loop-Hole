package com.loophole.app.data

/**
 * Issue #4: Firestore data model. See "Firestore data model" in the README.
 *
 *  users/{uid}                     email, createdAt
 *  courses/{courseId}              name, side, difficulty, description, order, keyMoves, lessonCount
 *  lessons/{lessonId}              courseId, order, title, summary, moves[{san, note}]
 *  progress/{uid}_{lessonId}       uid, lessonId, courseId, completedAt, reps, intervalDays,
 *                                  ease, dueAt, lastMistakes, updatedAt
 */
object FirestoreSchema {
    const val USERS = "users"
    const val COURSES = "courses"
    const val LESSONS = "lessons"
    const val PROGRESS = "progress"

    fun progressId(uid: String, lessonId: String) = "${uid}_$lessonId"

    fun courseDoc(c: Course) = mapOf(
        "name" to c.name,
        "side" to c.side,
        "difficulty" to c.difficulty,
        "description" to c.description,
        "order" to c.order,
        "keyMoves" to c.keyMoves,
        "lessonCount" to c.lessons.size,
    )

    fun lessonDoc(l: Lesson) = mapOf(
        "courseId" to l.courseId,
        "order" to l.order,
        "title" to l.title,
        "summary" to l.summary,
        "moves" to l.moves.map { mapOf("san" to it.san, "note" to it.note) },
    )
}
