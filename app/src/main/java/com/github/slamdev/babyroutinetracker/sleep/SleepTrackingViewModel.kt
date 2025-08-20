package com.github.slamdev.babyroutinetracker.sleep

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.service.ActivityService
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SleepTrackingUiState(
    val isLoading: Boolean = false,
    val ongoingSleep: Activity? = null,
    val lastSleep: Activity? = null,
    val currentElapsedTime: Long = 0L, // in seconds
    val errorMessage: String? = null
)

class SleepTrackingViewModel : ViewModel() {
    private val activityService = ActivityService()
    
    private val _uiState = MutableStateFlow(SleepTrackingUiState())
    val uiState: StateFlow<SleepTrackingUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var currentBabyId: String? = null
    
    companion object {
        private const val TAG = "SleepTrackingViewModel"
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
        Log.d(TAG, "Initializing sleep tracking for baby: $babyId")
        
        // Start listening to ongoing sleep activity
        viewModelScope.launch {
            activityService.getOngoingActivityFlow(babyId, ActivityType.SLEEP)
                .collect { ongoingSleep ->
                    Log.d(TAG, "Ongoing sleep activity updated: ${if (ongoingSleep != null) "active" else "none"}")
                    _uiState.value = _uiState.value.copy(ongoingSleep = ongoingSleep)
                    
                    // Start or stop timer based on ongoing activity
                    if (ongoingSleep != null) {
                        startTimer(ongoingSleep.startTime)
                    } else {
                        stopTimer()
                    }
                }
        }
        
        // Start listening to last sleep activity
        viewModelScope.launch {
            activityService.getLastActivityFlow(babyId, ActivityType.SLEEP)
                .collect { lastSleep ->
                    Log.d(TAG, "Last sleep activity updated: ${lastSleep?.let { "found" } ?: "none"}")
                    _uiState.value = _uiState.value.copy(lastSleep = lastSleep)
                }
        }
    }

    /**
     * Start a new sleep session
     */
    fun startSleep(notes: String = "") {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot start sleep - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                Log.i(TAG, "Starting sleep session for baby: $babyId")
                val result = activityService.startActivity(babyId, ActivityType.SLEEP, notes)
                
                result.fold(
                    onSuccess = { activity ->
                        Log.i(TAG, "Sleep session started successfully: ${activity.id}")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to start sleep session", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to start sleep session"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error starting sleep session", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * End the current sleep session
     */
    fun endSleep() {
        val babyId = currentBabyId
        val ongoingSleep = _uiState.value.ongoingSleep
        
        if (babyId == null) {
            Log.e(TAG, "Cannot end sleep - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }
        
        if (ongoingSleep == null) {
            Log.w(TAG, "Cannot end sleep - no ongoing sleep session")
            _uiState.value = _uiState.value.copy(errorMessage = "No ongoing sleep session to end")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                Log.i(TAG, "Ending sleep session: ${ongoingSleep.id}")
                val result = activityService.endActivity(ongoingSleep.id, babyId)
                
                result.fold(
                    onSuccess = { activity ->
                        Log.i(TAG, "Sleep session ended successfully: ${activity.id}, duration: ${activity.durationMinutes} minutes")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to end sleep session", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to end sleep session"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error ending sleep session", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * Start the timer for tracking elapsed time
     */
    private fun startTimer(startTime: Timestamp) {
        stopTimer() // Stop any existing timer
        
        timerJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val startTimeMillis = startTime.seconds * 1000
                val elapsedSeconds = (currentTime - startTimeMillis) / 1000
                
                _uiState.value = _uiState.value.copy(currentElapsedTime = elapsedSeconds)
                
                delay(1000) // Update every second
            }
        }
        
        Log.d(TAG, "Started sleep timer")
    }

    /**
     * Stop the timer
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(currentElapsedTime = 0L)
        Log.d(TAG, "Stopped sleep timer")
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Format elapsed time for display
     */
    fun formatElapsedTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        Log.d(TAG, "SleepTrackingViewModel cleared")
    }
}
