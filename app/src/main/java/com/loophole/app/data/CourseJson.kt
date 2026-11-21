package com.loophole.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Parses the bundled course files in assets/courses/ (format documented in the README). */
object CourseJson {

    fun parse(text: String): Course {
        val o = JSONObject(text)
        val id = o.getString("id")
        val lessons = o.getJSONArray("lessons").objects().mapIndexed { i, l ->
            Lesson(
                id = l.getString("id"),
                courseId = id,
                order = i + 1,
                title = l.getString("title"),
                summary = l.optString("summary"),
                moves = l.getJSONArray("moves").objects().map { LessonMove(it.getString("san"), it.optString("note")) },
            )
        }
        return Course(
            id = id,
            name = o.getString("name"),
            side = o.getString("side"),
            difficulty = o.getString("difficulty"),
            description = o.getString("description"),
            order = o.optInt("order", 0),
            keyMoves = o.getJSONArray("keyMoves").strings(),
            lessons = lessons,
        )
    }

    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    private fun JSONArray.strings() = (0 until length()).map { getString(it) }
}
