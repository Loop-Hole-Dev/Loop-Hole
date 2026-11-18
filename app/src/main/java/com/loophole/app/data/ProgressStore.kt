package com.loophole.app.data

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.loophole.app.auth.AuthManager
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class LessonProgress(
    val lessonId: String,
    val courseId: String,
    val completedAt: Long,
    val reps: Int,
    val intervalDays: Double,
    val ease: Double,
    val dueAt: Long,
    val lastMistakes: Int,
    val updatedAt: Long,
) {
    val schedule get() = Sm2.Schedule(reps, intervalDays, ease)
}

/**
 * Lesson completion and review scheduling. Saved on the device so guests keep their progress,
 * and mirrored to the Firestore `progress` collection when the user is signed in.
 */
object ProgressStore {

    private const val PREFS = "progress"
    private const val KEY_ACTIVE_DAYS = "active_days"
    private const val LESSON_PREFIX = "lesson:"
    private val FAILED_RETRY_MS = TimeUnit.MINUTES.toMillis(10)

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun all(context: Context): Map<String, LessonProgress> =
        prefs(context).all.entries
            .filter { it.key.startsWith(LESSON_PREFIX) }
            .mapNotNull { (it.value as? String)?.let(::decode) }
            .associateBy { it.lessonId }

    fun get(context: Context, lessonId: String): LessonProgress? =
        prefs(context).getString(LESSON_PREFIX + lessonId, null)?.let(::decode)

    fun isCompleted(context: Context, lessonId: String) = get(context, lessonId) != null

    fun completedCount(context: Context, course: Course): Int {
        val done = all(context)
        return course.lessons.count { it.id in done }
    }

    /** Marks a lesson learned. New lessons go straight into the review queue to lock them in. */
    fun completeLesson(context: Context, lesson: Lesson): LessonProgress {
        markActiveToday(context)
        get(context, lesson.id)?.let { return it }
        val now = System.currentTimeMillis()
        val progress = LessonProgress(
            lessonId = lesson.id,
            courseId = lesson.courseId,
            completedAt = now,
            reps = 0,
            intervalDays = 0.0,
            ease = Sm2.DEFAULT_EASE,
            dueAt = now,
            lastMistakes = 0,
            updatedAt = now,
        )
        save(context, progress)
        return progress
    }

    /** Applies SM-2 to a finished review and returns the new schedule. */
    fun recordReview(context: Context, lesson: Lesson, mistakes: Int): LessonProgress {
        markActiveToday(context)
        val now = System.currentTimeMillis()
        val current = get(context, lesson.id) ?: completeLesson(context, lesson)
        val next = Sm2.next(current.schedule, Sm2.quality(mistakes))
        val dueAt = if (next.intervalDays == 0.0) now + FAILED_RETRY_MS
        else now + (next.intervalDays * TimeUnit.DAYS.toMillis(1)).toLong()
        val updated = current.copy(
            reps = next.reps,
            intervalDays = next.intervalDays,
            ease = next.ease,
            dueAt = dueAt,
            lastMistakes = mistakes,
            updatedAt = now,
        )
        save(context, updated)
        return updated
    }

    fun dueNow(context: Context, now: Long = System.currentTimeMillis()): List<LessonProgress> =
        all(context).values.filter { it.dueAt <= now }.sortedBy { it.dueAt }

