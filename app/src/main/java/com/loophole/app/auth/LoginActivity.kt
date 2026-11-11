package com.loophole.app.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.loophole.app.MainActivity
import com.loophole.app.R
import com.loophole.app.databinding.ActivityLoginBinding

/** Issue #2: Login / Sign Up screen with Firebase Auth, plus "continue as guest". */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var signUpMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Skip this screen if the user already logged in or chose guest mode.
        if (AuthManager.isLoggedInOrGuest(this)) {
            openHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginRoot) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        signUpMode = savedInstanceState?.getBoolean(KEY_SIGN_UP) ?: false
        renderMode()

        binding.toggleModeButton.setOnClickListener {
            signUpMode = !signUpMode
            renderMode()
        }
        binding.submitButton.setOnClickListener { submit() }
        binding.guestButton.setOnClickListener {
            AuthManager.continueAsGuest(this)
            openHome()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SIGN_UP, signUpMode)
    }

    private fun renderMode() {
        binding.confirmLayout.isVisible = signUpMode
        binding.submitButton.setText(if (signUpMode) R.string.create_account else R.string.log_in)
        binding.toggleModeButton.setText(
            if (signUpMode) R.string.switch_to_log_in else R.string.switch_to_sign_up
        )
        clearErrors()
    }

    private fun clearErrors() {
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
        binding.confirmLayout.error = null
    }

    private fun submit() {
        clearErrors()
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()
        val confirm = binding.confirmInput.text?.toString().orEmpty()

        var valid = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = getString(R.string.error_email_invalid)
            valid = false
        }
        if (password.length < 6) {
            binding.passwordLayout.error = getString(R.string.error_password_short)
            valid = false
        }
        if (signUpMode && password != confirm) {
            binding.confirmLayout.error = getString(R.string.error_password_mismatch)
            valid = false
        }
        if (!valid) return

        setLoading(true)
        val onResult: (Int?) -> Unit = { errorRes ->
            setLoading(false)
            if (errorRes == null) openHome()
            else Snackbar.make(binding.root, errorRes, Snackbar.LENGTH_LONG).show()
        }
        if (signUpMode) AuthManager.signUp(this, email, password, onResult)
        else AuthManager.logIn(this, email, password, onResult)
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        binding.submitButton.isEnabled = !loading
        binding.guestButton.isEnabled = !loading
        binding.toggleModeButton.isEnabled = !loading
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val KEY_SIGN_UP = "sign_up_mode"
    }
}
