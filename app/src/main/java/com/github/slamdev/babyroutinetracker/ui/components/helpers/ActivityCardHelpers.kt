package com.github.slamdev.babyroutinetracker.ui.components.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.ui.components.ActivityCardConfig
import com.github.slamdev.babyroutinetracker.ui.components.ActivityCardContent
import com.github.slamdev.babyroutinetracker.ui.theme.ActivityColors
import java.util.Date

/**
 * Helper function to create configuration for sleep activity
 */
@Composable
fun sleepActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = stringResource(R.string.activity_sleep),
    icon = "😴",
    isImmediateActivity = false,
    cardBackgroundColor = { isOngoing -> ActivityColors.getSleepColor(isOngoing) }
)

/**
 * Helper function to create configuration for breast feeding activity
 */
@Composable
fun breastFeedingActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = stringResource(R.string.activity_breast),
    icon = "🤱",
    isImmediateActivity = false,
    cardBackgroundColor = { isOngoing -> ActivityColors.getFeedingColor(isOngoing) }
)

/**
 * Helper function to create configuration for bottle feeding activity
 */
@Composable
fun bottleFeedingActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = stringResource(R.string.activity_bottle),
    icon = "🍼",
    isImmediateActivity = true,
    cardBackgroundColor = { isOngoing -> ActivityColors.getFeedingColor(isOngoing) }
)

/**
 * Helper function to create configuration for diaper activity
 */
@Composable
fun diaperActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = stringResource(R.string.activity_poop),
    icon = "💩",
    isImmediateActivity = true,
    cardBackgroundColor = { isOngoing -> ActivityColors.getDiaperColor(isOngoing) }
)

// Content helper functions

/**
 * Helper function to create content for sleep activity
 */
@Composable
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
    noActivityText = stringResource(R.string.activity_no_recent_sleep)
)

/**
 * Helper function to create content for breast feeding activity
 */
@Composable
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
    noActivityText = stringResource(R.string.activity_no_recent_feeding)
)

/**
 * Helper function to create content for bottle feeding activity
 */
@Composable
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
    noActivityText = stringResource(R.string.activity_no_recent_feeding)
)

/**
 * Helper function to create content for diaper activity
 */
@Composable
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
    noActivityText = stringResource(R.string.activity_no_poops_logged)
)
