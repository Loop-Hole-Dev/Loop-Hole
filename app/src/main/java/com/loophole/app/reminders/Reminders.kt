package com.loophole.app.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.loophole.app.R
import com.loophole.app.auth.LoginActivity
import com.loophole.app.data.ProgressStore
import java.util.Calendar

/**
 * Issue #15: a daily reminder at 6 pm when reviews are due or the streak is at risk.
 * Uses an inexact repeating alarm (battery friendly, no exact-alarm permission needed)
 * and is re-scheduled after a reboot or app update.
 */
object Reminders {

    private const val CHANNEL_ID = "practice_reminders"
    private const val NOTIFICATION_ID = 1
    private const val PREFS = "settings"
    private const val KEY_ENABLED = "reminders"
    private const val REMINDER_HOUR = 18
    const val ACTION_REMIND = "com.loophole.app.action.REMIND"

    fun isEnabled(context: Context) = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_ENABLED, enabled) }
        if (enabled) schedule(context) else cancel(context)
    }

    fun hasPermission(context: Context) = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val channel = NotificationChannel(
            CHANNEL_ID, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.reminder_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun schedule(context: Context) {
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarms(context).setInexactRepeating(
            AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent(context)
        )
    }

    fun cancel(context: Context) = alarms(context).cancel(alarmIntent(context))

    /** Posts the reminder if there's something to do today. Returns true if one was shown. */
    fun showIfNeeded(context: Context): Boolean {
        if (!hasPermission(context)) return false
        val due = ProgressStore.dueTodayCount(context)
        val streak = ProgressStore.streak(context)
        val text = when {
            due > 0 -> context.resources.getQuantityString(R.plurals.reminder_due, due, due)
            streak > 0 && !ProgressStore.activeToday(context) ->
                context.resources.getQuantityString(R.plurals.reminder_streak, streak, streak)
            else -> return false
        }
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_openings)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission") // checked by hasPermission()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        return true
    }

    private fun alarmIntent(context: Context) = PendingIntent.getBroadcast(
        context, 0,
        Intent(context, ReminderReceiver::class.java).setAction(ACTION_REMIND),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun alarms(context: Context) = context.getSystemService(AlarmManager::class.java)
    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/** Fires the daily reminder, and re-schedules it after a reboot or an app update. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!Reminders.isEnabled(context)) return
        when (intent.action) {
            Reminders.ACTION_REMIND -> Reminders.showIfNeeded(context)
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> Reminders.schedule(context)
        }
    }
}
