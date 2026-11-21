package com.loophole.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.loophole.app.reminders.Reminders
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.loophole.app.auth.AuthManager
import com.loophole.app.auth.LoginActivity
import com.loophole.app.data.ProgressStore
import com.loophole.app.databinding.ActivityMainBinding
import com.loophole.app.learn.Feedback

/** Issue #3: bottom tab navigation (Openings, Review, Dashboard). */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        navController =
            (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController
        appBarConfig = AppBarConfiguration(
            setOf(R.id.openingsFragment, R.id.reviewFragment, R.id.dashboardFragment)
        )
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)

        // Lessons get the whole screen.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = destination.id != R.id.learnFragment
        }

        // Signed-in users get their progress from other devices.
        ProgressStore.syncFromFirestore(this) { recreate() }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_overflow, menu)
        menu.findItem(R.id.action_reminders).isChecked = Reminders.isEnabled(this) && Reminders.hasPermission(this)
        menu.findItem(R.id.action_sound).isChecked = Feedback.soundOn(this)
        menu.findItem(R.id.action_haptics).isChecked = Feedback.hapticsOn(this)
        // Guests see "Sign in" instead of "Log out".
        menu.findItem(R.id.action_account).setTitle(
            if (AuthManager.currentEmail(this) == null) R.string.sign_in else R.string.log_out
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_games -> navController.navigate(R.id.ticTacToeFragment)
            R.id.action_theme -> chooseTheme()
            R.id.action_reminders -> {
                if (item.isChecked) {
                    Reminders.setEnabled(this, false)
                    item.isChecked = false
                } else if (Reminders.hasPermission(this)) {
                    enableReminders()
                } else {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            R.id.action_sound -> {
                item.isChecked = !item.isChecked
                Feedback.setSound(this, item.isChecked)
            }
            R.id.action_haptics -> {
                item.isChecked = !item.isChecked
                Feedback.setHaptics(this, item.isChecked)
            }
            R.id.action_account -> {
                AuthManager.logOut(this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // Issue #15: Android 13+ asks before an app can post notifications.
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableReminders()
            else Snackbar.make(binding.root, R.string.reminders_permission_denied, Snackbar.LENGTH_LONG).show()
        }

    private fun enableReminders() {
        Reminders.setEnabled(this, true)
        invalidateOptionsMenu()
        Snackbar.make(binding.root, R.string.reminders_on, Snackbar.LENGTH_SHORT).setAnchorView(binding.bottomNav).show()
    }

    /** Issue #16: System / Light / Dark. */
    private fun chooseTheme() {
        val labels = arrayOf(getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark))
        val current = AppTheme.MODES.indexOf(AppTheme.saved(this)).coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.menu_theme)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                dialog.dismiss()
                AppTheme.set(this, AppTheme.MODES[which])
            }
            .show()
    }
}
