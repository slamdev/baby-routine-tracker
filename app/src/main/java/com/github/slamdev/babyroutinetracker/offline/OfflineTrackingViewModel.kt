package com.github.slamdev.babyroutinetracker.offline

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Example ViewModel demonstrating offline-first activity tracking
 * This shows how to integrate OfflineActivityService in ViewModels
 */
data class OfflineTrackingUiState(
    val isLoading: Boolean = false,
    val ongoingActivity: Activity? = null,
    val lastActivity: Activity? = null,
    val currentElapsedTime: Long = 0L, // in seconds
    val errorMessage: String? = null,
    val ongoingActivityError: String? = null,
    val lastActivityError: String? = null,
    val isLoadingOngoingActivity: Boolean = false,
    val isLoadingLastActivity: Boolean = false,
    val successMessage: String? = null,
    val isOffline: Boolean = false,
    val pendingOperationsCount: Int = 0
)

class OfflineTrackingViewModel(
    private val context: Context,
    private val activityType: ActivityType
) : ViewModel() {
    
    private val offlineManager = OfflineManager.getInstance(context)
    private val offlineActivityService = offlineManager.getOfflineActivityService()
    
    private val _uiState = MutableStateFlow(OfflineTrackingUiState())
    val uiState: StateFlow<OfflineTrackingUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var currentBabyId: String? = null
    
    companion object {
        private const val TAG = "OfflineTrackingViewModel"
    }

    /**
     * Initialize the ViewModel for a specific baby
     */
    fun initialize(babyId: String) {
        if (currentBabyId == babyId) {
            // Already initialized for this baby
            return
        }
        
        currentBabyId = babyId
        
        // Monitor offline state
        viewModelScope.launch {
            offlineManager.offlineState.collect { offlineState ->
                _uiState.value = _uiState.value.copy(
                    isOffline = offlineState.isOffline,
                    pendingOperationsCount = offlineState.pendingOperationsCount
                )
            }
        }
        
        // Monitor ongoing activity with offline support
        viewModelScope.launch {
            offlineActivityService.getOngoingActivityFlow(babyId, activityType)
                .collect { activityState ->
                    when (activityState) {
                        is OptionalUiState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingActivity = true,
                                ongoingActivityError = null
                            )
                        }
                        is OptionalUiState.Success -> {
                            val activity = activityState.data
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingActivity = false,
                                ongoingActivity = activity,
                                ongoingActivityError = null
                            )
                            
                            // Start timer if activity is ongoing
                            if (activity.isOngoing()) {
                                startTimer(activity.startTime.toDate())
                            } else {
                                stopTimer()
                            }
                        }
                        is OptionalUiState.Empty -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingActivity = false,
                                ongoingActivity = null,
                                ongoingActivityError = null
                            )
                            stopTimer()
                        }
                        is OptionalUiState.Error -> {
                            Log.e(TAG, "Error getting ongoing activity", activityState.exception)
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingActivity = false,
                                ongoingActivity = null,
                                ongoingActivityError = activityState.message
                            )
                            stopTimer()
                        }
                    }
                }
        }
        
        // Monitor last completed activity with offline support
        viewModelScope.launch {
            offlineActivityService.getLastCompletedActivityFlow(babyId, activityType)
                .collect { activityState ->
                    when (activityState) {
                        is OptionalUiState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastActivity = true,
                                lastActivityError = null
                            )
                        }
                        is OptionalUiState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastActivity = false,
                                lastActivity = activityState.data,
                                lastActivityError = null
                            )
                        }
                        is OptionalUiState.Empty -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastActivity = false,
                                lastActivity = null,
                                lastActivityError = null
                            )
                        }
                        is OptionalUiState.Error -> {
                            Log.e(TAG, "Error getting last activity", activityState.exception)
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastActivity = false,
                                lastActivity = null,
                                lastActivityError = activityState.message
                            )
                        }
                    }
                }
        }
    }
    
    /**
     * Start new activity with offline support
     */
    fun startActivity(notes: String = "") {
        val babyId = currentBabyId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = offlineActivityService.startActivity(babyId, activityType, notes)
                
                if (result.isSuccess) {
                    val activity = result.getOrThrow()
                    Log.i(TAG, "Activity started successfully: ${activityType.displayName}")
                    
                    // Show success message that indicates offline mode
                    val successMsg = if (_uiState.value.isOffline) {
                        "${activityType.displayName} started (offline - will sync when connected)"
                    } else {
                        "${activityType.displayName} started successfully"
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = successMsg
                    )
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to start activity", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error?.message ?: "Failed to start ${activityType.displayName.lowercase()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting activity", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error starting ${activityType.displayName.lowercase()}: ${e.message}"
                )
            }
        }
    }
    
    /**
     * End ongoing activity with offline support
     */
    fun endActivity() {
        val babyId = currentBabyId ?: return
        val activityId = _uiState.value.ongoingActivity?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = offlineActivityService.endActivity(activityId, babyId)
                
                if (result.isSuccess) {
                    Log.i(TAG, "Activity ended successfully: ${activityType.displayName}")
                    
                    // Show success message that indicates offline mode
                    val successMsg = if (_uiState.value.isOffline) {
                        "${activityType.displayName} ended (offline - will sync when connected)"
                    } else {
                        "${activityType.displayName} ended successfully"
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = successMsg
                    )
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to end activity", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error?.message ?: "Failed to end ${activityType.displayName.lowercase()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ending activity", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error ending ${activityType.displayName.lowercase()}: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update start time for ongoing activity with offline support
     */
    fun updateStartTime(newStartTime: Date) {
        val babyId = currentBabyId ?: return
        val activityId = _uiState.value.ongoingActivity?.id ?: return
        
        viewModelScope.launch {
            try {
                val result = offlineActivityService.updateActivityStartTime(
                    activityId = activityId,
                    babyId = babyId,
                    newStartTime = Timestamp(newStartTime)
                )
                
                if (result.isSuccess) {
                    Log.i(TAG, "Start time updated successfully")
                    
                    val successMsg = if (_uiState.value.isOffline) {
                        "Start time updated (offline - will sync when connected)"
                    } else {
                        "Start time updated successfully"
                    }
                    
                    _uiState.value = _uiState.value.copy(successMessage = successMsg)
                    
                    // Restart timer with new start time
                    startTimer(newStartTime)
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to update start time", error)
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to update start time: ${error?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating start time", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating start time: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Manually trigger sync of pending operations
     */
    fun triggerSync() {
        offlineManager.triggerSync()
        _uiState.value = _uiState.value.copy(successMessage = "Sync triggered")
    }
    
    private fun startTimer(startTime: Date) {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val startTimeMillis = startTime.time
                val elapsedSeconds = (currentTime - startTimeMillis) / 1000
                
                _uiState.value = _uiState.value.copy(currentElapsedTime = elapsedSeconds)
                
                delay(1000) // Update every second
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(currentElapsedTime = 0L)
    }
    
    // Error clearing functions
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearOngoingActivityError() {
        _uiState.value = _uiState.value.copy(ongoingActivityError = null)
    }
    
    fun clearLastActivityError() {
        _uiState.value = _uiState.value.copy(lastActivityError = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
