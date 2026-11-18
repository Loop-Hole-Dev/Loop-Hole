package com.loophole.app.learn

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.content.edit

/** Issue #9: sound and haptic feedback, each switchable from the overflow menu. */
object Feedback {

    private const val PREFS = "settings"
    private const val KEY_SOUND = "sound"
    private const val KEY_HAPTICS = "haptics"

    private var tones: ToneGenerator? = null

    fun soundOn(context: Context) = prefs(context).getBoolean(KEY_SOUND, true)
    fun hapticsOn(context: Context) = prefs(context).getBoolean(KEY_HAPTICS, true)
    fun setSound(context: Context, on: Boolean) = prefs(context).edit { putBoolean(KEY_SOUND, on) }
    fun setHaptics(context: Context, on: Boolean) = prefs(context).edit { putBoolean(KEY_HAPTICS, on) }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun correct(view: View) = play(
        view,
        tone = ToneGenerator.TONE_PROP_ACK,
        haptic = if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY,
    )

    fun wrong(view: View) = play(
        view,
        tone = ToneGenerator.TONE_PROP_NACK,
        haptic = if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS,
    )

    private fun play(view: View, tone: Int, haptic: Int) {
        val context = view.context
        if (hapticsOn(context)) view.performHapticFeedback(haptic)
        if (soundOn(context)) {
            runCatching {
                val generator = tones ?: ToneGenerator(AudioManager.STREAM_MUSIC, 70).also { tones = it }
                generator.startTone(tone, 150)
            }
        }
    }
}
