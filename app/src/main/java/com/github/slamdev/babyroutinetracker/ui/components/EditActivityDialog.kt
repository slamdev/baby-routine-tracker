package com.github.slamdev.babyroutinetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityDialog(
    activity: Activity,
    onDismiss: () -> Unit,
    onSave: (Activity, Date, Date?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showInstantDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showInstantTimePicker by remember { mutableStateOf(false) }
    
    var startTime by remember { mutableStateOf(activity.startTime.toDate()) }
    var endTime by remember { mutableStateOf(activity.endTime?.toDate()) }
    var instantTime by remember { mutableStateOf(activity.startTime.toDate()) }
    var notes by remember { mutableStateOf(activity.notes) }

    // Date pickers state
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startTime.time
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endTime?.time ?: startTime.time
    )
    val instantDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = instantTime.time
    )

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
            Text(stringResource(R.string.title_edit_activity, activity.type.displayName))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Only show time editing for completed activities
                if (activity.endTime != null) {
                    Text(
                        text = "Date & Time",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (isInstantActivity) {
                        // For instant activities (bottle feeding, diaper), show single date/time pickers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date picker
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                onClick = { showInstantDatePicker = true }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Date",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = formatDate(instantTime),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            // Time picker
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                onClick = { showInstantTimePicker = true }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Time",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = formatTime(instantTime),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        // For duration activities (sleep, breast feeding), show start and end date/time pickers
                        // Start date/time
                        Text(
                            text = "Start",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                onClick = { showStartDatePicker = true }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Date",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = formatDate(startTime),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                onClick = { showStartTimePicker = true }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Time",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = formatTime(startTime),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // End date/time
                        Text(
                            text = "End",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                onClick = { showEndDatePicker = true }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Date",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = formatDate(endTime ?: Date()),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                onClick = { showEndTimePicker = true }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Time",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = formatTime(endTime ?: Date()),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
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
                        label = { Text(stringResource(R.string.label_notes)) },
                        placeholder = { Text(stringResource(R.string.placeholder_notes)) },
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
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )

    // Date Picker Dialogs
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Date(millis)
                            // Combine selected date with current time
                            val calendar = Calendar.getInstance().apply {
                                time = startTime
                            }
                            val newCalendar = Calendar.getInstance().apply {
                                time = selectedDate
                                set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                                set(Calendar.SECOND, calendar.get(Calendar.SECOND))
                            }
                            startTime = newCalendar.time
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker = false }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(
                state = startDatePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Date(millis)
                            // Combine selected date with current time
                            val calendar = Calendar.getInstance().apply {
                                time = endTime ?: Date()
                            }
                            val newCalendar = Calendar.getInstance().apply {
                                time = selectedDate
                                set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                                set(Calendar.SECOND, calendar.get(Calendar.SECOND))
                            }
                            endTime = newCalendar.time
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(
                state = endDatePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showInstantDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showInstantDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        instantDatePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Date(millis)
                            // Combine selected date with current time
                            val calendar = Calendar.getInstance().apply {
                                time = instantTime
                            }
                            val newCalendar = Calendar.getInstance().apply {
                                time = selectedDate
                                set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                                set(Calendar.SECOND, calendar.get(Calendar.SECOND))
                            }
                            instantTime = newCalendar.time
                        }
                        showInstantDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showInstantDatePicker = false }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(
                state = instantDatePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Time Picker Dialogs
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
                time = initialTime
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

private fun formatTime(date: Date): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(date)
}
