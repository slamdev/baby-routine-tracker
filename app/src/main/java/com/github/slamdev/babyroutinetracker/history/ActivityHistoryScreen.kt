package com.github.slamdev.babyroutinetracker.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                title = { Text("Activity History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        Text("Loading activities...")
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
                        text = "No activities found",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.activities) { activity ->
                        ActivityHistoryItem(
                            activity = activity,
                            onEditActivity = {
                                selectedActivity = activity
                                showEditDialog = true
                            }
                        )
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
                        ActivityType.DIAPER -> "💩 Diaper"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                // Activity-specific details
                when (activity.type) {
                    ActivityType.FEEDING -> {
                        if (activity.feedingType == "bottle" && activity.amount > 0) {
                            Text(
                                text = "${activity.amount.toInt()} ml",
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
                        text = "\"${activity.notes}\"",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
            // Edit button (only for completed activities)
            if (activity.endTime != null) {
                IconButton(onClick = onEditActivity) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit activity",
                        tint = MaterialTheme.colorScheme.primary
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
