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
    onSave: (Activity, Date, Date?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showInstantTimePicker by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(activity.startTime.toDate()) }
    var endTime by remember { mutableStateOf(activity.endTime?.toDate()) }
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
        endTime?.let { startTime <= it } ?: true // Allow start time to be equal to end time
    }

    val hasTimeChanges = if (isInstantActivity) {
        instantTime != activity.startTime.toDate()
    } else {
        startTime != activity.startTime.toDate() ||
                (activity.endTime != null && endTime != activity.endTime?.toDate())
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
                                    text = formatDateTime(endTime ?: Date()),
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
            Button(
                onClick = {
                    val finalEndTime = if (isInstantActivity) null else endTime
                    onSave(activity, if (isInstantActivity) instantTime else startTime, finalEndTime, notes)
                    onDismiss()
                },
                enabled = hasChanges && isValidTimes
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

    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                startTime = it
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = endTime ?: Date(),
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                endTime = it
                showEndTimePicker = false
            }
        )
    }

    if (showInstantTimePicker) {
        TimePickerDialog(
            initialTime = instantTime,
            onDismiss = { showInstantTimePicker = false },
            onConfirm = {
                instantTime = it
                showInstantTimePicker = false
            }
        )
    }
}

@Composable
private fun TimePickerDialog(
    initialTime: Date,
    onDismiss: () -> Unit,
    onConfirm: (Date) -> Unit
) {
    val calendar = Calendar.getInstance().apply { time = initialTime }
    val timePickerDialog = android.app.TimePickerDialog(
        androidx.compose.ui.platform.LocalContext.current,
        { _, hourOfDay, minute ->
            val newTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }.time
            onConfirm(newTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    DisposableEffect(Unit) {
        timePickerDialog.show()
        onDispose {
            timePickerDialog.dismiss()
        }
    }
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(date)
}
