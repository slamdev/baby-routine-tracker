package com.github.slamdev.babyroutinetracker.sleep

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.ui.components.CompactErrorDisplay
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
    
    // Show error message if any
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            // Error will be cleared automatically after being shown
            // In a production app, you might want to show a snackbar here
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.ongoingSleep != null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
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
            Text(
                text = "😴 Sleep",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // Action button
            val isOngoingSleep = uiState.ongoingSleep != null
            Button(
                onClick = {
                    if (isOngoingSleep) {
                        viewModel.endSleep()
                    } else {
                        viewModel.startSleep()
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOngoingSleep) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isOngoingSleep) Icons.Default.Check else Icons.Default.PlayArrow,
                        contentDescription = if (isOngoingSleep) "Stop Sleep" else "Start Sleep",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Current status and timer with error handling
            val ongoingSleepError = uiState.ongoingSleepError
            val ongoingSleep = uiState.ongoingSleep
            when {
                ongoingSleepError != null -> {
                    CompactErrorDisplay(
                        errorMessage = ongoingSleepError,
                        onDismiss = { viewModel.clearOngoingSleepError() },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                uiState.isLoadingOngoingSleep -> {
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
                            text = "Loading sleep status...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                ongoingSleep != null -> {
                    // Timer display
                    Text(
                        text = viewModel.formatElapsedTime(uiState.currentElapsedTime),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    
                    // Start time (clickable for editing)
                    Row(
                        modifier = Modifier
                            .clickable { showTimePickerDialog = true }
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
                    // Show last sleep with error handling
                    val lastSleepError = uiState.lastSleepError
                    val lastSleep = uiState.lastSleep
                    when {
                        lastSleepError != null -> {
                            CompactErrorDisplay(
                                errorMessage = lastSleepError,
                                onDismiss = { viewModel.clearLastSleepError() },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        uiState.isLoadingLastSleep -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Loading last sleep...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                        lastSleep != null && lastSleep.endTime != null -> {
                            val duration = lastSleep.getDurationMinutes()
                            val durationText = if (duration != null) {
                                val hours = duration / 60
                                val minutes = duration % 60
                                if (hours > 0) {
                                    "${hours}h ${minutes}m"
                                } else {
                                    "${minutes}m"
                                }
                            } else {
                                "Unknown duration"
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
                                modifier = Modifier
                                    .clickable { showEditLastActivityDialog = true }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Last sleep: $durationText",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
            
            // Error message
            uiState.errorMessage?.let { errorMessage ->
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
                    viewModel.clearError()
                }
            }
        }
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
                onDismiss = {
                    showTimePickerDialog = false
                }
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
                },
                onSaveInstantTimeChange = { _, _ ->
                    // Sleep activities are never instant, so this won't be called
                }
            )
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
