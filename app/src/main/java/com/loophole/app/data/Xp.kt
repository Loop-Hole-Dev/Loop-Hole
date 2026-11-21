package com.loophole.app.data

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.loophole.app.auth.AuthManager

/**
 * Issue #13: XP and levels.
 *
 * Level L starts at 50·L·(L−1) XP: level 2 at 100, 3 at 300, 4 at 600, 5 at 1000...
 * Signed-in users' totals are also written to users/{uid}.xp for the leaderboard (#18).
 */
object Xp {

    const val LESSON_FIRST_TIME = 50
    const val LESSON_REPEAT = 10
    const val REVIEW = 20
    const val PERFECT_BONUS = 15
    const val PRACTICE_PER_MOVE = 2

    private const val PREFS = "progress"
    private const val KEY_XP = "xp"

    fun total(context: Context) = prefs(context).getInt(KEY_XP, 0)

    /** Adds XP and returns the new total. */
    fun award(context: Context, amount: Int): Int {
        val total = total(context) + amount
        prefs(context).edit { putInt(KEY_XP, total) }
        if (AuthManager.isFirebaseReady(context)) {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                FirebaseFirestore.getInstance().collection(FirestoreSchema.USERS).document(uid)
                    .set(mapOf("xp" to total, "xpUpdatedAt" to System.currentTimeMillis()), SetOptions.merge())
            }
        }
        return total
    }

    fun levelFor(xp: Int): Int {
        var level = 1
        while (xp >= startOf(level + 1)) level++
        return level
    }

    /** XP needed to reach [level]. */
    fun startOf(level: Int) = 50 * level * (level - 1)

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
