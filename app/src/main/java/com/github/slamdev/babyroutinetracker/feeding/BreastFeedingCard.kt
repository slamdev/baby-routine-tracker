package com.github.slamdev.babyroutinetracker.feeding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.ui.components.CompactErrorDisplay
import com.github.slamdev.babyroutinetracker.ui.components.TimePickerDialog
import com.github.slamdev.babyroutinetracker.ui.components.EditActivityDialog
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
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.ongoingBreastFeeding != null) {
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
                text = "🤱 Boob",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            // Last breast feeding info (only show breast milk feedings)
            val lastFeeding = uiState.lastFeeding?.takeIf { it.feedingType == "breast_milk" }
            when {
                uiState.isLoadingLastFeeding -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
                lastFeeding != null && lastFeeding.endTime != null -> {
                    val duration = lastFeeding.getDurationMinutes() ?: 0
                    
                    // Last feeding info (clickable for editing)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showEditLastActivityDialog = true }
                            .padding(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${duration}min",
                                fontSize = 14.sp,
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
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Breast milk feeding section
            BreastMilkFeedingSection(
                uiState = uiState,
                onStartBreastFeeding = { viewModel.startBreastMilkFeeding() },
                onStopBreastFeeding = { viewModel.endBreastMilkFeeding() },
                onClearOngoingError = { viewModel.clearOngoingFeedingError() },
                onEditStartTime = { showTimePickerDialog = true },
                formatElapsedTime = { viewModel.formatElapsedTime(it) }
            )
            
            // Error display
            uiState.ongoingFeedingError?.let { errorMessage ->
                CompactErrorDisplay(
                    errorMessage = errorMessage,
                    onDismiss = { viewModel.clearOngoingFeedingError() },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
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
                onDismiss = {
                    showTimePickerDialog = false
                }
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

@Composable
private fun BreastMilkFeedingSection(
    uiState: FeedingTrackingUiState,
    onStartBreastFeeding: () -> Unit,
    onStopBreastFeeding: () -> Unit,
    onClearOngoingError: () -> Unit,
    onEditStartTime: () -> Unit,
    formatElapsedTime: (Long) -> String
) {
    val ongoingBreastFeeding = uiState.ongoingBreastFeeding
    val isOngoing = ongoingBreastFeeding != null
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isOngoing && ongoingBreastFeeding != null) {
            // Show timer and controls for ongoing feeding
            Text(
                text = formatElapsedTime(uiState.currentElapsedTime),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Start time (clickable for editing)
            Text(
                text = "Started at ${formatTime(ongoingBreastFeeding.startTime.toDate())}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.clickable { onEditStartTime() }
            )
            
            // Stop button
            Button(
                onClick = onStopBreastFeeding,
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Feeding")
            }
        } else {
            // Start button
            Button(
                onClick = onStartBreastFeeding,
                modifier = Modifier.fillMaxWidth(0.8f),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Feeding",
                    )
                }
            }
        }
    }
}
