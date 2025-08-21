package com.github.slamdev.babyroutinetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditActivityDialog(
    activity: Activity,
    onDismiss: () -> Unit,
    onSaveTimeChanges: (Activity, Date, Date) -> Unit,
    onSaveNotesChanges: (Activity, String) -> Unit,
    onSaveInstantTimeChange: (Activity, Date) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showInstantTimePicker by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(activity.startTime.toDate()) }
    var endTime by remember { mutableStateOf(activity.endTime?.toDate() ?: Date()) }
    var instantTime by remember { mutableStateOf(activity.startTime.toDate()) }
    var notes by remember { mutableStateOf(activity.notes) }
    
    // Check if this activity type supports notes
    val supportsNotes = activity.type == ActivityType.FEEDING && activity.feedingType == "bottle" ||
                       activity.type == ActivityType.DIAPER
    
    // Check if this is an instant activity (bottle feeding or diaper)
    val isInstantActivity = activity.isInstantActivity()
    
    // Validation
    val isValidTimes = if (isInstantActivity) {
        true // Instant activities always valid
    } else {
        startTime <= endTime // Allow start time to be equal to end time
    }
    
    val hasTimeChanges = if (isInstantActivity) {
        instantTime != activity.startTime.toDate()
    } else {
        startTime != activity.startTime.toDate() || 
        (activity.endTime != null && endTime != activity.endTime.toDate())
    }
    
    val hasNotesChanges = notes != activity.notes
    val hasChanges = hasTimeChanges || hasNotesChanges
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text("Edit ${activity.type.displayName}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Only show time editing for completed activities
                if (activity.endTime != null) {
                    Text(
                        text = "Time",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (isInstantActivity) {
                        // For instant activities (bottle feeding, diaper), show single time picker
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showInstantTimePicker = true }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (activity.type) {
                                        ActivityType.DIAPER -> "Diaper Change Time"
                                        ActivityType.FEEDING -> "Feeding Time"
                                        else -> "Activity Time"
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = formatDateTime(instantTime),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // For duration activities (sleep, breast feeding), show start and end time pickers
                        // Start time picker
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showStartTimePicker = true }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Start Time",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = formatDateTime(startTime),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // End time picker
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showEndTimePicker = true }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "End Time",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = formatDateTime(endTime),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Validation error
                        if (!isValidTimes) {
                            Text(
                                text = "Start time cannot be after end time",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Notes editing (only for supported activity types)
                if (supportsNotes) {
                    if (activity.endTime != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        text = "Notes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        placeholder = { Text("Any additional details...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
                
                // Show message if no editable fields
                if (activity.endTime == null && !supportsNotes) {
                    Text(
                        text = "Only start time can be edited for ongoing activities. Use the edit icon on the main card.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Save changes based on what changed and activity type
                    when {
                        hasTimeChanges && hasNotesChanges -> {
                            if (isInstantActivity) {
                                // For instant activities, update single timestamp and notes
                                onSaveInstantTimeChange(activity, instantTime)
                                onSaveNotesChanges(activity, notes)
                            } else {
                                // For duration activities, update start/end times and notes
                                onSaveTimeChanges(activity, startTime, endTime)
                                onSaveNotesChanges(activity, notes)
                            }
                        }
                        hasTimeChanges -> {
                            if (isInstantActivity) {
                                onSaveInstantTimeChange(activity, instantTime)
                            } else {
                                onSaveTimeChanges(activity, startTime, endTime)
                            }
                        }
                        hasNotesChanges -> {
                            onSaveNotesChanges(activity, notes)
                        }
                    }
                },
                enabled = hasChanges && (activity.endTime == null || isValidTimes)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Time picker dialogs
    if (showStartTimePicker) {
        TimePickerDialog(
            title = "Select Start Time",
            initialTime = startTime,
            onTimeSelected = { newTime ->
                startTime = newTime
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            title = "Select End Time",
            initialTime = endTime,
            onTimeSelected = { newTime ->
                endTime = newTime
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
    
    if (showInstantTimePicker) {
        TimePickerDialog(
            title = when (activity.type) {
                ActivityType.DIAPER -> "Select Diaper Change Time"
                ActivityType.FEEDING -> "Select Feeding Time"
                else -> "Select Activity Time"
            },
            initialTime = instantTime,
            onTimeSelected = { newTime ->
                instantTime = newTime
                showInstantTimePicker = false
            },
            onDismiss = { showInstantTimePicker = false }
        )
    }
}

/**
 * Format a Date to display date and time
 */
private fun formatDateTime(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(date)
}
