package com.github.slamdev.babyroutinetracker.notifications

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.NotificationPreferences
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.github.slamdev.babyroutinetracker.service.NotificationPreferencesService
import com.github.slamdev.babyroutinetracker.service.PartnerNotificationService
import com.github.slamdev.babyroutinetracker.util.LocalizedMessageProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val preferences: OptionalUiState<NotificationPreferences> = OptionalUiState.Loading,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    val testNotificationStatus: String? = null
)

class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationPreferencesService = NotificationPreferencesService(application)
    private val partnerNotificationService = PartnerNotificationService(application)
    private val messageProvider = LocalizedMessageProvider(application)

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "NotificationSettingsViewModel"
    }

    /**
     * Initialize notification preferences for a baby
     */
    fun initializePreferences(babyId: String) {
        viewModelScope.launch {
            notificationPreferencesService.getNotificationPreferencesFlow(babyId)
                .collect { preferencesState ->
                    _uiState.value = _uiState.value.copy(
                        preferences = preferencesState
                    )
                }
        }
    }

    /**
     * Update notification preferences
     */
    fun updatePreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                saveError = null,
                saveSuccess = false
            )

            try {
                val result = notificationPreferencesService.updateNotificationPreferences(preferences)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                    Log.d(TAG, "Notification preferences updated successfully")
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveError = messageProvider.getSavePreferencesFailedErrorMessage(error?.message ?: "")
                    )
                    Log.e(TAG, "Failed to update notification preferences", error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveError = messageProvider.getSavePreferencesFailedErrorMessage(e.message ?: "")
                )
                Log.e(TAG, "Failed to update notification preferences", e)
            }
        }
    }

    /**
     * Send test notification to partners
     */
    fun sendTestNotification(babyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                testNotificationStatus = messageProvider.getSendingTestNotificationStatusMessage()
            )

            try {
                val result = partnerNotificationService.sendTestNotification(babyId)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        testNotificationStatus = result.getOrNull()
                    )
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        testNotificationStatus = messageProvider.getTestNotificationFailedErrorMessage(error?.message ?: "")
                    )
                    Log.e(TAG, "Failed to send test notification", error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    testNotificationStatus = messageProvider.getTestNotificationFailedErrorMessage(e.message ?: "")
                )
                Log.e(TAG, "Failed to send test notification", e)
            }
        }
    }

    /**
     * Clear error messages
     */
    fun clearSaveError() {
        _uiState.value = _uiState.value.copy(saveError = null)
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearTestStatus() {
        _uiState.value = _uiState.value.copy(testNotificationStatus = null)
    }
}
