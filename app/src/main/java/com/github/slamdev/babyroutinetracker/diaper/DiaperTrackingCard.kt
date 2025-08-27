package com.github.slamdev.babyroutinetracker.diaper

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.ui.components.*
import com.github.slamdev.babyroutinetracker.ui.components.helpers.diaperActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.helpers.diaperActivityContent
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
                lastDiaperText = stringResource(R.string.activity_last_poop),
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
        onDismissError = { viewModel.clearError() },
        onDismissSuccess = { viewModel.clearSuccess() }
    )

    if (showPoopDialog) {
        PoopDialog(
            onDismiss = { showPoopDialog = false },
            onConfirm = { notes ->
                viewModel.logPoop(notes)
                showPoopDialog = false
            }
        )
    }

    val lastDiaperToEdit = uiState.lastDiaper
    if (showEditLastActivityDialog && lastDiaperToEdit != null) {
        EditActivityDialog(
            activity = lastDiaperToEdit,
            onDismiss = { showEditLastActivityDialog = false },
            onSave = { activity, newStartTime, newEndTime, newNotes ->
                viewModel.updateCompletedDiaper(
                    activity.copy(
                        startTime = com.google.firebase.Timestamp(newStartTime),
                        endTime = newEndTime?.let { com.google.firebase.Timestamp(it) },
                        notes = newNotes ?: ""
                    )
                )
                showEditLastActivityDialog = false
            }
        )
    }
}

@Composable
private fun PoopDialog(
    onDismiss: () -> Unit,
    onConfirm: (notes: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_poop_title)) },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.dialog_poop_notes)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(notes) }) {
                Text(stringResource(R.string.log))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
