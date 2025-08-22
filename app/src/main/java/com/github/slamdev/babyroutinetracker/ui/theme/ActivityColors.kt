package com.github.slamdev.babyroutinetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.slamdev.babyroutinetracker.model.ActivityType

/**
 * Color scheme for different activity types that work well in both light and dark modes
 */
object ActivityColors {
    
    // Sleep colors - Calming blue tones
    private val sleepLight = Color(0xFFE3F2FD) // Very light blue
    private val sleepDark = Color(0xFF1A237E) // Deep blue
    private val sleepOngoingLight = Color(0xFFBBDEFB) // Medium light blue
    private val sleepOngoingDark = Color(0xFF283593) // Medium dark blue
    
    // Feeding colors - Warm orange/peach tones  
    private val feedingLight = Color(0xFFFFF3E0) // Very light orange
    private val feedingDark = Color(0xFFE65100) // Deep orange
    private val feedingOngoingLight = Color(0xFFFFE0B2) // Medium light orange
    private val feedingOngoingDark = Color(0xFFFF6F00) // Medium dark orange
    
    // Diaper colors - Fresh green tones
    private val diaperLight = Color(0xFFE8F5E8) // Very light green
    private val diaperDark = Color(0xFF2E7D32) // Deep green
    private val diaperOngoingLight = Color(0xFFC8E6C9) // Medium light green (not used for instant activities)
    private val diaperOngoingDark = Color(0xFF388E3C) // Medium dark green (not used for instant activities)
    
    /**
     * Get background color for sleep activity
     */
    @Composable
    fun getSleepColor(isOngoing: Boolean = false): Color {
        val isDarkTheme = !MaterialTheme.colorScheme.background.luminance().isHighLuminance()
        return when {
            isDarkTheme && isOngoing -> sleepOngoingDark
            isDarkTheme -> sleepDark
            isOngoing -> sleepOngoingLight
            else -> sleepLight
        }
    }
    
    /**
     * Get background color for feeding activity (both breast and bottle)
     */
    @Composable
    fun getFeedingColor(isOngoing: Boolean = false): Color {
        val isDarkTheme = !MaterialTheme.colorScheme.background.luminance().isHighLuminance()
        return when {
            isDarkTheme && isOngoing -> feedingOngoingDark
            isDarkTheme -> feedingDark
            isOngoing -> feedingOngoingLight
            else -> feedingLight
        }
    }
    
    /**
     * Get background color for diaper activity
     */
    @Composable
    fun getDiaperColor(isOngoing: Boolean = false): Color {
        val isDarkTheme = !MaterialTheme.colorScheme.background.luminance().isHighLuminance()
        return when {
            isDarkTheme && isOngoing -> diaperOngoingDark
            isDarkTheme -> diaperDark
            isOngoing -> diaperOngoingLight
            else -> diaperLight
        }
    }
    
    /**
     * Get background color for any activity type
     */
    @Composable
    fun getActivityColor(activityType: ActivityType, isOngoing: Boolean = false): Color {
        return when (activityType) {
            ActivityType.SLEEP -> getSleepColor(isOngoing)
            ActivityType.FEEDING -> getFeedingColor(isOngoing)
            ActivityType.DIAPER -> getDiaperColor(isOngoing)
        }
    }
    
    /**
     * Get background color for feeding activity based on feeding type
     */
    @Composable
    fun getFeedingColorByType(feedingType: String, isOngoing: Boolean = false): Color {
        return getFeedingColor(isOngoing)
    }
}

/**
 * Extension function to check if a color is high luminance (light)
 */
private fun Float.isHighLuminance(): Boolean = this > 0.5f

/**
 * Calculate luminance of a color
 */
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
