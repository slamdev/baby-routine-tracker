package com.github.slamdev.babyroutinetracker.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
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
import com.github.slamdev.babyroutinetracker.sleep.SleepTrackingCard
import com.github.slamdev.babyroutinetracker.feeding.FeedingTrackingCard
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit,
    onNavigateToInvitePartner: () -> Unit,
    onNavigateToJoinInvitation: () -> Unit,
    onNavigateToCreateBaby: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthenticationViewModel = viewModel(),
    invitationViewModel: InvitationViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val invitationState by invitationViewModel.uiState.collectAsState()

    // Navigate back to sign-in if user signs out
    LaunchedEffect(uiState.isSignedIn) {
        if (!uiState.isSignedIn) {
            onSignOut()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Baby Routine Tracker",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { authViewModel.signOut() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome message
            WelcomeCard(user = uiState.user)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Activity tracking section - only show when there's a selected baby
            if (invitationState.selectedBabyId.isNotEmpty()) {
                val selectedBaby = invitationState.babies.find { it.id == invitationState.selectedBabyId }
                selectedBaby?.let { baby ->
                    Text(
                        text = "${baby.name}'s Activities",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Activity tracking cards
                    SleepTrackingCard(
                        babyId = baby.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    FeedingTrackingCard(
                        babyId = baby.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // No baby selected - show guidance
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
            }
            
            // Baby Profile Management Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Baby Profile Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToCreateBaby,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Create Baby Profile",
                                maxLines = 1,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        OutlinedButton(
                            onClick = onNavigateToJoinInvitation,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Join Profile",
                                maxLines = 1,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Only show invite partner button if there are baby profiles
                    if (invitationState.babies.isNotEmpty()) {
                        Button(
                            onClick = onNavigateToInvitePartner,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Invite Partner")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Feature status cards - show coming soon for other features
            if (invitationState.selectedBabyId.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Diapers",
                        description = "Coming soon",
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = "Data Visualization",
                        description = "Coming soon",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    user: FirebaseUser?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User profile picture or avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (user?.photoUrl != null) {
                    // Load Google profile picture
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        fallback = null,
                        error = null
                    )
                } else {
                    // Fallback avatar with user's initial
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.displayName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Welcome message
            Text(
                text = "Welcome ${user?.displayName ?: "User"}!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            
            // Email address
            if (user?.email != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email!!,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
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