package com.loophole.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.loophole.app.reminders.Reminders

class LoopHoleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppTheme.applySaved(this)
        Reminders.createChannel(this)
    }
}

/** Issue #16: System / Light / Dark theme choice, saved and applied at launch. */
object AppTheme {
    private const val PREFS = "settings"
    private const val KEY_THEME = "theme"

    /** Menu order: System, Light, Dark. */
    val MODES = intArrayOf(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
    )

    fun saved(context: Context) =
        prefs(context).getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun applySaved(context: Context) = AppCompatDelegate.setDefaultNightMode(saved(context))

    fun set(context: Context, mode: Int) {
        prefs(context).edit { putInt(KEY_THEME, mode) }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
