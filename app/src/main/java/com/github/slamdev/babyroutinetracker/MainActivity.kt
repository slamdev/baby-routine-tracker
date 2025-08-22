package com.github.slamdev.babyroutinetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.github.slamdev.babyroutinetracker.auth.AuthenticationViewModel
import com.github.slamdev.babyroutinetracker.auth.SignInScreen
import com.github.slamdev.babyroutinetracker.dashboard.DashboardScreen
import com.github.slamdev.babyroutinetracker.main.MainTabScreen
import com.github.slamdev.babyroutinetracker.invitation.InvitePartnerScreen
import com.github.slamdev.babyroutinetracker.invitation.JoinInvitationScreen
import com.github.slamdev.babyroutinetracker.invitation.CreateBabyProfileScreen
import com.github.slamdev.babyroutinetracker.invitation.EditBabyProfileScreen
import com.github.slamdev.babyroutinetracker.invitation.InvitationViewModel
import com.github.slamdev.babyroutinetracker.history.ActivityHistoryScreen
import com.github.slamdev.babyroutinetracker.offline.OfflineManager
import com.github.slamdev.babyroutinetracker.ui.theme.BabyroutinetrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize offline manager
        OfflineManager.getInstance(this).initialize()
        
        enableEdgeToEdge()
        setContent {
            BabyroutinetrackerTheme {
                BabyRoutineTrackerApp()
            }
        }
    }
}

@Composable
fun BabyRoutineTrackerApp() {
    val navController = rememberNavController()
    val authViewModel: AuthenticationViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsState()

    // Determine starting destination based on authentication state
    val startDestination = if (uiState.isSignedIn) "dashboard" else "signin"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable("signin") {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate("dashboard") {
                        // Clear the back stack so user can't navigate back to sign-in
                        popUpTo("signin") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                viewModel = authViewModel
            )
        }
        
        composable("dashboard") {
            MainTabScreen(
                onSignOut = {
                    navController.navigate("signin") {
                        // Clear the back stack so user can't navigate back to dashboard
                        popUpTo("dashboard") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToInvitePartner = {
                    navController.navigate("invite_partner")
                },
                onNavigateToJoinInvitation = {
                    navController.navigate("join_invitation")
                },
                onNavigateToCreateBaby = {
                    navController.navigate("create_baby")
                },
                onNavigateToEditBaby = { baby ->
                    navController.navigate("edit_baby/${baby.id}")
                },
                authViewModel = authViewModel
            )
        }
        
        composable("invite_partner") {
            InvitePartnerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("join_invitation") {
            JoinInvitationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onJoinSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("join_invitation") { inclusive = true }
                    }
                }
            )
        }
        
        composable("create_baby") {
            CreateBabyProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCreateSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("create_baby") { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            "edit_baby/{babyId}",
            arguments = listOf(navArgument("babyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val babyId = backStackEntry.arguments?.getString("babyId") ?: ""
            val invitationViewModel: InvitationViewModel = viewModel()
            
            EditBabyProfileScreen(
                babyId = babyId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUpdateSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("edit_baby/{babyId}") { inclusive = true }
                    }
                },
                viewModel = invitationViewModel
            )
        }
        
        composable(
            "history/{babyId}",
            arguments = listOf(navArgument("babyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val babyId = backStackEntry.arguments?.getString("babyId") ?: ""
            ActivityHistoryScreen(
                babyId = babyId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
    
    // Handle authentication state changes to navigate appropriately
    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn && navController.currentDestination?.route == "signin") {
            navController.navigate("dashboard") {
                popUpTo("signin") { inclusive = true }
                launchSingleTop = true
            }
        } else if (!uiState.isSignedIn && navController.currentDestination?.route == "dashboard") {
            navController.navigate("signin") {
                popUpTo("dashboard") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}