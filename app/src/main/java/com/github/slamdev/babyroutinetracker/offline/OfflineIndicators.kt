package com.github.slamdev.babyroutinetracker.offline

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable that displays offline status indicator when network is unavailable
 */
@Composable
fun OfflineStatusIndicator(
    isOffline: Boolean,
    pendingOperationsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(20.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "You're offline",
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                val statusText = when {
                    pendingOperationsCount == 0 -> "Recent data available for viewing"
                    pendingOperationsCount == 1 -> "$pendingOperationsCount activity waiting to sync"
                    else -> "$pendingOperationsCount activities waiting to sync"
                }
                
                Text(
                    text = statusText,
                    color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Composable that displays sync progress indicator
 */
@Composable
fun SyncProgressIndicator(
    isVisible: Boolean,
    syncedCount: Int = 0,
    totalCount: Int = 0,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Syncing",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Syncing data...",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                if (totalCount > 0) {
                    Text(
                        text = "Synchronized $syncedCount of $totalCount activities",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            
            if (totalCount > 0) {
                LinearProgressIndicator(
                    progress = if (totalCount > 0) syncedCount.toFloat() / totalCount else 0f,
                    modifier = Modifier.width(60.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Compact offline indicator for smaller UI areas
 */
@Composable
fun CompactOfflineIndicator(
    isOffline: Boolean,
    pendingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = if (pendingCount > 0) "Offline ($pendingCount)" else "Offline",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Success indicator when sync completes
 */
@Composable
fun SyncSuccessIndicator(
    isVisible: Boolean,
    syncedCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var showIndicator by remember { mutableStateOf(false) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            showIndicator = true
            kotlinx.coroutines.delay(3000) // Show for 3 seconds
            showIndicator = false
        }
    }
    
    AnimatedVisibility(
        visible = showIndicator,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Sync complete",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = if (syncedCount > 0) {
                    "Synchronized $syncedCount ${if (syncedCount == 1) "activity" else "activities"}"
                } else {
                    "All data synchronized"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
