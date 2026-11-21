package com.loophole.app.review

import android.text.format.DateUtils
import java.util.concurrent.TimeUnit

/** Friendly "when is it due" text: "in 10 minutes", "in 5 hours", "tomorrow", "in 6 days". */
object DueText {

    fun relative(dueAt: Long, now: Long = System.currentTimeMillis(), midSentence: Boolean = false): String {
        val resolution = if (dueAt - now >= TimeUnit.HOURS.toMillis(20)) DateUtils.DAY_IN_MILLIS
        else DateUtils.MINUTE_IN_MILLIS
        // Nudge forward so a fresh 1-day interval reads "tomorrow", not "in 23 hours".
        val rounded = if (resolution == DateUtils.DAY_IN_MILLIS) dueAt + TimeUnit.HOURS.toMillis(4) else dueAt
        val text = DateUtils.getRelativeTimeSpanString(rounded, now, resolution).toString()
        return if (midSentence) text.replaceFirstChar { it.lowercase() } else text
    }
}
