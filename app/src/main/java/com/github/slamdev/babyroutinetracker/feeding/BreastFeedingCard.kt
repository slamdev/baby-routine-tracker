package com.github.slamdev.babyroutinetracker.feeding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.ui.components.ActivityCard
import com.github.slamdev.babyroutinetracker.ui.components.ActivityCardState
import com.github.slamdev.babyroutinetracker.ui.components.breastFeedingActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.CompactErrorDisplay
import com.github.slamdev.babyroutinetracker.ui.components.TimePickerDialog
import com.github.slamdev.babyroutinetracker.ui.components.EditActivityDialog
import com.github.slamdev.babyroutinetracker.ui.components.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BreastFeedingCard(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: FeedingTrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showEditLastActivityDialog by remember { mutableStateOf(false) }
    
    // Initialize the ViewModel for this baby
    LaunchedEffect(babyId) {
        viewModel.initialize(babyId)
    }
    
    // Activity card state
    val cardState = ActivityCardState(
        isLoading = uiState.isLoading,
        isOngoing = uiState.ongoingBreastFeeding != null,
        errorMessage = uiState.errorMessage,
        currentElapsedTime = uiState.currentElapsedTime,
        isLoadingContent = uiState.isLoadingLastFeeding || uiState.isLoadingOngoingFeeding,
        contentError = uiState.lastFeedingError ?: uiState.ongoingFeedingError
    )
    
    ActivityCard(
        config = breastFeedingActivityConfig(),
        state = cardState,
        modifier = modifier,
        onPrimaryAction = { viewModel.startBreastMilkFeeding() },
        onAlternateAction = { viewModel.endBreastMilkFeeding() },
        onContentClick = { 
            // Only show edit dialog for completed breast feedings
            uiState.lastFeeding?.takeIf { 
                it.feedingType == "breast_milk" && it.endTime != null 
            }?.let { showEditLastActivityDialog = true }
        },
        onDismissError = { 
            uiState.lastFeedingError?.let { viewModel.clearLastFeedingError() }
            uiState.ongoingFeedingError?.let { viewModel.clearOngoingFeedingError() }
            uiState.errorMessage?.let { viewModel.clearError() }
        }
    ) {
        BreastFeedingContent(
            uiState = uiState,
            onEditStartTime = { showTimePickerDialog = true }
        )
    }
    // Time picker dialog for editing breast feeding start time
    if (showTimePickerDialog) {
        uiState.ongoingBreastFeeding?.let { ongoingFeeding ->
            TimePickerDialog(
                title = "Edit Feeding Start Time",
                initialTime = ongoingFeeding.startTime.toDate(),
                onTimeSelected = { newTime ->
                    viewModel.updateStartTime(newTime)
                    showTimePickerDialog = false
                },
                onDismiss = { showTimePickerDialog = false }
            )
        }
    }
    
    // Edit dialog for last completed feeding activity
    if (showEditLastActivityDialog) {
        val lastFeeding = uiState.lastFeeding?.takeIf { it.feedingType == "breast_milk" }
        lastFeeding?.let { lastFeedingActivity ->
            EditActivityDialog(
                activity = lastFeedingActivity,
                onDismiss = {
                    showEditLastActivityDialog = false
                },
                onSaveTimeChanges = { activity, newStartTime, newEndTime ->
                    viewModel.updateCompletedActivityTimes(activity, newStartTime, newEndTime)
                    showEditLastActivityDialog = false
                },
                onSaveNotesChanges = { activity, newNotes ->
                    viewModel.updateCompletedActivityNotes(activity, newNotes)
                    showEditLastActivityDialog = false
                },
                onSaveInstantTimeChange = { activity, newTime ->
                    viewModel.updateInstantActivityTime(activity, newTime)
                    showEditLastActivityDialog = false
                }
            )
        }
    }
}

@Composable
private fun BreastFeedingContent(
    uiState: FeedingTrackingUiState,
    onEditStartTime: () -> Unit
) {
    val ongoingBreastFeeding = uiState.ongoingBreastFeeding
    when {
        ongoingBreastFeeding != null -> {
            // Start time (clickable for editing)
            Row(
                modifier = Modifier
                    .clickable { onEditStartTime() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit start time",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Started at ${formatTime(ongoingBreastFeeding.startTime.toDate())}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        else -> {
            // Show last breast feeding
            val lastFeeding = uiState.lastFeeding?.takeIf { it.feedingType == "breast_milk" }
            when {
                lastFeeding != null && lastFeeding.endTime != null -> {
                    val duration = lastFeeding.getDurationMinutes()
                    val durationText = if (duration != null) {
                        val hours = duration / 60
                        val minutes = duration % 60
                        when {
                            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                            hours > 0 -> "${hours}h"
                            else -> "${minutes}m"
                        }
                    } else {
                        "Duration unknown"
                    }
                    
                    // Calculate time ago
                    val timeAgo = TimeUtils.formatTimeAgo(
                        TimeUtils.getRelevantTimestamp(
                            lastFeeding.startTime.toDate(),
                            lastFeeding.endTime?.toDate()
                        )
                    )
                    
                    // Last feeding info (clickable for editing)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last fed $durationText",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit last feeding",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        
                        Text(
                            text = timeAgo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        
                        lastFeeding.endTime?.let { endTime ->
                            Text(
                                text = "Ended at ${formatTime(endTime.toDate())}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = "No recent feeding",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// Helper function to format time
private fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}
