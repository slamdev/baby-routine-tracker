package com.github.slamdev.babyroutinetracker.sleep

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.ui.components.ActivityCard
import com.github.slamdev.babyroutinetracker.ui.components.ActivityCardState
import com.github.slamdev.babyroutinetracker.ui.components.EditActivityDialog
import com.github.slamdev.babyroutinetracker.ui.components.formatters.TimeUtils
import com.github.slamdev.babyroutinetracker.ui.components.helpers.sleepActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.helpers.sleepActivityContent
import java.util.*

@Composable
fun SleepTrackingCard(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: SleepTrackingViewModel = viewModel()
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
        isOngoing = uiState.ongoingSleep != null,
        errorMessage = uiState.errorMessage,
        currentElapsedTime = uiState.currentElapsedTime,
        isLoadingContent = uiState.isLoadingLastSleep || uiState.isLoadingOngoingSleep,
        contentError = uiState.lastSleepError ?: uiState.ongoingSleepError
    )

    // Prepare content based on current state
    val ongoingSleep = uiState.ongoingSleep
    val lastSleep = uiState.lastSleep

    val cardContent = when {
        ongoingSleep != null -> {
            sleepActivityContent(
                ongoingSleep = ongoingSleep,
                ongoingStartTime = ongoingSleep.startTime.toDate(),
                onEditStartTime = { showEditStartTimeDialog = true }
            )
        }
        lastSleep != null && lastSleep.endTime != null -> {
            val duration = lastSleep.getDurationMinutes()
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
                    lastSleep.startTime.toDate(),
                    lastSleep.endTime?.toDate()
                )
            )

            sleepActivityContent(
                lastSleep = lastSleep,
                lastSleepText = "Last slept $durationText",
                timeAgo = timeAgo,
                endTime = lastSleep.endTime.toDate()
            )
        }
        else -> {
            sleepActivityContent() // Empty content
        }
    }

    ActivityCard(
        config = sleepActivityConfig(),
        state = cardState,
        content = cardContent,
        modifier = modifier,
        onPrimaryAction = { viewModel.startSleep() },
        onAlternateAction = { viewModel.endSleep() },
        onContentClick = {
            uiState.lastSleep?.let { showEditLastActivityDialog = true }
        },
        onDismissError = { viewModel.clearError() },
        onDismissSuccess = { viewModel.clearSuccessMessage() }
    )

    val ongoingSleepToEdit = uiState.ongoingSleep
    if (showEditStartTimeDialog && ongoingSleepToEdit != null) {
        EditActivityDialog(
            activity = ongoingSleepToEdit,
            onDismiss = { showEditStartTimeDialog = false },
            onSave = { activity, newStartTime, newEndTime, newNotes ->
                viewModel.updateSleepStartTime(newStartTime)
                showEditStartTimeDialog = false
            }
        )
    }

    val lastSleepToEdit = uiState.lastSleep
    if (showEditLastActivityDialog && lastSleepToEdit != null) {
        EditActivityDialog(
            activity = lastSleepToEdit,
            onDismiss = { showEditLastActivityDialog = false },
            onSave = { activity, newStartTime, newEndTime, newNotes ->
                viewModel.updateCompletedSleep(
                    activity.copy(
                        startTime = com.google.firebase.Timestamp(newStartTime),
                        endTime = newEndTime?.let { com.google.firebase.Timestamp(it) },
                        notes = newNotes ?: ""
                    )
                )
                showEditLastActivityDialog = false
            }
        )
    }
}
