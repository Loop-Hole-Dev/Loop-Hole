package com.loophole.app.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.loophole.app.R

/**
 * Wraps Firebase Authentication plus a local "guest" flag.
 *
 * If google-services.json hasn't been added yet, Firebase isn't initialized;
 * [isFirebaseReady] returns false and the app can still be used as a guest.
 */
object AuthManager {

    private const val PREFS = "auth_prefs"
    private const val KEY_GUEST = "is_guest"

    fun isFirebaseReady(context: Context): Boolean =
        FirebaseApp.getApps(context).isNotEmpty()

    private fun auth(context: Context): FirebaseAuth? =
        if (isFirebaseReady(context)) FirebaseAuth.getInstance() else null

    fun currentEmail(context: Context): String? = auth(context)?.currentUser?.email

    fun isGuest(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_GUEST, false)

    fun isLoggedInOrGuest(context: Context): Boolean =
        currentEmail(context) != null || isGuest(context)

    fun continueAsGuest(context: Context) = setGuest(context, true)

    private fun setGuest(context: Context, guest: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_GUEST, guest) }
    }

    fun logIn(context: Context, email: String, password: String, onResult: (errorRes: Int?) -> Unit) {
        val auth = auth(context) ?: return onResult(R.string.error_firebase_missing)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { setGuest(context, false); onResult(null) }
            .addOnFailureListener { onResult(errorFor(it)) }
    }

    fun signUp(context: Context, email: String, password: String, onResult: (errorRes: Int?) -> Unit) {
        val auth = auth(context) ?: return onResult(R.string.error_firebase_missing)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { setGuest(context, false); onResult(null) }
            .addOnFailureListener { onResult(errorFor(it)) }
    }

    fun logOut(context: Context) {
        auth(context)?.signOut()
        setGuest(context, false)
    }

    private fun errorFor(e: Exception): Int = when (e) {
        is FirebaseAuthUserCollisionException -> R.string.error_email_in_use
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> R.string.error_wrong_credentials
        is FirebaseNetworkException -> R.string.error_network
        else -> R.string.error_generic
    }
}
