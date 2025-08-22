package com.github.slamdev.babyroutinetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.ui.theme.extended

/**
 * Component to display baby's age information including real age and corrected age
 */
@Composable
fun BabyAgeDisplay(
    baby: Baby,
    modifier: Modifier = Modifier,
    showDetails: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.extended.successContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Primary age display (real age)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = baby.getFormattedRealAge(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (baby.dueDate != null && showDetails) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Age info",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Corrected age if available
            baby.getFormattedAdjustedAge()?.let { adjustedAge ->
                if (showDetails) {
                    Text(
                        text = "Corrected: $adjustedAge",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Gestation info if born early
            if (baby.wasBornEarly() && showDetails) {
                baby.getGestationWeeks()?.let { weeks ->
                    Text(
                        text = "Born at ~$weeks weeks gestation",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Compact age display for dashboard or limited space contexts
 */
@Composable
fun CompactBabyAgeDisplay(
    baby: Baby,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = baby.getFormattedRealAge(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        baby.getFormattedAdjustedAge()?.let { adjustedAge ->
            Text(
                text = "Corrected: $adjustedAge",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Age display suitable for the dashboard title area
 */
@Composable
fun DashboardAgeDisplay(
    baby: Baby,
    modifier: Modifier = Modifier
) {
    val age = baby.getFormattedRealAge()
    val correctedAge = baby.getFormattedAdjustedAge()
    
    Text(
        text = if (correctedAge != null && baby.wasBornEarly()) {
            "$age (corrected: $correctedAge)"
        } else {
            age
        },
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        modifier = modifier
    )
}
