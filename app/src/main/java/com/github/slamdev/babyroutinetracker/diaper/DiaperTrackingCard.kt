package com.github.slamdev.babyroutinetracker.diaper

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
import com.github.slamdev.babyroutinetracker.ui.components.diaperActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.diaperActivityContent
import com.github.slamdev.babyroutinetracker.ui.components.EditActivityDialog
import com.github.slamdev.babyroutinetracker.ui.components.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DiaperTrackingCard(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: DiaperTrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPoopDialog by remember { mutableStateOf(false) }
    var showEditLastActivityDialog by remember { mutableStateOf(false) }
    
    // Initialize the ViewModel for this baby
    LaunchedEffect(babyId) {
        viewModel.initialize(babyId)
    }
    
    // Activity card state
    val cardState = ActivityCardState(
        isLoading = uiState.isLoading,
        isOngoing = false, // Diaper is immediate activity
        errorMessage = uiState.errorMessage,
        successMessage = uiState.successMessage,
        isLoadingContent = uiState.isLoadingLastDiaper,
        contentError = uiState.lastDiaperError
    )
    
    // Prepare content based on current state
    val lastDiaper = uiState.lastDiaper
    
    val cardContent = when {
        lastDiaper != null -> {
            val timeAgo = TimeUtils.formatTimeAgo(
                TimeUtils.getRelevantTimestamp(
                    lastDiaper.startTime.toDate(),
                    lastDiaper.endTime?.toDate()
                )
            )
            
            diaperActivityContent(
                lastDiaper = lastDiaper,
                lastDiaperText = "Last poop logged",
                timeAgo = timeAgo,
                diaperTime = lastDiaper.startTime.toDate(),
                notes = lastDiaper.notes
            )
        }
        else -> {
            diaperActivityContent() // Empty content
        }
    }
    
    ActivityCard(
        config = diaperActivityConfig(),
        state = cardState,
        content = cardContent,
        modifier = modifier,
        onPrimaryAction = { showPoopDialog = true },
        onContentClick = { 
            uiState.lastDiaper?.let { showEditLastActivityDialog = true }
        },
        onDismissError = { 
            uiState.lastDiaperError?.let { viewModel.clearLastDiaperError() }
            uiState.errorMessage?.let { viewModel.clearError() }
        },
        onDismissSuccess = {
            uiState.successMessage?.let { viewModel.clearSuccess() }
        }
    )

    // Poop logging dialog
    if (showPoopDialog) {
        PoopLoggingDialog(
            onDismiss = { showPoopDialog = false },
            onConfirm = { notes ->
                viewModel.logPoop(notes)
                showPoopDialog = false
            }
        )
    }
    
    // Edit dialog for last completed diaper activity
    if (showEditLastActivityDialog) {
        uiState.lastDiaper?.let { lastDiaper ->
            EditActivityDialog(
                activity = lastDiaper,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoopLoggingDialog(
    onDismiss: () -> Unit,
    onConfirm: (notes: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "💩 Log Poop",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add any notes about this diaper change (optional):",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    placeholder = { Text("e.g., consistency, color, timing...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(notes)
                }
            ) {
                Text("Log Poop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
