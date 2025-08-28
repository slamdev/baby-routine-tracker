package com.github.slamdev.babyroutinetracker.account

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.service.DataCleanupService
import com.github.slamdev.babyroutinetracker.util.LocalizedMessageProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountDeletionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAccountDeleted: Boolean = false
)

class AccountDeletionViewModel(application: Application) : AndroidViewModel(application) {
    private val dataCleanupService = DataCleanupService()
    private val messageProvider = LocalizedMessageProvider(application)

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
                                messageProvider.getAccountDeletionUserNotAuthenticatedErrorMessage()
                            exception.message?.contains("network", ignoreCase = true) == true ->
                                messageProvider.getAccountDeletionNetworkErrorMessage()
                            exception.message?.contains("permission", ignoreCase = true) == true ->
                                messageProvider.getAccountDeletionPermissionErrorMessage()
                            else ->
                                messageProvider.getAccountDeletionGenericErrorMessage()
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
                    errorMessage = messageProvider.getAccountDeletionUnexpectedErrorMessage()
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
