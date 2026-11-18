package com.loophole.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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
}
