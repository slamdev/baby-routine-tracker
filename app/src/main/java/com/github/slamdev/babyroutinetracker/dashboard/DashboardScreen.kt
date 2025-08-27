package com.github.slamdev.babyroutinetracker.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.slamdev.babyroutinetracker.auth.AuthenticationViewModel
import com.github.slamdev.babyroutinetracker.invitation.InvitationViewModel
import com.github.slamdev.babyroutinetracker.invitation.InvitationUiState
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.sleep.SleepTrackingCard
import com.github.slamdev.babyroutinetracker.feeding.BreastFeedingCard
import com.github.slamdev.babyroutinetracker.feeding.BottleFeedingCard
import com.github.slamdev.babyroutinetracker.diaper.DiaperTrackingCard
import com.github.slamdev.babyroutinetracker.ui.components.ProfileIcon
import com.google.firebase.auth.FirebaseUser
import kotlin.math.ceil
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit,
    onNavigateToInvitePartner: () -> Unit,
    onNavigateToJoinInvitation: () -> Unit,
    onNavigateToCreateBaby: () -> Unit,
    onNavigateToEditBaby: (Baby) -> Unit,
    onNavigateToNotificationSettings: (Baby) -> Unit,
    onNavigateToAccountDeletion: () -> Unit,
    onNavigateToHistory: (String) -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthenticationViewModel = viewModel(),
    invitationViewModel: InvitationViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val invitationState by invitationViewModel.uiState.collectAsState()

    // Get the selected baby from the invitation state
    val selectedBaby = invitationState.babies.find { it.id == invitationState.selectedBabyId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = selectedBaby?.name ?: "Baby Routine Tracker"
                        )
                        selectedBaby?.let { baby ->
                            Text(
                                text = baby.getFormattedRealAge() + (
                                    baby.getFormattedAdjustedAge()?.let { corrected ->
                                        if (baby.wasBornEarly()) " (corrected: $corrected)" else ""
                                    } ?: ""
                                ),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    ProfileIcon(
                        user = authState.user,
                        onSignOut = onSignOut,
                        babies = invitationState.babies,
                        selectedBaby = selectedBaby,
                        onNavigateToCreateBaby = onNavigateToCreateBaby,
                        onNavigateToJoinInvitation = onNavigateToJoinInvitation,
                        onNavigateToInvitePartner = onNavigateToInvitePartner,
                        onNavigateToEditBaby = onNavigateToEditBaby,
                        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
                        onNavigateToAccountDeletion = onNavigateToAccountDeletion
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        DashboardContent(
            invitationState = invitationState,
            onNavigateToHistory = onNavigateToHistory,
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun DashboardContent(
    invitationState: InvitationUiState,
    onNavigateToHistory: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Activity tracking section - only show when there's a selected baby
    if (invitationState.selectedBabyId.isNotEmpty()) {
        val selectedBaby = invitationState.babies.find { it.id == invitationState.selectedBabyId }
        selectedBaby?.let { baby ->
            // Responsive layout that adapts number of columns & card height to available space
            BoxWithConstraints(modifier = modifier.fillMaxSize()) {
                val cardMinWidth = 180.dp
                val minCardHeight = 340.dp
                val outerPadding = 16.dp
                val spacing = 12.dp

                // Enhanced responsive logic for better landscape support
                val availableRowWidth = maxWidth - outerPadding * 2
                val isLandscape = maxWidth > maxHeight
                
                // Calculate optimal columns based on available width and orientation
                val rawColumns = floor(
                    ((availableRowWidth + spacing) / (cardMinWidth + spacing)).toDouble()
                ).toInt().coerceAtLeast(1)
                
                val itemCount = 4
                val columns = if (isLandscape) {
                    // In landscape, prefer 4 columns if space allows, otherwise 2
                    when {
                        rawColumns >= 4 -> 4
                        rawColumns >= 2 -> 2
                        else -> 1
                    }
                } else {
                    // In portrait, stick to 2 columns for optimal usability
                    rawColumns.coerceAtMost(2)
                }.coerceAtMost(itemCount) // Never exceed item count
                
                val rows = ceil(itemCount / columns.toDouble()).toInt()

                // Calculate card heights with better landscape optimization
                val verticalPaddingTotal = outerPadding * 2
                val totalVerticalSpacing = spacing * (rows - 1)
                val availableHeightForCards = maxHeight - verticalPaddingTotal - totalVerticalSpacing
                val computedCardHeight = (availableHeightForCards / rows)
                
                // In landscape mode, allow slightly smaller cards for better fit
                val effectiveMinHeight = if (isLandscape) (minCardHeight * 0.8f) else minCardHeight
                val fitsWithoutScroll = computedCardHeight >= effectiveMinHeight
                val cardHeight = if (fitsWithoutScroll) computedCardHeight else effectiveMinHeight

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(outerPadding),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    item {
                        SleepTrackingCard(
                            babyId = baby.id,
                            modifier = Modifier.height(cardHeight)
                        )
                    }
                    item {
                        DiaperTrackingCard(
                            babyId = baby.id,
                            modifier = Modifier.height(cardHeight)
                        )
                    }
                    item {
                        BottleFeedingCard(
                            babyId = baby.id,
                            baby = baby,
                            modifier = Modifier.height(cardHeight)
                        )
                    }
                    item {
                        BreastFeedingCard(
                            babyId = baby.id,
                            modifier = Modifier.height(cardHeight)
                        )
                    }
                }
            }
        }
    } else {
        // No baby selected - show guidance (keep existing implementation)
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Create or Join a Baby Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "To start tracking your baby's activities, create a new baby profile or join an existing one using an invitation code.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 22.sp
            )
            
            Text(
                text = "Use the profile menu in the top-right corner to access baby profile options.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // History navigation and feature cards - only show when we have a navigation callback (old UI)
            if (onNavigateToHistory != null) {
                // History button (only in old DashboardScreen)
                Button(
                    onClick = { onNavigateToHistory(invitationState.selectedBabyId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Activity History")
                }
                
                // Feature status cards - show coming soon for other features
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Data Visualization",
                        description = "Coming soon",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = "AI Sleep Plans",
                        description = "Coming soon",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}