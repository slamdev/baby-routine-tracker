package com.github.slamdev.babyroutinetracker.sleep

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
import com.github.slamdev.babyroutinetracker.ui.components.sleepActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.TimePickerDialog
import com.github.slamdev.babyroutinetracker.ui.components.EditActivityDialog
import com.github.slamdev.babyroutinetracker.ui.components.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SleepTrackingCard(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: SleepTrackingViewModel = viewModel()
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
        isOngoing = uiState.ongoingSleep != null,
        errorMessage = uiState.errorMessage,
        currentElapsedTime = uiState.currentElapsedTime,
        isLoadingContent = uiState.isLoadingLastSleep || uiState.isLoadingOngoingSleep,
        contentError = uiState.lastSleepError ?: uiState.ongoingSleepError
    )
    
    ActivityCard(
        config = sleepActivityConfig(),
        state = cardState,
        modifier = modifier,
        onPrimaryAction = { viewModel.startSleep() },
        onAlternateAction = { viewModel.endSleep() },
        onContentClick = { 
            uiState.lastSleep?.let { showEditLastActivityDialog = true }
        },
        onDismissError = { 
            uiState.lastSleepError?.let { viewModel.clearLastSleepError() }
            uiState.ongoingSleepError?.let { viewModel.clearOngoingSleepError() }
            uiState.errorMessage?.let { viewModel.clearError() }
        }
    ) {
        SleepContent(
            uiState = uiState,
            onEditStartTime = { showTimePickerDialog = true }
        )
    }
    
    // Time picker dialog for editing ongoing sleep start time
    if (showTimePickerDialog) {
        uiState.ongoingSleep?.let { ongoingSleep ->
            TimePickerDialog(
                title = "Edit Sleep Start Time",
                initialTime = ongoingSleep.startTime.toDate(),
                onTimeSelected = { newTime ->
                    viewModel.updateStartTime(newTime)
                    showTimePickerDialog = false
                },
                onDismiss = { showTimePickerDialog = false }
            )
        }
    }
    
    // Edit dialog for last completed sleep activity
    if (showEditLastActivityDialog) {
        uiState.lastSleep?.let { lastSleep ->
            EditActivityDialog(
                activity = lastSleep,
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
                }
            )
        }
    }
}

@Composable
private fun SleepContent(
    uiState: SleepTrackingUiState,
    onEditStartTime: () -> Unit
) {
    val ongoingSleep = uiState.ongoingSleep
    when {
        ongoingSleep != null -> {
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
                    text = "Started at ${formatTime(ongoingSleep.startTime.toDate())}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        else -> {
            // Show last sleep
            val lastSleep = uiState.lastSleep
            when {
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
                    
                    // Calculate time ago
                    val timeAgo = TimeUtils.formatTimeAgo(
                        TimeUtils.getRelevantTimestamp(
                            lastSleep.startTime.toDate(),
                            lastSleep.endTime?.toDate()
                        )
                    )
                    
                    // Last sleep info (clickable for editing)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last slept $durationText",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit last sleep",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        
                        Text(
                            text = timeAgo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        
                        lastSleep.endTime?.let { endTime ->
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
                        text = "No recent sleep",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Format a Date to display time in HH:mm format
 */
private fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}
