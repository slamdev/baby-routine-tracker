package com.github.slamdev.babyroutinetracker.ui.components.formatters

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.slamdev.babyroutinetracker.R
import java.util.Date
import kotlin.math.abs

/**
 * Localized time formatting functions for use in Compose UI.
 * These functions use string resources for proper internationalization.
 */

/**
 * Format time elapsed since the given date in a user-friendly way using localized strings
 * Examples: "Happened 23m ago" / "Произошло 23 мин назад"
 */
@Composable
fun formatTimeAgoLocalized(pastDate: Date): String {
    val now = Date()
    val diffMillis = abs(now.time - pastDate.time)
    val diffSeconds = diffMillis / 1000
    val diffMinutes = diffSeconds / 60
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffMinutes < 1 -> stringResource(R.string.happened_now)
        diffMinutes < 60 -> stringResource(R.string.happened_minutes_ago, diffMinutes)
        diffHours < 24 -> {
            val hours = diffHours
            val remainingMinutes = diffMinutes % 60
            if (remainingMinutes == 0L) {
                stringResource(R.string.happened_hours_ago, hours)
            } else {
                stringResource(R.string.happened_hours_minutes_ago, hours, remainingMinutes)
            }
        }
        diffDays < 7 -> stringResource(R.string.happened_days_ago, diffDays)
        else -> {
            val weeks = diffDays / 7
            stringResource(R.string.happened_weeks_ago, weeks)
        }
    }
}

/**
 * Get the most relevant timestamp for an activity to calculate "time ago"
 * For completed activities, use end time. For ongoing activities, use start time.
 */
fun getRelevantTimestamp(startTime: Date, endTime: Date?): Date {
    return endTime ?: startTime
}
