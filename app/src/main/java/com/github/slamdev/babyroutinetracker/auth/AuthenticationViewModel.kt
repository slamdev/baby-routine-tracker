package com.github.slamdev.babyroutinetracker.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val errorMessage: String? = null
)

class AuthenticationViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val TAG = "AuthenticationViewModel"
    }

    init {
        // Check if user is already signed in
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        _uiState.value = _uiState.value.copy(
            isSignedIn = currentUser != null,
            user = currentUser
        )
    }

    fun signInWithGoogle(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                
                _uiState.value = _uiState.value.copy(
                    isSignedIn = true,
                    isLoading = false,
                    user = user,
                    errorMessage = null
                )
                Log.i(TAG, "Google Sign-In successful for user: ${user?.email}")
            } catch (e: ApiException) {
                Log.w(TAG, "Google Sign-In failed with API exception", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-In failed: ${e.message}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Authentication failed with unexpected error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Authentication failed: ${e.message}"
                )
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = _uiState.value.copy(
            isSignedIn = false,
            user = null,
            errorMessage = null
        )
        Log.i(TAG, "User signed out successfully")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}