package com.github.slamdev.babyroutinetracker.feeding

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
import com.github.slamdev.babyroutinetracker.ui.components.bottleFeedingActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.bottleFeedingActivityContent
import com.github.slamdev.babyroutinetracker.ui.components.EditActivityDialog
import com.github.slamdev.babyroutinetracker.ui.components.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BottleFeedingCard(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: FeedingTrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottleFeedingDialog by remember { mutableStateOf(false) }
    var showEditLastActivityDialog by remember { mutableStateOf(false) }
    
    // Initialize the ViewModel for this baby
    LaunchedEffect(babyId) {
        viewModel.initialize(babyId)
    }
    
    // Activity card state
    val cardState = ActivityCardState(
        isLoading = uiState.isLoading,
        isOngoing = false, // Bottle feeding is immediate activity
        errorMessage = uiState.errorMessage,
        successMessage = uiState.successMessage,
        isLoadingContent = uiState.isLoadingLastFeeding,
        contentError = uiState.lastFeedingError
    )
    
    // Prepare content based on current state
    val lastFeeding = uiState.lastFeeding?.takeIf { it.feedingType == "bottle" }
    
    val cardContent = when {
        lastFeeding != null && lastFeeding.endTime != null -> {
            val amount = lastFeeding.amount.toInt()
            val timeAgo = TimeUtils.formatTimeAgo(
                TimeUtils.getRelevantTimestamp(
                    lastFeeding.startTime.toDate(),
                    lastFeeding.endTime?.toDate()
                )
            )
            
            bottleFeedingActivityContent(
                lastFeeding = lastFeeding,
                lastFeedingText = "Last fed ${amount}ml",
                timeAgo = timeAgo,
                feedingTime = lastFeeding.startTime.toDate()
            )
        }
        else -> {
            bottleFeedingActivityContent() // Empty content
        }
    }
    
    ActivityCard(
        config = bottleFeedingActivityConfig(),
        state = cardState,
        content = cardContent,
        modifier = modifier,
        onPrimaryAction = { showBottleFeedingDialog = true },
        onContentClick = { 
            // Only show edit dialog for bottle feedings
            uiState.lastFeeding?.takeIf { 
                it.feedingType == "bottle" 
            }?.let { showEditLastActivityDialog = true }
        },
        onDismissError = { 
            uiState.lastFeedingError?.let { viewModel.clearLastFeedingError() }
            uiState.errorMessage?.let { viewModel.clearError() }
        },
        onDismissSuccess = {
            uiState.successMessage?.let { viewModel.clearSuccessMessage() }
        }
    )
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
    
    // Edit dialog for last completed feeding activity
    if (showEditLastActivityDialog) {
        val lastFeeding = uiState.lastFeeding?.takeIf { it.feedingType == "bottle" }
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
                text = "🍼 Log Bottle Feeding",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Amount (ml)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        isValidInput = it.isNotBlank() && it.toDoubleOrNull() != null && it.toDoubleOrNull()!! > 0
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("e.g., 120") },
                    suffix = { Text("ml") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isValidInput && amount.isNotBlank()
                )
                
                // Notes (optional)
                Text(
                    text = "Notes (optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    onConfirm(amountDouble, notes)
                },
                enabled = amount.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
            ) {
                Text("Log Bottle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