    fun dueTodayCount(context: Context): Int {
        val endOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }.timeInMillis
        return all(context).values.count { it.dueAt <= endOfToday }
    }

    fun nextDue(context: Context): LessonProgress? = all(context).values.minByOrNull { it.dueAt }

    /** Consecutive days with a finished lesson or review, ending today (or yesterday). */
    fun streak(context: Context): Int {
        val days = prefs(context).getStringSet(KEY_ACTIVE_DAYS, emptySet()).orEmpty()
        val cal = Calendar.getInstance()
        if (dayKey(cal.time) !in days) cal.add(Calendar.DAY_OF_YEAR, -1)
        var count = 0
        while (dayKey(cal.time) in days) {
            count++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return count
    }

    fun activeToday(context: Context) =
        dayKey(Date()) in prefs(context).getStringSet(KEY_ACTIVE_DAYS, emptySet()).orEmpty()

    private fun markActiveToday(context: Context) {
        val days = prefs(context).getStringSet(KEY_ACTIVE_DAYS, emptySet()).orEmpty()
        prefs(context).edit { putStringSet(KEY_ACTIVE_DAYS, days + dayKey(Date())) }
    }

    private fun dayKey(date: Date) = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    private fun save(context: Context, p: LessonProgress) {
        prefs(context).edit { putString(LESSON_PREFIX + p.lessonId, encode(p)) }
        pushToFirestore(context, p)
    }

    // ---- Firestore sync ----

    private fun currentUid(context: Context): String? =
        if (AuthManager.isFirebaseReady(context)) FirebaseAuth.getInstance().currentUser?.uid else null

    private fun pushToFirestore(context: Context, p: LessonProgress) {
        val uid = currentUid(context) ?: return
        FirebaseFirestore.getInstance()
            .collection(FirestoreSchema.PROGRESS)
            .document(FirestoreSchema.progressId(uid, p.lessonId))
            .set(
                mapOf(
                    "uid" to uid,
                    "lessonId" to p.lessonId,
                    "courseId" to p.courseId,
                    "completedAt" to p.completedAt,
                    "reps" to p.reps,
                    "intervalDays" to p.intervalDays,
                    "ease" to p.ease,
                    "dueAt" to p.dueAt,
                    "lastMistakes" to p.lastMistakes,
                    "updatedAt" to p.updatedAt,
                )
            )
    }

    /** Pulls the signed-in user's progress and keeps whichever copy of each lesson is newer. */
    fun syncFromFirestore(context: Context, onDone: () -> Unit) {
        val uid = currentUid(context) ?: return
        val appContext = context.applicationContext
        FirebaseFirestore.getInstance().collection(FirestoreSchema.PROGRESS)
            .whereEqualTo("uid", uid).get()
            .addOnSuccessListener { snap ->
                val local = all(appContext)
                var changed = false
                for (doc in snap.documents) {
                    val remote = LessonProgress(
                        lessonId = doc.getString("lessonId") ?: continue,
                        courseId = doc.getString("courseId").orEmpty(),
                        completedAt = doc.getLong("completedAt") ?: 0,
                        reps = doc.getLong("reps")?.toInt() ?: 0,
                        intervalDays = doc.getDouble("intervalDays") ?: 0.0,
                        ease = doc.getDouble("ease") ?: Sm2.DEFAULT_EASE,
                        dueAt = doc.getLong("dueAt") ?: 0,
                        lastMistakes = doc.getLong("lastMistakes")?.toInt() ?: 0,
                        updatedAt = doc.getLong("updatedAt") ?: 0,
                    )
                    val mine = local[remote.lessonId]
                    if (mine == null || remote.updatedAt > mine.updatedAt) {
                        prefs(appContext).edit { putString(LESSON_PREFIX + remote.lessonId, encode(remote)) }
                        changed = true
                    }
                }
                if (changed) onDone()
            }
    }

    // ---- Local encoding ----

    private fun encode(p: LessonProgress) = JSONObject()
        .put("lessonId", p.lessonId)
        .put("courseId", p.courseId)
        .put("completedAt", p.completedAt)
        .put("reps", p.reps)
        .put("intervalDays", p.intervalDays)
        .put("ease", p.ease)
        .put("dueAt", p.dueAt)
        .put("lastMistakes", p.lastMistakes)
        .put("updatedAt", p.updatedAt)
        .toString()

    private fun decode(s: String): LessonProgress? = runCatching {
        val o = JSONObject(s)
        LessonProgress(
            lessonId = o.getString("lessonId"),
            courseId = o.getString("courseId"),
            completedAt = o.getLong("completedAt"),
            reps = o.getInt("reps"),
            intervalDays = o.getDouble("intervalDays"),
            ease = o.getDouble("ease"),
            dueAt = o.getLong("dueAt"),
            lastMistakes = o.optInt("lastMistakes"),
            updatedAt = o.optLong("updatedAt"),
        )
    }.getOrNull()
}
