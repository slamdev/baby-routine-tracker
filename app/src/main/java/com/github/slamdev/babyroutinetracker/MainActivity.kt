package com.github.slamdev.babyroutinetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.slamdev.babyroutinetracker.auth.AuthViewModel
import com.github.slamdev.babyroutinetracker.ui.screens.DashboardScreen
import com.github.slamdev.babyroutinetracker.ui.screens.SignInScreen
import com.github.slamdev.babyroutinetracker.ui.theme.BabyRoutineTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BabyRoutineTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BabyRoutineTrackerApp()
                }
            }
        }
    }
}

@Composable
fun BabyRoutineTrackerApp() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(context)
    )
    
    val authState by authViewModel.authState.collectAsState()
    
    if (authState.user != null) {
        DashboardScreen(authViewModel = authViewModel)
    } else {
        SignInScreen(authViewModel = authViewModel)
    }
}