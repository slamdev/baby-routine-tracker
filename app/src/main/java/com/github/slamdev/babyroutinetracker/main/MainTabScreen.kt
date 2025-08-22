package com.github.slamdev.babyroutinetracker.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.auth.AuthenticationViewModel
import com.github.slamdev.babyroutinetracker.dashboard.DashboardContent
import com.github.slamdev.babyroutinetracker.history.ActivityHistoryContent
import com.github.slamdev.babyroutinetracker.datavisualization.DataVisualizationScreen
import com.github.slamdev.babyroutinetracker.sleepplans.AISleepPlansScreen
import com.github.slamdev.babyroutinetracker.invitation.InvitationViewModel
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.ui.components.ProfileIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    onSignOut: () -> Unit,
    onNavigateToInvitePartner: () -> Unit,
    onNavigateToJoinInvitation: () -> Unit,
    onNavigateToCreateBaby: () -> Unit,
    onNavigateToEditBaby: (Baby) -> Unit,
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
    
    // symbol + accessible label
    val pageSymbols: List<Pair<String, String>> = listOf(
        "🏠" to "Dashboard",
        "📜" to "History",
        "📊" to "Charts",
        "🤖" to "AI Plans"
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Responsive title layout optimized for landscape and portrait
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val isLandscape = maxWidth > maxHeight
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = if (isLandscape) {
                                Arrangement.spacedBy(16.dp)
                            } else {
                                Arrangement.spacedBy(8.dp)
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(end = if (isLandscape) 16.dp else 8.dp)
                            ) {
                                Text(
                                    text = selectedBaby?.name ?: "Baby Routine Tracker",
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                            
                            // Navigation chips with responsive spacing
                            Row(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(
                                    if (isLandscape) 12.dp else 8.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                pageSymbols.forEachIndexed { index, (symbol, desc) ->
                                    val selected = pagerState.currentPage == index
                                    NavIconChip(
                                        symbol = symbol,
                                        contentDescription = desc,
                                        selected = selected,
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                    )
                                }
                            }
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
                        onNavigateToEditBaby = onNavigateToEditBaby
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
            
            // Bottom indicators removed; navigation moved to top bar
        }
    }
}

@Composable
private fun NavIconChip(
    symbol: String,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(symbol) },
        modifier = modifier.semantics { this.contentDescription = contentDescription }
    )
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
