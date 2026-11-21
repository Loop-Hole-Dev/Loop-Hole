package com.loophole.app.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.loophole.app.auth.AuthManager

/**
 * Issue #4: course catalog.
 *
 * The courses bundled in assets/courses/ load instantly and work offline (and in builds
 * without google-services.json). When Firebase is configured, [refreshFromFirestore]
 * replaces them with the `courses` + `lessons` collections, and seeds those collections
 * from the bundled files the first time a signed-in user opens the app.
 */
object CourseRepository {

    private const val TAG = "CourseRepository"
    private const val ASSET_DIR = "courses"

    @Volatile
    private var cache: List<Course>? = null

    fun courses(context: Context): List<Course> =
        cache ?: loadBundled(context).also { cache = it }

    fun course(context: Context, id: String): Course? = courses(context).find { it.id == id }

    fun lesson(context: Context, lessonId: String): Lesson? =
        courses(context).asSequence().flatMap { it.lessons }.find { it.id == lessonId }

    private fun loadBundled(context: Context): List<Course> {
        val assets = context.applicationContext.assets
        return assets.list(ASSET_DIR).orEmpty()
            .filter { it.endsWith(".json") }
            .map { name -> assets.open("$ASSET_DIR/$name").bufferedReader().use { CourseJson.parse(it.readText()) } }
            .sortedBy { it.order }
    }

    /** Loads courses from Firestore; calls [onUpdated] only if Firestore returned a catalog. */
    fun refreshFromFirestore(context: Context, onUpdated: (List<Course>) -> Unit) {
        if (!AuthManager.isFirebaseReady(context)) return
        val appContext = context.applicationContext
        val db = FirebaseFirestore.getInstance()
        db.collection(FirestoreSchema.COURSES).get()
            .addOnSuccessListener { courseDocs ->
                if (courseDocs.isEmpty) {
                    seedIfSignedIn(db, courses(appContext))
                    return@addOnSuccessListener
                }
                db.collection(FirestoreSchema.LESSONS).get().addOnSuccessListener { lessonDocs ->
                    val parsed = fromFirestore(courseDocs.documents, lessonDocs.documents)
                    if (parsed.isNotEmpty()) {
                        cache = parsed
                        onUpdated(parsed)
                    }
                }
            }
            .addOnFailureListener { Log.w(TAG, "Using bundled courses: ${it.message}") }
    }

    private fun fromFirestore(courseDocs: List<DocumentSnapshot>, lessonDocs: List<DocumentSnapshot>): List<Course> {
        val lessonsByCourse = lessonDocs.mapNotNull { doc ->
            @Suppress("UNCHECKED_CAST")
            val moves = (doc.get("moves") as? List<Map<String, Any?>>).orEmpty()
                .map { LessonMove(it["san"] as? String ?: return@mapNotNull null, it["note"] as? String ?: "") }
            Lesson(
                id = doc.id,
                courseId = doc.getString("courseId") ?: return@mapNotNull null,
                order = doc.getLong("order")?.toInt() ?: 0,
                title = doc.getString("title").orEmpty(),
                summary = doc.getString("summary").orEmpty(),
                moves = moves,
            )
        }.groupBy { it.courseId }

        return courseDocs.mapNotNull { doc ->
            @Suppress("UNCHECKED_CAST")
            Course(
                id = doc.id,
                name = doc.getString("name") ?: return@mapNotNull null,
                side = doc.getString("side") ?: "white",
                difficulty = doc.getString("difficulty").orEmpty(),
                description = doc.getString("description").orEmpty(),
                order = doc.getLong("order")?.toInt() ?: 0,
                keyMoves = (doc.get("keyMoves") as? List<String>).orEmpty(),
                lessons = lessonsByCourse[doc.id].orEmpty().sortedBy { it.order },
            )
        }.filter { it.lessons.isNotEmpty() }.sortedBy { it.order }
    }

    /** Writes the bundled catalog to Firestore. Security rules only allow signed-in writes. */
    private fun seedIfSignedIn(db: FirebaseFirestore, courses: List<Course>) {
        if (FirebaseAuth.getInstance().currentUser == null) return
        val batch = db.batch()
        for (course in courses) {
            batch.set(db.collection(FirestoreSchema.COURSES).document(course.id), FirestoreSchema.courseDoc(course))
            for (lesson in course.lessons) {
                batch.set(db.collection(FirestoreSchema.LESSONS).document(lesson.id), FirestoreSchema.lessonDoc(lesson))
            }
        }
        batch.commit()
            .addOnSuccessListener { Log.i(TAG, "Seeded ${courses.size} courses into Firestore") }
            .addOnFailureListener { Log.w(TAG, "Seeding failed: ${it.message}") }
    }
}
