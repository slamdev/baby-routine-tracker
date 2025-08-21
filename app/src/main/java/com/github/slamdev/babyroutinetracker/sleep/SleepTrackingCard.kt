package com.github.slamdev.babyroutinetracker.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SleepTrackingCard(
    babyId: String,
    modifier: Modifier = Modifier,
    viewModel: SleepTrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize the ViewModel for this baby
    LaunchedEffect(babyId) {
        viewModel.initialize(babyId)
    }
    
    // Show error message if any
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            // Error will be cleared automatically after being shown
            // In a production app, you might want to show a snackbar here
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.ongoingSleep != null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Sleep",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sleep",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Current status and timer
            val ongoingSleep = uiState.ongoingSleep
            if (ongoingSleep != null) {
                // Ongoing sleep status
                Text(
                    text = "Sleep in progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Timer display
                Text(
                    text = viewModel.formatElapsedTime(uiState.currentElapsedTime),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                // Start time
                Text(
                    text = "Started at ${formatTime(ongoingSleep.startTime.toDate())}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
            } else {
                // No ongoing sleep
                Text(
                    text = "Not sleeping",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                // Show last sleep if available
                uiState.lastSleep?.let { lastSleep ->
                    if (lastSleep.endTime != null) {
                        val duration = lastSleep.getDurationMinutes()
                        val durationText = if (duration != null) {
                            val hours = duration / 60
                            val minutes = duration % 60
                            if (hours > 0) {
                                "${hours}h ${minutes}m"
                            } else {
                                "${minutes}m"
                            }
                        } else {
                            "Unknown duration"
                        }
                        
                        Text(
                            text = "Last sleep: $durationText",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        
                        Text(
                            text = "Ended at ${formatTime(lastSleep.endTime.toDate())}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Action button
            val isOngoingSleep = uiState.ongoingSleep != null
            Button(
                onClick = {
                    if (isOngoingSleep) {
                        viewModel.endSleep()
                    } else {
                        viewModel.startSleep()
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOngoingSleep) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOngoingSleep) "Stopping..." else "Starting...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    if (isOngoingSleep) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Stop Sleep",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stop Sleep",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Sleep",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Sleep",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Error message
            uiState.errorMessage?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Clear error after showing it
                LaunchedEffect(errorMessage) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearError()
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
