package com.github.slamdev.babyroutinetracker.feeding

import androidx.compose.foundation.clickable
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
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.ui.components.*
import com.github.slamdev.babyroutinetracker.ui.components.formatters.TimeUtils
import com.github.slamdev.babyroutinetracker.ui.components.helpers.bottleFeedingActivityConfig
import com.github.slamdev.babyroutinetracker.ui.components.helpers.bottleFeedingActivityContent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import java.util.*

@Composable
fun BottleFeedingCard(
    babyId: String,
    baby: Baby? = null,
    modifier: Modifier = Modifier,
    viewModel: FeedingTrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottleDialog by remember { mutableStateOf(false) }
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
                lastFeedingText = stringResource(R.string.activity_last_fed, "${amount}ml"),
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
        onPrimaryAction = { showBottleDialog = true },
        onContentClick = {
            // Only show edit dialog for bottle feedings
            uiState.lastFeeding?.takeIf {
                it.feedingType == "bottle"
            }?.let { showEditLastActivityDialog = true }
        },
        onDismissError = { viewModel.clearError() },
        onDismissSuccess = { viewModel.clearSuccessMessage() }
    )

    if (showBottleDialog) {
        BottleFeedingDialog(
            defaultAmount = baby?.defaultBottleAmount,
            onDismiss = { showBottleDialog = false },
            onConfirm = { amount, notes ->
                viewModel.logBottleFeeding(amount, notes)
                showBottleDialog = false
            }
        )
    }

    val lastFeedingToEdit = uiState.lastFeeding
    if (showEditLastActivityDialog && lastFeedingToEdit != null) {
        EditActivityDialog(
            activity = lastFeedingToEdit,
            onDismiss = { showEditLastActivityDialog = false },
            onSave = { activity, newStartTime, newEndTime, newNotes ->
                viewModel.updateCompletedFeeding(activity, newStartTime, newEndTime, newNotes)
                showEditLastActivityDialog = false
            }
        )
    }
}

@Composable
private fun BottleFeedingDialog(
    defaultAmount: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, notes: String) -> Unit
) {
    var amount by remember { 
        mutableStateOf(
            defaultAmount?.let { 
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() 
            } ?: ""
        ) 
    }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_bottle_feeding_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.dialog_bottle_feeding_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.dialog_bottle_feeding_notes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, notes) },
                enabled = amount.toDoubleOrNull() != null
            ) {
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
