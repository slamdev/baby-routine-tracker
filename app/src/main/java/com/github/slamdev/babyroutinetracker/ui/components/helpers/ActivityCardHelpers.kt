package com.github.slamdev.babyroutinetracker.ui.components.helpers

import com.github.slamdev.babyroutinetracker.ui.components.ActivityCardConfig
import com.github.slamdev.babyroutinetracker.ui.components.ActivityCardContent
import com.github.slamdev.babyroutinetracker.ui.theme.ActivityColors
import java.util.Date

/**
 * Helper function to create configuration for sleep activity
 */
fun sleepActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = "Sleep",
    icon = "😴",
    isImmediateActivity = false,
    cardBackgroundColor = { isOngoing -> ActivityColors.getSleepColor(isOngoing) }
)

/**
 * Helper function to create configuration for breast feeding activity
 */
fun breastFeedingActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = "Boob",
    icon = "🤱",
    isImmediateActivity = false,
    cardBackgroundColor = { isOngoing -> ActivityColors.getFeedingColor(isOngoing) }
)

/**
 * Helper function to create configuration for bottle feeding activity
 */
fun bottleFeedingActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = "Bottle",
    icon = "🍼",
    isImmediateActivity = true,
    cardBackgroundColor = { isOngoing -> ActivityColors.getFeedingColor(isOngoing) }
)

/**
 * Helper function to create configuration for diaper activity
 */
fun diaperActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = "Poop",
    icon = "💩",
    isImmediateActivity = true,
    cardBackgroundColor = { isOngoing -> ActivityColors.getDiaperColor(isOngoing) }
)

// Content helper functions

/**
 * Helper function to create content for sleep activity
 */
fun sleepActivityContent(
    ongoingSleep: Any? = null,
    ongoingStartTime: Date? = null,
    lastSleep: Any? = null,
    lastSleepText: String? = null,
    timeAgo: String? = null,
    endTime: Date? = null,
    onEditStartTime: (() -> Unit)? = null
): ActivityCardContent = ActivityCardContent(
    ongoingActivity = ongoingSleep,
    ongoingStartTime = ongoingStartTime,
    onEditStartTime = onEditStartTime,
    lastActivity = lastSleep,
    lastActivityText = lastSleepText,
    lastActivityTimeAgo = timeAgo,
    lastActivityTime = endTime,
    noActivityText = "No recent sleep"
)

/**
 * Helper function to create content for breast feeding activity
 */
fun breastFeedingActivityContent(
    ongoingFeeding: Any? = null,
    ongoingStartTime: Date? = null,
    lastFeeding: Any? = null,
    lastFeedingText: String? = null,
    timeAgo: String? = null,
    endTime: Date? = null,
    onEditStartTime: (() -> Unit)? = null
): ActivityCardContent = ActivityCardContent(
    ongoingActivity = ongoingFeeding,
    ongoingStartTime = ongoingStartTime,
    onEditStartTime = onEditStartTime,
    lastActivity = lastFeeding,
    lastActivityText = lastFeedingText,
    lastActivityTimeAgo = timeAgo,
    lastActivityTime = endTime,
    noActivityText = "No recent feeding"
)

/**
 * Helper function to create content for bottle feeding activity
 */
fun bottleFeedingActivityContent(
    lastFeeding: Any? = null,
    lastFeedingText: String? = null,
    timeAgo: String? = null,
    feedingTime: Date? = null,
    notes: String? = null
): ActivityCardContent = ActivityCardContent(
    lastActivity = lastFeeding,
    lastActivityText = lastFeedingText,
    lastActivityTimeAgo = timeAgo,
    lastActivityTime = feedingTime,
    lastActivityNotes = notes,
    noActivityText = "No recent feeding"
)

/**
 * Helper function to create content for diaper activity
 */
fun diaperActivityContent(
    lastDiaper: Any? = null,
    lastDiaperText: String? = null,
    timeAgo: String? = null,
    diaperTime: Date? = null,
    notes: String? = null
): ActivityCardContent = ActivityCardContent(
    lastActivity = lastDiaper,
    lastActivityText = lastDiaperText,
    lastActivityTimeAgo = timeAgo,
    lastActivityTime = diaperTime,
    lastActivityNotes = notes,
    noActivityText = "No poops logged yet"
)
