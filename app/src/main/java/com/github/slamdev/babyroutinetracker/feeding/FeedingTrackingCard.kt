package com.github.slamdev.babyroutinetracker.feeding

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.ui.components.CompactErrorDisplay
import com.github.slamdev.babyroutinetracker.ui.components.TimePickerDialog
import com.github.slamdev.babyroutinetracker.ui.components.EditActivityDialog
import com.github.slamdev.babyroutinetracker.ui.components.helpers.breastFeedingActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.helpers.breastFeedingActivityContent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedingTrackingCard(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: FeedingTrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottleFeedingDialog by remember { mutableStateOf(false) }
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = stringResource(R.string.feeding_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            // Last feeding info with error handling
            val lastFeedingError = uiState.lastFeedingError
            val lastFeeding = uiState.lastFeeding
            when {
                lastFeedingError != null -> {
                    CompactErrorDisplay(
                        errorMessage = lastFeedingError,
                        onDismiss = { viewModel.clearLastFeedingError() },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                uiState.isLoadingLastFeeding -> {
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
                            text = stringResource(R.string.loading_last_feeding),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                lastFeeding != null && lastFeeding.endTime != null -> {
                    // Format feeding details
                    val feedingDetails = when (lastFeeding.feedingType) {
                        "breast_milk" -> {
                            val duration = lastFeeding.getDurationMinutes() ?: 0
                            "Breast milk - ${duration}min"
                        }
                        "bottle" -> {
                            val amount = lastFeeding.amount.toInt()
                            "Bottle - ${amount}ml"
                        }
                        else -> "Feeding completed"
                    }
                    
                    // Last feeding info (clickable for editing)
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
                                text = stringResource(R.string.last_feeding_format, feedingDetails),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.content_desc_edit_last_feeding),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        
                        lastFeeding.endTime?.let { endTime ->
                            val timeText = if (lastFeeding.feedingType == "bottle") {
                                "at ${formatTime(endTime.toDate())}"  // Instant activity
                            } else {
                                "Ended at ${formatTime(endTime.toDate())}"  // Duration activity
                            }
                            Text(
                                text = timeText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Show notes if available for bottle feeding
                        if (lastFeeding.feedingType == "bottle" && lastFeeding.notes.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.activity_notes_format, lastFeeding.notes),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = stringResource(R.string.no_recent_feeding),
                        fontSize = 14.sp,
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
            
            HorizontalDivider()
            
            // Bottle feeding section
            BottleFeedingSection(
                isLoading = uiState.isLoading,
                onLogBottleFeeding = { showBottleFeedingDialog = true }
            )
            
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
    
    // Bottle feeding input dialog
    if (showBottleFeedingDialog) {
        BottleFeedingDialog(
            onDismiss = { showBottleFeedingDialog = false },
            onConfirm = { amount, notes ->
                viewModel.logBottleFeeding(amount, notes)
                showBottleFeedingDialog = false
            }
        )
    }
    
    // Time picker dialog for editing breast feeding start time
    if (showTimePickerDialog) {
        uiState.ongoingBreastFeeding?.let { ongoingFeeding ->
            TimePickerDialog(
                title = stringResource(R.string.edit_feeding_start_time_title),
                initialTime = ongoingFeeding.startTime.toDate(),
                onTimeSelected = { newTime ->
                    viewModel.updateBreastFeedingStartTime(newTime)
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
        uiState.lastFeeding?.let { lastFeeding ->
            EditActivityDialog(
                activity = lastFeeding,
                onDismiss = {
                    showEditLastActivityDialog = false
                },
                onSave = { activity, newStartTime, newEndTime, newNotes ->
                    viewModel.updateCompletedFeeding(activity, newStartTime, newEndTime, newNotes)
                    showEditLastActivityDialog = false
                }
            )
        }
    }
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
        Text(
            text = stringResource(R.string.breast_feeding_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Show ongoing feeding error if present
        val ongoingFeedingError = uiState.ongoingFeedingError
        if (ongoingFeedingError != null) {
            CompactErrorDisplay(
                errorMessage = ongoingFeedingError,
                onDismiss = onClearOngoingError,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else if (uiState.isLoadingOngoingFeeding) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.loading),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else if (isOngoing) {
            // Timer display
            Text(
                text = formatElapsedTime(uiState.currentElapsedTime),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
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
                    contentDescription = stringResource(R.string.content_desc_edit_start_time),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.started_at_format, formatTime(ongoingBreastFeeding!!.startTime.toDate())),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        Button(
            onClick = {
                if (isOngoing) {
                    onStopBreastFeeding()
                } else {
                    onStartBreastFeeding()
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOngoing) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOngoing) "Stopping..." else "Starting...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                if (isOngoing) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.content_desc_stop_breast_feeding),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stop_feeding),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.content_desc_start_breast_feeding),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.start_feeding),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BottleFeedingSection(
    isLoading: Boolean,
    onLogBottleFeeding: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.bottle_feeding_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedButton(
            onClick = onLogBottleFeeding,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.content_desc_log_bottle_feeding),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.log_bottle),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottleFeedingDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, notes: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isValidInput by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.log_bottle_feeding_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.amount_ml_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        isValidInput = it.isNotBlank() && it.toDoubleOrNull() != null && it.toDoubleOrNull()!! > 0
                    },
                    label = { Text(stringResource(R.string.label_amount)) },
                    placeholder = { Text(stringResource(R.string.placeholder_amount)) },
                    suffix = { Text(stringResource(R.string.suffix_ml)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isValidInput && amount.isNotBlank()
                )
                
                // Notes (optional)
                Text(
                    text = stringResource(R.string.notes_optional_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.label_notes)) },
                    placeholder = { Text(stringResource(R.string.placeholder_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    onConfirm(amountDouble, notes)
                },
                enabled = amount.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
            ) {
                Text(stringResource(R.string.action_log_bottle))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

/**
 * Format a Date to display time in HH:mm format
 */
private fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}
