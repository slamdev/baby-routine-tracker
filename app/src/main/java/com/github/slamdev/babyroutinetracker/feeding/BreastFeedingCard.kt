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
import com.github.slamdev.babyroutinetracker.ui.components.breastFeedingActivityContent
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
    
    // Prepare content based on current state
    val ongoingBreastFeeding = uiState.ongoingBreastFeeding
    val lastFeeding = uiState.lastFeeding?.takeIf { it.feedingType == "breast_milk" }
    
    val cardContent = when {
        ongoingBreastFeeding != null -> {
            breastFeedingActivityContent(
                ongoingFeeding = ongoingBreastFeeding,
                ongoingStartTime = ongoingBreastFeeding.startTime.toDate(),
                onEditStartTime = { showTimePickerDialog = true }
            )
        }
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
            
            val timeAgo = TimeUtils.formatTimeAgo(
                TimeUtils.getRelevantTimestamp(
                    lastFeeding.startTime.toDate(),
                    lastFeeding.endTime?.toDate()
                )
            )
            
            breastFeedingActivityContent(
                lastFeeding = lastFeeding,
                lastFeedingText = "Last fed $durationText",
                timeAgo = timeAgo,
                endTime = lastFeeding.endTime.toDate()
            )
        }
        else -> {
            breastFeedingActivityContent() // Empty content
        }
    }
    
    ActivityCard(
        config = breastFeedingActivityConfig(),
        state = cardState,
        content = cardContent,
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
    )
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

// Helper function to format time
private fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}
