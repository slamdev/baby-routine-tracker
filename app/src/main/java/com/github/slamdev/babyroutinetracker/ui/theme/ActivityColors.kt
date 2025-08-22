package com.github.slamdev.babyroutinetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.slamdev.babyroutinetracker.model.ActivityType

/**
 * Modern color scheme for different activity types with improved harmony in both light and dark modes
 * Following Material Design 3 principles with better contrast and visual appeal
 */
object ActivityColors {
    
    // Sleep colors - Calming blue/indigo tones with better contrast
    private val sleepLight = Color(0xFFE8F4FD) // Softer light blue
    private val sleepDark = Color(0xFF2C3E7A) // Refined dark blue (less harsh)
    private val sleepOngoingLight = Color(0xFFD1E9FC) // More vibrant ongoing light
    private val sleepOngoingDark = Color(0xFF3F51B5) // Material indigo for ongoing
    
    // Feeding colors - Warm coral/peach tones (more modern than orange)
    private val feedingLight = Color(0xFFFFF4F0) // Soft peachy background
    private val feedingDark = Color(0xFF8D4E2A) // Warmer brown-orange (less jarring)
    private val feedingOngoingLight = Color(0xFFFFE4D6) // Gentle coral
    private val feedingOngoingDark = Color(0xFFD4700A) // Refined orange-amber
    
    // Diaper colors - Fresh mint/teal tones (more modern than pure green)
    private val diaperLight = Color(0xFFF0FAF5) // Mint-tinged background
    private val diaperDark = Color(0xFF2E5D4A) // Forest green (more sophisticated)
    private val diaperOngoingLight = Color(0xFFD8F5E3) // Soft mint
    private val diaperOngoingDark = Color(0xFF4CAF50) // Material green for ongoing
    
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
