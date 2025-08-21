package com.github.slamdev.babyroutinetracker.feeding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "🍼 Bottle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // Action button
            Button(
                onClick = { showBottleFeedingDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Bottle",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Last bottle feeding info (only show bottle feedings)
            val lastFeeding = uiState.lastFeeding?.takeIf { it.feedingType == "bottle" }
            when {
                uiState.isLoadingLastFeeding -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
                lastFeeding != null && lastFeeding.endTime != null -> {
                    val amount = lastFeeding.amount.toInt()
                    
                    // Calculate time ago
                    val timeAgo = TimeUtils.formatTimeAgo(
                        TimeUtils.getRelevantTimestamp(
                            lastFeeding.startTime.toDate(),
                            lastFeeding.endTime?.toDate()
                        )
                    )
                    
                    // Last feeding info (clickable for editing)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showEditLastActivityDialog = true }
                            .padding(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last fed: ${amount}ml",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit last feeding",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        
                        Text(
                            text = timeAgo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        
                        lastFeeding.endTime?.let { endTime ->
                            Text(
                                text = "at ${formatTime(endTime.toDate())}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Show notes if available
                        if (lastFeeding.notes.isNotBlank()) {
                            Text(
                                text = "\"${lastFeeding.notes}\"",
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
                        text = "No recent feeding",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
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
