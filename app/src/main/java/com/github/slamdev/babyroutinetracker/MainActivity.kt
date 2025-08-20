package com.github.slamdev.babyroutinetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.slamdev.babyroutinetracker.auth.AuthenticationViewModel
import com.github.slamdev.babyroutinetracker.auth.SignInScreen
import com.github.slamdev.babyroutinetracker.dashboard.DashboardScreen
import com.github.slamdev.babyroutinetracker.ui.theme.BabyroutinetrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            DashboardScreen(
                onSignOut = {
                    navController.navigate("signin") {
                        // Clear the back stack so user can't navigate back to dashboard
                        popUpTo("dashboard") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                authViewModel = authViewModel
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