package com.github.slamdev.babyroutinetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity configuration for the reusable activity card
 */
data class ActivityCardConfig(
    val title: String,
    val icon: String,
    val isImmediateActivity: Boolean, // true for instant activities like bottle/diaper, false for timed activities like sleep/breastfeeding
    val cardBackgroundColor: @Composable (Boolean) -> androidx.compose.ui.graphics.Color = { isOngoing ->
        if (isOngoing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    }
)

/**
 * Content configuration for activity card body
 */
data class ActivityCardContent(
    // For ongoing activities
    val ongoingActivity: Any? = null, // Current ongoing activity (sleep, feeding)
    val ongoingStartTime: java.util.Date? = null, // Start time for ongoing activity
    val onEditStartTime: (() -> Unit)? = null,
    
    // For last activity display
    val lastActivity: Any? = null, // Last completed activity
    val lastActivityText: String? = null, // e.g., "Last slept 2h 30m", "Last fed 120ml", "Last poop logged"
    val lastActivityTimeAgo: String? = null, // e.g., "2 hours ago"
    val lastActivityTime: java.util.Date? = null, // End time for ongoing activities, start time for immediate activities
    val lastActivityNotes: String? = null, // Optional notes to display
    
    // Fallback text when no activity exists
    val noActivityText: String = "No recent activity"
)

/**
 * State for the activity card
 */
data class ActivityCardState(
    val isLoading: Boolean = false,
    val isOngoing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentElapsedTime: Long = 0L, // For ongoing activities timer
    val isLoadingContent: Boolean = false,
    val contentError: String? = null
)

/**
 * Reusable Activity Card Component
 * 
 * @param config Configuration for the activity card
 * @param state Current state of the activity
 * @param content Content configuration for the activity card body
 * @param modifier Modifier for the card
 * @param onPrimaryAction Primary action callback (start/log activity)
 * @param onAlternateAction Alternate action callback (stop activity), only used for ongoing activities
 * @param onContentClick Callback when content area is clicked (for editing)
 * @param onDismissError Callback to dismiss error messages
 * @param onDismissSuccess Callback to dismiss success messages
 */
@Composable
fun ActivityCard(
    config: ActivityCardConfig,
    state: ActivityCardState,
    content: ActivityCardContent,
    modifier: Modifier = Modifier,
    onPrimaryAction: () -> Unit = {},
    onAlternateAction: () -> Unit = {},
    onContentClick: () -> Unit = {},
    onDismissError: () -> Unit = {},
    onDismissSuccess: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = config.cardBackgroundColor(state.isOngoing)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${config.icon} ${config.title}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            // Action button
            val hasOngoingState = !config.isImmediateActivity
            val isOngoing = hasOngoingState && state.isOngoing
            
            // Determine button properties based on activity type and state
            val (buttonIcon, buttonText, buttonColor) = when {
                config.isImmediateActivity -> Triple(
                    Icons.Default.Add,
                    "Log ${config.title}",
                    MaterialTheme.colorScheme.primary
                )
                isOngoing -> Triple(
                    Icons.Default.Check,
                    "Stop ${config.title}",
                    MaterialTheme.colorScheme.error
                )
                else -> Triple(
                    Icons.Default.PlayArrow,
                    "Start ${config.title}",
                    MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    if (isOngoing) {
                        onAlternateAction()
                    } else {
                        onPrimaryAction()
                    }
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = buttonIcon,
                        contentDescription = buttonText,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Timer for ongoing activities
            if (!config.isImmediateActivity && state.isOngoing && state.currentElapsedTime > 0) {
                Text(
                    text = formatElapsedTime(state.currentElapsedTime),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            // Content area with error handling
            when {
                state.contentError != null -> {
                    CompactErrorDisplay(
                        errorMessage = state.contentError,
                        onDismiss = onDismissError,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                state.isLoadingContent -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContentClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        ActivityCardContentDisplay(
                            config = config,
                            content = content
                        )
                    }
                }
            }

            // Success message
            state.successMessage?.let { successMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = successMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Clear success after showing it
                LaunchedEffect(successMessage) {
                    kotlinx.coroutines.delay(3000)
                    onDismissSuccess()
                }
            }
            
            // Error message
            state.errorMessage?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Clear error after showing it
                LaunchedEffect(errorMessage) {
                    kotlinx.coroutines.delay(3000)
                    onDismissError()
                }
            }
        }
    }
}

/**
 * Display content for activity cards based on configuration
 */
@Composable
private fun ActivityCardContentDisplay(
    config: ActivityCardConfig,
    content: ActivityCardContent
) {
    when {
        // Ongoing activity state (for non-immediate activities)
        !config.isImmediateActivity && content.ongoingActivity != null -> {
            content.ongoingStartTime?.let { startTime ->
                val startTimeText = "Started at ${formatTime(startTime)}"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { modifier ->
                            if (content.onEditStartTime != null) {
                                modifier.clickable { content.onEditStartTime.invoke() }
                            } else modifier
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (content.onEditStartTime != null) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit start time",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = startTimeText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Last activity state
        content.lastActivity != null -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                // Main activity description with edit icon
                content.lastActivityText?.let { activityText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activityText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit last activity",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                
                // Time ago
                content.lastActivityTimeAgo?.let { timeAgo ->
                    Text(
                        text = timeAgo,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Activity time
                content.lastActivityTime?.let { activityTime ->
                    val timeText = if (config.isImmediateActivity) {
                        "at ${formatTime(activityTime)}"
                    } else {
                        "Ended at ${formatTime(activityTime)}"
                    }
                    Text(
                        text = timeText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Notes if available
                content.lastActivityNotes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Text(
                            text = "\"$notes\"",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
        
        // No activity state
        else -> {
            Text(
                text = content.noActivityText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Format elapsed time in seconds to a human-readable string
 */
private fun formatElapsedTime(elapsedTimeSeconds: Long): String {
    val hours = elapsedTimeSeconds / 3600
    val minutes = (elapsedTimeSeconds % 3600) / 60
    val seconds = elapsedTimeSeconds % 60
    
    return when {
        hours > 0 -> "%02d:%02d:%02d".format(hours, minutes, seconds)
        else -> "%02d:%02d".format(minutes, seconds)
    }
}

/**
 * Helper function to create configuration for sleep activity
 */
fun sleepActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = "Sleep",
    icon = "😴",
    isImmediateActivity = false
)

/**
 * Helper function to create configuration for breast feeding activity
 */
fun breastFeedingActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = "Boob",
    icon = "🤱",
    isImmediateActivity = false
)

/**
 * Helper function to create configuration for bottle feeding activity
 */
fun bottleFeedingActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = "Bottle",
    icon = "🍼",
    isImmediateActivity = true
)

/**
 * Helper function to create configuration for diaper activity
 */
fun diaperActivityConfig(): ActivityCardConfig = ActivityCardConfig(
    title = "Poop",
    icon = "💩",
    isImmediateActivity = true
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
    feedingTime: Date? = null
): ActivityCardContent = ActivityCardContent(
    lastActivity = lastFeeding,
    lastActivityText = lastFeedingText,
    lastActivityTimeAgo = timeAgo,
    lastActivityTime = feedingTime,
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

/**
 * Format a Date to display time in HH:mm format
 */
private fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}
