package com.github.slamdev.babyroutinetracker.ui.components

import android.content.Context
import com.github.slamdev.babyroutinetracker.R
import java.util.*
import kotlin.math.abs

/**
 * Utility functions for time formatting and calculations
 */
object TimeUtils {
    
    /**
     * Format time elapsed since the given date in a user-friendly way
     * Examples: "Happened 23m ago", "Happened 1h 23m ago", "Happened 1d ago"
     * 
     * @param pastDate The date/time when the activity happened
     * @param context Android context to access string resources for localization
     * @return Formatted string showing how long ago the activity happened
     */
    fun formatTimeAgo(pastDate: Date, context: Context): String {
        val now = Date()
        val diffMillis = abs(now.time - pastDate.time)
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        
        return when {
            diffMinutes < 1 -> context.getString(R.string.happened_now)
            diffMinutes < 60 -> context.getString(R.string.happened_minutes_ago, diffMinutes)
            diffHours < 24 -> {
                val hours = diffHours
                val remainingMinutes = diffMinutes % 60
                if (remainingMinutes == 0L) {
                    context.getString(R.string.happened_hours_ago, hours)
                } else {
                    context.getString(R.string.happened_hours_minutes_ago, hours, remainingMinutes)
                }
            }
            diffDays < 7 -> context.getString(R.string.happened_days_ago, diffDays)
            else -> {
                val weeks = diffDays / 7
                context.getString(R.string.happened_weeks_ago, weeks)
            }
        }
    }
    
    /**
     * Get the most relevant timestamp for an activity to calculate "time ago"
     * For completed activities, use end time. For ongoing activities, use start time.
     * For instant activities (diaper, bottle), use start time.
     * 
     * @param startTime The start time of the activity
     * @param endTime The end time of the activity (null if ongoing or instant)
     * @return The most relevant timestamp for "time ago" calculation
     */
    fun getRelevantTimestamp(startTime: Date, endTime: Date?): Date {
        return endTime ?: startTime
    }
}
