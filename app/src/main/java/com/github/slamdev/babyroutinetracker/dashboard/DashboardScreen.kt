package com.github.slamdev.babyroutinetracker.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.sleep.SleepTrackingCard
import com.github.slamdev.babyroutinetracker.feeding.FeedingTrackingCard
import com.github.slamdev.babyroutinetracker.diaper.DiaperTrackingCard
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit,
    onNavigateToInvitePartner: () -> Unit,
    onNavigateToJoinInvitation: () -> Unit,
    onNavigateToCreateBaby: () -> Unit,
    onNavigateToHistory: (String) -> Unit,
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
                    // Show baby's name if one is selected, otherwise show app name
                    val titleText = if (invitationState.selectedBabyId.isNotEmpty()) {
                        invitationState.babies.find { it.id == invitationState.selectedBabyId }?.name ?: "Baby Routine Tracker"
                    } else {
                        "Baby Routine Tracker"
                    }
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Profile icon
                    ProfileIcon(
                        user = uiState.user,
                        onSignOut = { authViewModel.signOut() },
                        babies = invitationState.babies,
                        onNavigateToCreateBaby = onNavigateToCreateBaby,
                        onNavigateToJoinInvitation = onNavigateToJoinInvitation,
                        onNavigateToInvitePartner = onNavigateToInvitePartner
                    )
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
            // Activity tracking section - only show when there's a selected baby
            if (invitationState.selectedBabyId.isNotEmpty()) {
                val selectedBaby = invitationState.babies.find { it.id == invitationState.selectedBabyId }
                selectedBaby?.let { baby ->
                    // Activity tracking cards
                    SleepTrackingCard(
                        babyId = baby.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    FeedingTrackingCard(
                        babyId = baby.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DiaperTrackingCard(
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
                
                Text(
                    text = "Use the profile menu in the top-right corner to access baby profile options.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Feature cards and history navigation
            if (invitationState.selectedBabyId.isNotEmpty()) {
                // History button
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
private fun ProfileIcon(
    user: FirebaseUser?,
    onSignOut: () -> Unit,
    babies: List<Baby>,
    onNavigateToCreateBaby: () -> Unit,
    onNavigateToJoinInvitation: () -> Unit,
    onNavigateToInvitePartner: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    Box {
        // Profile icon/avatar
        Box(
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { showDropdownMenu = true },
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
                // Fallback avatar with user's initial or person icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.displayName?.isNotEmpty() == true) {
                        Text(
                            text = user.displayName!!.firstOrNull()?.toString()?.uppercase() ?: "U",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false }
        ) {
            // User info
            user?.let {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it.displayName ?: "User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (it.email != null) {
                        Text(
                            text = it.email!!,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                HorizontalDivider()
            }
            
            // Baby Profile Management Options
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Baby Profile"
                        )
                        Text("Create Baby Profile")
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onNavigateToCreateBaby()
                }
            )
            
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Join Profile"
                        )
                        Text("Join Profile")
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onNavigateToJoinInvitation()
                }
            )
            
            // Only show invite partner option if there are baby profiles
            if (babies.isNotEmpty()) {
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Invite Partner"
                            )
                            Text("Invite Partner")
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        onNavigateToInvitePartner()
                    }
                )
            }
            
            HorizontalDivider()
            
            // Sign out option
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                        Text("Sign Out")
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onSignOut()
                }
            )
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