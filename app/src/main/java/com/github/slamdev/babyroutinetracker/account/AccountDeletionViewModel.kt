package com.github.slamdev.babyroutinetracker.account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.service.DataCleanupService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountDeletionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAccountDeleted: Boolean = false
)

class AccountDeletionViewModel : ViewModel() {
    private val dataCleanupService = DataCleanupService()

    private val _uiState = MutableStateFlow(AccountDeletionUiState())
    val uiState: StateFlow<AccountDeletionUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "AccountDeletionViewModel"
    }

    /**
     * Initiate account deletion process
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                Log.i(TAG, "Starting account deletion process")
                
                val result = dataCleanupService.deleteUserAccountAndData()
                
                result.fold(
                    onSuccess = {
                        Log.i(TAG, "Account deletion completed successfully")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAccountDeleted = true,
                            errorMessage = null
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Account deletion failed", exception)
                        val userFriendlyMessage = when {
                            exception.message?.contains("User not authenticated") == true ->
                                "Please sign in again to delete your account"
                            exception.message?.contains("network", ignoreCase = true) == true ->
                                "Unable to connect to server. Please check your internet connection and try again"
                            exception.message?.contains("permission", ignoreCase = true) == true ->
                                "You don't have permission to perform this action. Please try signing in again"
                            else ->
                                "Failed to delete account. Please try again or contact support if the problem persists"
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = userFriendlyMessage
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during account deletion", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred. Please try again"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
