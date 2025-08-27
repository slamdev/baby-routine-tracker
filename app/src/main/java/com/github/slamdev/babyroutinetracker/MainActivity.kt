package com.github.slamdev.babyroutinetracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.github.slamdev.babyroutinetracker.account.AccountDeletionScreen
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
import com.github.slamdev.babyroutinetracker.notifications.NotificationSettingsScreen
import com.github.slamdev.babyroutinetracker.preferences.getLanguagePreferences
import com.github.slamdev.babyroutinetracker.service.UserService
import com.github.slamdev.babyroutinetracker.settings.LanguageSettingsScreen
import com.github.slamdev.babyroutinetracker.ui.theme.BabyroutinetrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun attachBaseContext(newBase: Context) {
        val languagePreferences = newBase.getLanguagePreferences()
        val locale = languagePreferences.getLocale()
        
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        
        val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newBase.createConfigurationContext(config)
        } else {
            newBase.resources.updateConfiguration(config, newBase.resources.displayMetrics)
            newBase
        }
        
        super.attachBaseContext(context)
    }
    
    // Notification permission launcher for Android 13+
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "Notification permission granted: $isGranted")
        if (!isGranted) {
            Log.w(TAG, "Notification permission denied by user")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize language preferences
        getLanguagePreferences().initializeLanguage()
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        // Initialize FCM token
        initializeFcmToken()
        
        enableEdgeToEdge()
        setContent {
            BabyroutinetrackerTheme {
                BabyRoutineTrackerApp()
            }
        }
    }
    
    /**
     * Request notification permission for Android 13+
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                }
                else -> {
                    Log.d(TAG, "Requesting notification permission")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "Android version < 13, notification permission not required")
        }
    }

    /**
     * Initialize Firebase Cloud Messaging token
     */
    private fun initializeFcmToken() {
        lifecycleScope.launch {
            try {
                val userService = UserService()
                val result = userService.initializeFcmToken()
                if (result.isSuccess) {
                    Log.d(TAG, "FCM token initialized successfully")
                } else {
                    Log.w(TAG, "Failed to initialize FCM token", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing FCM token", e)
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
                onNavigateToNotificationSettings = { baby ->
                    navController.navigate("notification_settings/${baby.id}/${baby.name}")
                },
                onNavigateToAccountDeletion = {
                    navController.navigate("account_deletion")
                },
                onNavigateToLanguageSettings = {
                    navController.navigate("language_settings")
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
        
        composable(
            "notification_settings/{babyId}/{babyName}",
            arguments = listOf(
                navArgument("babyId") { type = NavType.StringType },
                navArgument("babyName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val babyId = backStackEntry.arguments?.getString("babyId") ?: ""
            val babyName = backStackEntry.arguments?.getString("babyName") ?: ""
            NotificationSettingsScreen(
                babyId = babyId,
                babyName = babyName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("account_deletion") {
            AccountDeletionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAccountDeleted = {
                    // Navigate to sign-in screen and clear all back stack
                    navController.navigate("signin") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable("language_settings") {
            LanguageSettingsScreen(
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