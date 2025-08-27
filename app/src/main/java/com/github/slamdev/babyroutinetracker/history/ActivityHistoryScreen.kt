package com.github.slamdev.babyroutinetracker.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.ui.components.CompactErrorDisplay
import com.github.slamdev.babyroutinetracker.ui.components.EditActivityDialog
import com.github.slamdev.babyroutinetracker.ui.theme.ActivityColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    babyId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityHistoryViewModel = viewModel()
) {
    // Initialize the ViewModel for this baby
    LaunchedEffect(babyId) {
        viewModel.initialize(babyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        ActivityHistoryContent(
            babyId = babyId,
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            viewModel = viewModel
        )
    }
}

@Composable
fun ActivityHistoryContent(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: ActivityHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedActivity by remember { mutableStateOf<Activity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    // Initialize the ViewModel for this baby
    LaunchedEffect(babyId) {
        viewModel.initialize(babyId)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.loading_activities))
                    }
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val errorMessage = uiState.errorMessage
                    if (errorMessage != null) {
                        CompactErrorDisplay(
                            errorMessage = errorMessage,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                }
            }
            uiState.activities.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_activities_found),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Activity type filter
                    ActivityTypeFilter(
                        selectedType = uiState.selectedActivityType,
                        onTypeSelected = { activityType ->
                            viewModel.filterByActivityType(activityType)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Activities list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredActivities) { activity ->
                            ActivityHistoryItem(
                                activity = activity,
                                onEditActivity = {
                                    selectedActivity = activity
                                    showEditDialog = true
                                },
                                onDeleteActivity = {
                                    viewModel.deleteActivity(activity)
                                }
                            )
                        }
                        
                        // Show message if no activities match filter
                        if (uiState.filteredActivities.isEmpty() && uiState.activities.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_activities_found),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Edit activity dialog
    if (showEditDialog && selectedActivity != null) {
        EditActivityDialog(
            activity = selectedActivity!!,
            onDismiss = {
                showEditDialog = false
                selectedActivity = null
            },
            onSave = { activity, newStartTime, newEndTime, newNotes ->
                viewModel.updateActivity(
                    activity.copy(
                        startTime = com.google.firebase.Timestamp(newStartTime),
                        endTime = newEndTime?.let { com.google.firebase.Timestamp(it) },
                        notes = newNotes ?: ""
                    )
                )
                showEditDialog = false
                selectedActivity = null
            }
        )
    }
}

