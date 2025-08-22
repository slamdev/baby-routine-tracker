package com.github.slamdev.babyroutinetracker.sleep

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.github.slamdev.babyroutinetracker.offline.OfflineManager
import com.github.slamdev.babyroutinetracker.service.ActivityService
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class SleepTrackingUiState(
    val isLoading: Boolean = false,
    val ongoingSleep: Activity? = null,
    val lastSleep: Activity? = null,
    val currentElapsedTime: Long = 0L, // in seconds
    val errorMessage: String? = null,
    val ongoingSleepError: String? = null,
    val lastSleepError: String? = null,
    val isLoadingOngoingSleep: Boolean = false,
    val isLoadingLastSleep: Boolean = false,
    val successMessage: String? = null
)

class SleepTrackingViewModel(
    private val context: android.content.Context
) : ViewModel() {
    private val offlineManager = OfflineManager.getInstance(context)
    private val offlineActivityService = offlineManager.getOfflineActivityService()
    private val onlineActivityService = ActivityService()
    
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
            offlineActivityService.getOngoingActivityFlow(babyId, ActivityType.SLEEP)
                .collect { ongoingSleepState ->
                    when (ongoingSleepState) {
                        is OptionalUiState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingSleep = true,
                                ongoingSleepError = null
                            )
                        }
                        is OptionalUiState.Success -> {
                            Log.d(TAG, "Ongoing sleep activity updated: active")
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingSleep = false,
                                ongoingSleep = ongoingSleepState.data,
                                ongoingSleepError = null
                            )
                            startTimer(ongoingSleepState.data.startTime)
                        }
                        is OptionalUiState.Empty -> {
                            Log.d(TAG, "No ongoing sleep activity")
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingSleep = false,
                                ongoingSleep = null,
                                ongoingSleepError = null
                            )
                            stopTimer()
                        }
                        is OptionalUiState.Error -> {
                            Log.e(TAG, "Error getting ongoing sleep activity", ongoingSleepState.exception)
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingSleep = false,
                                ongoingSleep = null,
                                ongoingSleepError = ongoingSleepState.message
                            )
                            stopTimer()
                        }
                    }
                }
        }
        
        // Start listening to last sleep activity
        viewModelScope.launch {
            offlineActivityService.getLastCompletedActivityFlow(babyId, ActivityType.SLEEP)
                .collect { lastSleepState ->
                    when (lastSleepState) {
                        is OptionalUiState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastSleep = true,
                                lastSleepError = null
                            )
                        }
                        is OptionalUiState.Success -> {
                            Log.d(TAG, "Last sleep activity updated: found")
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastSleep = false,
                                lastSleep = lastSleepState.data,
                                lastSleepError = null
                            )
                        }
                        is OptionalUiState.Empty -> {
                            Log.d(TAG, "No last sleep activity found")
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastSleep = false,
                                lastSleep = null,
                                lastSleepError = null
                            )
                        }
                        is OptionalUiState.Error -> {
                            Log.e(TAG, "Error getting last sleep activity", lastSleepState.exception)
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastSleep = false,
                                lastSleep = null,
                                lastSleepError = lastSleepState.message
                            )
                        }
                    }
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
                val result = offlineActivityService.startActivity(babyId, ActivityType.SLEEP, notes)
                
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
                val result = offlineActivityService.endActivity(ongoingSleep.id, babyId)
                
                result.fold(
                    onSuccess = { activity ->
                        Log.i(TAG, "Sleep session ended successfully: ${activity.id}, duration: ${activity.getDurationMinutes()} minutes")
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

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Update start time for an ongoing sleep activity
     */
    fun updateSleepStartTime(newStartTime: Date) {
        val babyId = currentBabyId
        val ongoingSleep = _uiState.value.ongoingSleep
        if (babyId == null || ongoingSleep == null) {
            Log.e(TAG, "Cannot update start time - no ongoing sleep")
            _uiState.value = _uiState.value.copy(errorMessage = "No ongoing sleep to update")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                Log.i(TAG, "Updating sleep start time for activity: ${ongoingSleep.id}")
                val result = offlineActivityService.updateActivityStartTime(
                    ongoingSleep.id,
                    babyId,
                    Timestamp(newStartTime)
                )

                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Sleep start time updated successfully: ${updatedActivity.id}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Start time updated successfully!"
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update sleep start time", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update start time"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating sleep start time", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * Update times for a completed sleep activity
     */
    fun updateCompletedSleep(activity: Activity) {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot update sleep - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                Log.i(TAG, "Updating sleep activity: ${activity.id}")
                val result = onlineActivityService.updateActivity(babyId, activity)

                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Sleep activity updated successfully: ${updatedActivity.id}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Sleep activity updated successfully!"
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update sleep activity", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update sleep activity"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating sleep activity", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }
}
