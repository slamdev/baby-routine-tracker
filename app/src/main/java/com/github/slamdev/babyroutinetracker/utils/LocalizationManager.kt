package com.github.slamdev.babyroutinetracker.utils

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Utility object for handling localization in the app.
 * Provides methods for formatting strings, dates, times, and numbers according to the current locale.
 */
object LocalizationManager {
    
    /**
     * Get string resource with proper locale handling
     */
    @Composable
    fun getString(@StringRes resId: Int): String {
        return stringResource(id = resId)
    }
    
    /**
     * Get formatted string resource with arguments
     */
    @Composable
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return stringResource(id = resId, formatArgs = formatArgs)
    }
    
    /**
     * Get plural string resource with proper locale handling
     */
    @Composable
    fun getPlural(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String {
        return pluralStringResource(id = resId, count = quantity, formatArgs = formatArgs)
    }
    
    /**
     * Format time according to current locale
     * @param date The date to format
     * @param use24Hour Whether to use 24-hour format (defaults to true for most locales)
     */
    fun formatTime(date: Date, use24Hour: Boolean = shouldUse24HourFormat()): String {
        val locale = Locale.getDefault()
        val pattern = if (use24Hour) "HH:mm" else "h:mm a"
        return SimpleDateFormat(pattern, locale).format(date)
    }
    
    /**
     * Format date according to current locale
     */
    fun formatDate(date: Date): String {
        val locale = Locale.getDefault()
        return DateFormat.getDateInstance(DateFormat.SHORT, locale).format(date)
    }
    
    /**
     * Format date and time according to current locale
     */
    fun formatDateTime(date: Date): String {
        val locale = Locale.getDefault()
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(date)
    }
    
    /**
     * Format decimal numbers according to locale (comma vs period)
     */
    fun formatDecimal(value: Double): String {
        return NumberFormat.getNumberInstance().format(value)
    }
    
    /**
     * Format feeding amount with proper units and locale formatting
     */
    fun formatFeedingAmount(amountMl: Double): String {
        return "${formatDecimal(amountMl)} ml"
    }
    
    /**
     * Format duration in minutes to a user-friendly string
     */
    fun formatDuration(minutes: Long): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        
        return when {
            hours > 0 -> "${hours}h ${remainingMinutes}m"
            else -> "${remainingMinutes}m"
        }
    }
    
    /**
     * Format elapsed time to show current duration (for ongoing activities)
     */
    fun formatElapsedTime(startTime: Date): String {
        val now = Date()
        val diffMillis = now.time - startTime.time
        val diffMinutes = diffMillis / (1000 * 60)
        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
    
    /**
     * Format time ago in a user-friendly, localized format
     * Examples: "now", "5m ago", "1h 23m ago", "2d ago"
     */
    fun formatTimeAgo(pastDate: Date): String {
        val now = Date()
        val diffMillis = abs(now.time - pastDate.time)
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        val diffWeeks = diffDays / 7
        
        val locale = Locale.getDefault()
        
        return when {
            diffMinutes < 1 -> when (locale.language) {
                "ru" -> "сейчас"
                else -> "now"
            }
            diffMinutes < 60 -> when (locale.language) {
                "ru" -> "${diffMinutes}м назад"
                else -> "${diffMinutes}m ago"
            }
            diffHours < 24 -> {
                val hours = diffHours
                val remainingMinutes = diffMinutes % 60
                when (locale.language) {
                    "ru" -> if (remainingMinutes == 0L) {
                        "${hours}ч назад"
                    } else {
                        "${hours}ч ${remainingMinutes}м назад"
                    }
                    else -> if (remainingMinutes == 0L) {
                        "${hours}h ago"
                    } else {
                        "${hours}h ${remainingMinutes}m ago"
                    }
                }
            }
            diffDays < 7 -> when (locale.language) {
                "ru" -> "${diffDays}д назад"
                else -> "${diffDays}d ago"
            }
            else -> when (locale.language) {
                "ru" -> "${diffWeeks}н назад"
                else -> "${diffWeeks}w ago"
            }
        }
    }
    
    /**
     * Determine if 24-hour format should be used based on locale
     */
    private fun shouldUse24HourFormat(): Boolean {
        val locale = Locale.getDefault()
        // Most locales prefer 24-hour format, US prefers 12-hour
        return when (locale.country) {
            "US" -> false
            else -> true
        }
    }
    
    /**
     * Format age with cultural preferences
     * Russian speakers tend to prefer months, English speakers use weeks for young babies
     */
    fun formatAge(totalDays: Long): String {
        val months = totalDays / 30
        val weeks = totalDays / 7
        val days = totalDays
        
        val locale = Locale.getDefault()
        
        return when (locale.language) {
            "ru" -> {
                // Russian preference: use months more often
                when {
                    months > 0 -> "${months} ${getPluralForm("месяц", "месяца", "месяцев", months.toInt())}"
                    weeks > 0 -> "${weeks} ${getPluralForm("неделя", "недели", "недель", weeks.toInt())}"
                    else -> "${days} ${getPluralForm("день", "дня", "дней", days.toInt())}"
                }
            }
            else -> {
                // English: more detailed with weeks when young
                when {
                    months >= 3 -> "${months} month${if (months == 1L) "" else "s"} old"
                    weeks > 0 -> "${weeks} week${if (weeks == 1L) "" else "s"} old"
                    else -> "${days} day${if (days == 1L) "" else "s"} old"
                }
            }
        }
    }
    
    /**
     * Helper function for Russian pluralization
     */
    private fun getPluralForm(one: String, few: String, many: String, count: Int): String {
        val mod10 = count % 10
        val mod100 = count % 100
        
        return when {
            mod100 in 11..14 -> many
            mod10 == 1 -> one
            mod10 in 2..4 -> few
            else -> many
        }
    }
}
