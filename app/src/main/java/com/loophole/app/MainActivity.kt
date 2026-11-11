package com.loophole.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.loophole.app.auth.AuthManager
import com.loophole.app.auth.LoginActivity
import com.loophole.app.databinding.ActivityMainBinding

/** Issue #3: bottom tab navigation (Openings, Review, Dashboard). */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController
        val topLevelTabs = AppBarConfiguration(
            setOf(R.id.openingsFragment, R.id.reviewFragment, R.id.dashboardFragment)
        )
        setupActionBarWithNavController(navController, topLevelTabs)
        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_overflow, menu)
        // Guests see "Sign in" instead of "Log out".
        menu.findItem(R.id.action_account).setTitle(
            if (AuthManager.currentEmail(this) == null) R.string.sign_in else R.string.log_out
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_account) {
            AuthManager.logOut(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
