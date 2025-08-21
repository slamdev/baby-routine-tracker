package com.github.slamdev.babyroutinetracker.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.auth.AuthenticationViewModel
import com.github.slamdev.babyroutinetracker.dashboard.DashboardContent
import com.github.slamdev.babyroutinetracker.history.ActivityHistoryContent
import com.github.slamdev.babyroutinetracker.datavisualization.DataVisualizationScreen
import com.github.slamdev.babyroutinetracker.sleepplans.AISleepPlansScreen
import com.github.slamdev.babyroutinetracker.invitation.InvitationViewModel
import com.github.slamdev.babyroutinetracker.ui.components.ProfileIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    onSignOut: () -> Unit,
    onNavigateToInvitePartner: () -> Unit,
    onNavigateToJoinInvitation: () -> Unit,
    onNavigateToCreateBaby: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthenticationViewModel = viewModel(),
    invitationViewModel: InvitationViewModel = viewModel()
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 4 }
    )
    
    val invitationState by invitationViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    
    // Get the selected baby from the invitation state
    val selectedBaby = invitationState.babies.find { it.id == invitationState.selectedBabyId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = selectedBaby?.name ?: "Baby Routine Tracker"
                    )
                },
                actions = {
                    ProfileIcon(
                        user = authState.user,
                        onSignOut = onSignOut,
                        babies = invitationState.babies,
                        onNavigateToCreateBaby = onNavigateToCreateBaby,
                        onNavigateToJoinInvitation = onNavigateToJoinInvitation,
                        onNavigateToInvitePartner = onNavigateToInvitePartner
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content with horizontal pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> DashboardContent(
                        invitationState = invitationState,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> {
                        if (invitationState.selectedBabyId.isNotEmpty()) {
                            ActivityHistoryContent(
                                babyId = invitationState.selectedBabyId,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptyStateScreen(
                                title = "Activity History",
                                description = "Create or join a baby profile to view activity history."
                            )
                        }
                    }
                    2 -> {
                        if (invitationState.selectedBabyId.isNotEmpty()) {
                            DataVisualizationScreen(
                                babyId = invitationState.selectedBabyId,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptyStateScreen(
                                title = "Data Visualization",
                                description = "Create or join a baby profile to view data visualizations."
                            )
                        }
                    }
                    3 -> {
                        if (invitationState.selectedBabyId.isNotEmpty()) {
                            AISleepPlansScreen(
                                babyId = invitationState.selectedBabyId,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptyStateScreen(
                                title = "AI Sleep Plans",
                                description = "Create or join a baby profile to access AI sleep plans."
                            )
                        }
                    }
                }
            }
            
            // Page indicators with labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pageLabels = listOf("Dashboard", "History", "Charts", "AI Plans")
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 12.dp else 8.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxSize()
                            ) {}
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = pageLabels[index],
                            fontSize = 10.sp,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
