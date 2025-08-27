package com.github.slamdev.babyroutinetracker.feeding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.slamdev.babyroutinetracker.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.ui.components.*
import com.github.slamdev.babyroutinetracker.ui.components.formatters.TimeUtils
import com.github.slamdev.babyroutinetracker.ui.components.helpers.breastFeedingActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.helpers.breastFeedingActivityContent
import java.util.*

@Composable
fun BreastFeedingCard(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: FeedingTrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditStartTimeDialog by remember { mutableStateOf(false) }
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
                onEditStartTime = { showEditStartTimeDialog = true }
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
                lastFeedingText = stringResource(R.string.last_fed_format, durationText),
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
        onDismissError = { viewModel.clearError() },
        onDismissSuccess = { viewModel.clearSuccessMessage() }
    )

    val ongoingFeeding = uiState.ongoingBreastFeeding
    if (showEditStartTimeDialog && ongoingFeeding != null) {
        EditActivityDialog(
            activity = ongoingFeeding,
            onDismiss = { showEditStartTimeDialog = false },
            onSave = { activity, newStartTime, newEndTime, newNotes ->
                viewModel.updateBreastFeedingStartTime(newStartTime)
                showEditStartTimeDialog = false
            }
        )
    }

    val lastFeedingToEdit = uiState.lastFeeding
    if (showEditLastActivityDialog && lastFeedingToEdit != null) {
        EditActivityDialog(
            activity = lastFeedingToEdit,
            onDismiss = { showEditLastActivityDialog = false },
            onSave = { activity, newStartTime, newEndTime, newNotes ->
                viewModel.updateCompletedFeeding(activity, newStartTime, newEndTime, newNotes)
                showEditLastActivityDialog = false
            }
        )
    }
}
