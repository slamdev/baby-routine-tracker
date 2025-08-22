package com.github.slamdev.babyroutinetracker.ui.components.formatters

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object TimeUtils {
    /**
     * Format time elapsed since the given date in a user-friendly way
     * Examples: "Happened 23m ago", "Happened 1h 23m ago", "Happened 1d ago"
     */
    fun formatTimeAgo(pastDate: Date): String {
        val now = Date()
        val diffMillis = abs(now.time - pastDate.time)
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        return when {
            diffMinutes < 1 -> "Happened now"
            diffMinutes < 60 -> "Happened ${diffMinutes}m ago"
            diffHours < 24 -> {
                val hours = diffHours
                val remainingMinutes = diffMinutes % 60
                if (remainingMinutes == 0L) {
                    "Happened ${hours}h ago"
                } else {
                    "Happened ${hours}h ${remainingMinutes}m ago"
                }
            }
            diffDays < 7 -> "Happened ${diffDays}d ago"
            else -> {
                val weeks = diffDays / 7
                "Happened ${weeks}w ago"
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
}

/**
 * Format elapsed time in seconds to a human-readable string
 */
fun formatElapsedTime(elapsedTimeSeconds: Long): String {
    val hours = elapsedTimeSeconds / 3600
    val minutes = (elapsedTimeSeconds % 3600) / 60
    val seconds = elapsedTimeSeconds % 60

    return when {
        hours > 0 -> "%02d:%02d:%02d".format(hours, minutes, seconds)
        else -> "%02d:%02d".format(minutes, seconds)
    }
}

/**
 * Format a Date to display time in HH:mm format
 */
fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}