@Composable
private fun ActivityHistoryItem(
    activity: Activity,
    onEditActivity: () -> Unit,
    onDeleteActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get background color based on activity type
    val backgroundColor = when (activity.type) {
        ActivityType.SLEEP -> ActivityColors.getSleepColor(isOngoing = activity.isOngoing())
        ActivityType.FEEDING -> ActivityColors.getFeedingColor(isOngoing = activity.isOngoing())
        ActivityType.DIAPER -> ActivityColors.getDiaperColor(isOngoing = activity.isOngoing())
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        // Responsive layout that adapts to screen width
        BoxWithConstraints {
            val isWideScreen = maxWidth > 600.dp
            
            if (isWideScreen) {
                // Landscape/wide layout: more horizontal organization
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Activity type and icon
                    Row(
                        modifier = Modifier.weight(0.25f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (activity.type) {
                                ActivityType.SLEEP -> "😴 Sleep"
                                ActivityType.FEEDING -> {
                                    if (activity.feedingType == "breast_milk") {
                                        "🤱 Breast"
                                    } else {
                                        "🍼 Bottle"
                                    }
                                }
                                ActivityType.DIAPER -> "💩 Diaper"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    // Time information
                    Row(
                        modifier = Modifier.weight(0.4f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActivityTimeInfo(activity = activity)
                    }
                    
                    // Additional details
                    Row(
                        modifier = Modifier.weight(0.2f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActivityDetailsInfo(activity = activity)
                    }
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.weight(0.15f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEditActivity
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit activity",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        var showDeleteConfirmation by remember { mutableStateOf(false) }
                        
                        IconButton(
                            onClick = { showDeleteConfirmation = true }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete activity",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        if (showDeleteConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmation = false },
                                title = { Text(stringResource(R.string.dialog_delete_title)) },
                                text = { Text(stringResource(R.string.dialog_delete_activity_message)) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            onDeleteActivity()
                                            showDeleteConfirmation = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text(stringResource(R.string.action_delete))
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDeleteConfirmation = false }
                                    ) {
                                        Text(stringResource(R.string.action_cancel))
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Portrait/narrow layout: more vertical organization
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Activity type and basic info
                        Text(
                            text = when (activity.type) {
                                ActivityType.SLEEP -> "😴 Sleep"
                                ActivityType.FEEDING -> {
                                    if (activity.feedingType == "breast_milk") {
                                        "🤱 Breast Feeding"
                                    } else {
                                        "🍼 Bottle Feeding"
                                    }
                                }
                                ActivityType.DIAPER -> "💩 Diaper Change"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        ActivityTimeInfo(activity = activity)
                        ActivityDetailsInfo(activity = activity)
                    }
                    
                    // Action buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = onEditActivity) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit activity",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        var showDeleteConfirmation by remember { mutableStateOf(false) }
                        
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete activity",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        if (showDeleteConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmation = false },
                                title = { Text(stringResource(R.string.dialog_delete_title)) },
                                text = { Text(stringResource(R.string.dialog_delete_activity_message)) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            onDeleteActivity()
                                            showDeleteConfirmation = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text(stringResource(R.string.action_delete))
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDeleteConfirmation = false }
                                    ) {
                                        Text(stringResource(R.string.action_cancel))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityTimeInfo(
    activity: Activity,
    modifier: Modifier = Modifier
) {
    // Time information
    val timeInfo = if (activity.endTime != null) {
        if (activity.isInstantActivity()) {
            // For instant activities (bottle feeding, diaper), show single timestamp
            "at ${formatTime(activity.startTime.toDate())}"
        } else {
            // For duration activities (sleep, breast feeding), show start-end with duration
            val startTime = formatTime(activity.startTime.toDate())
            val endTime = formatTime(activity.endTime.toDate())
            val duration = activity.getDurationMinutes()
            if (duration != null) {
                val hours = duration / 60
                val minutes = duration % 60
                val durationText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                "$startTime - $endTime ($durationText)"
            } else {
                "$startTime - $endTime"
            }
        }
    } else {
        "Started at ${formatTime(activity.startTime.toDate())} (ongoing)"
    }
    
    Text(
        text = timeInfo,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        modifier = modifier
    )
}

@Composable
private fun ActivityDetailsInfo(
    activity: Activity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Activity-specific details
        when (activity.type) {
            ActivityType.FEEDING -> {
                if (activity.feedingType == "bottle" && activity.amount > 0) {
                    Text(
                        text = stringResource(R.string.activity_amount_ml, activity.amount.toInt()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            else -> { /* No additional details for sleep/diaper */ }
        }
        
        // Notes if available
        if (activity.notes.isNotBlank()) {
            Text(
                text = stringResource(R.string.activity_notes_format, activity.notes),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActivityTypeFilter(
    selectedType: ActivityType?,
    onTypeSelected: (ActivityType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.filter_by_activity_type),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // All activities option
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                    label = { Text(stringResource(R.string.history_filter_all)) }
                )
            }
            
            // Individual activity types
            item {
                FilterChip(
                    selected = selectedType == ActivityType.SLEEP,
                    onClick = { onTypeSelected(ActivityType.SLEEP) },
                    label = { Text(stringResource(R.string.history_filter_sleep)) }
                )
            }
            
            item {
                FilterChip(
                    selected = selectedType == ActivityType.FEEDING,
                    onClick = { onTypeSelected(ActivityType.FEEDING) },
                    label = { Text(stringResource(R.string.history_filter_feeding)) }
                )
            }
            
            item {
                FilterChip(
                    selected = selectedType == ActivityType.DIAPER,
                    onClick = { onTypeSelected(ActivityType.DIAPER) },
                    label = { Text(stringResource(R.string.history_filter_diaper)) }
                )
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
