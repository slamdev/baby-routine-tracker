package com.github.slamdev.babyroutinetracker.feeding

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

data class FeedingTrackingUiState(
    val isLoading: Boolean = false,
    val ongoingBreastFeeding: Activity? = null,
    val lastFeeding: Activity? = null,
    val currentElapsedTime: Long = 0L, // in seconds
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val ongoingFeedingError: String? = null,
    val lastFeedingError: String? = null,
    val isLoadingOngoingFeeding: Boolean = false,
    val isLoadingLastFeeding: Boolean = false
)

class FeedingTrackingViewModel(
    private val context: android.content.Context
) : ViewModel() {
    private val offlineManager = OfflineManager.getInstance(context)
    private val offlineActivityService = offlineManager.getOfflineActivityService()
    private val onlineActivityService = ActivityService()
    
    private val _uiState = MutableStateFlow(FeedingTrackingUiState())
    val uiState: StateFlow<FeedingTrackingUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var currentBabyId: String? = null
    
    companion object {
        private const val TAG = "FeedingTrackingViewModel"
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
        Log.d(TAG, "Initializing feeding tracking for baby: $babyId")
        
        // Start listening to ongoing breast milk feeding activity
        viewModelScope.launch {
            offlineActivityService.getOngoingActivityFlow(babyId, ActivityType.FEEDING)
                .collect { ongoingFeedingState ->
                    when (ongoingFeedingState) {
                        is OptionalUiState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingFeeding = true,
                                ongoingFeedingError = null
                            )
                        }
                        is OptionalUiState.Success -> {
                            // Only track ongoing breast milk feeding sessions
                            val ongoingBreastFeeding = ongoingFeedingState.data.takeIf { it.feedingType == "breast_milk" }
                            Log.d(TAG, "Ongoing breast feeding activity updated: ${if (ongoingBreastFeeding != null) "active" else "none"}")
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingFeeding = false,
                                ongoingBreastFeeding = ongoingBreastFeeding,
                                ongoingFeedingError = null
                            )
                            
                            // Start or stop timer based on ongoing activity
                            if (ongoingBreastFeeding != null) {
                                startTimer(ongoingBreastFeeding.startTime)
                            } else {
                                stopTimer()
                            }
                        }
                        is OptionalUiState.Empty -> {
                            Log.d(TAG, "No ongoing breast feeding activity")
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingFeeding = false,
                                ongoingBreastFeeding = null,
                                ongoingFeedingError = null
                            )
                            stopTimer()
                        }
                        is OptionalUiState.Error -> {
                            Log.e(TAG, "Error getting ongoing feeding activity", ongoingFeedingState.exception)
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingFeeding = false,
                                ongoingBreastFeeding = null,
                                ongoingFeedingError = ongoingFeedingState.message
                            )
                            stopTimer()
                        }
                    }
                }
        }
        
        // Start listening to last feeding activity
        viewModelScope.launch {
            offlineActivityService.getLastCompletedActivityFlow(babyId, ActivityType.FEEDING)
                .collect { lastFeedingState ->
                    when (lastFeedingState) {
                        is OptionalUiState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastFeeding = true,
                                lastFeedingError = null
                            )
                        }
                        is OptionalUiState.Success -> {
                            Log.d(TAG, "Last feeding activity updated: found")
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastFeeding = false,
                                lastFeeding = lastFeedingState.data,
                                lastFeedingError = null
                            )
                        }
                        is OptionalUiState.Empty -> {
                            Log.d(TAG, "No last feeding activity found")
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastFeeding = false,
                                lastFeeding = null,
                                lastFeedingError = null
                            )
                        }
                        is OptionalUiState.Error -> {
                            Log.e(TAG, "Error getting last feeding activity", lastFeedingState.exception)
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastFeeding = false,
                                lastFeeding = null,
                                lastFeedingError = lastFeedingState.message
                            )
                        }
                    }
                }
        }
    }

    /**
     * Start a breast milk feeding session
     */
    fun startBreastMilkFeeding(notes: String = "") {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot start breast milk feeding - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                Log.i(TAG, "Starting breast milk feeding session for baby: $babyId")
                val result = offlineActivityService.startBreastMilkFeeding(babyId, notes)
                
                result.fold(
                    onSuccess = { activity ->
                        Log.i(TAG, "Breast milk feeding started successfully: ${activity.id}")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to start breast milk feeding", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to start breast milk feeding"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error starting breast milk feeding", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * End the current breast milk feeding session
     */
    fun endBreastMilkFeeding() {
        val babyId = currentBabyId
        val ongoingBreastFeeding = _uiState.value.ongoingBreastFeeding
        
        if (babyId == null) {
            Log.e(TAG, "Cannot end breast milk feeding - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }
        
        if (ongoingBreastFeeding == null) {
            Log.w(TAG, "Cannot end breast milk feeding - no ongoing session")
            _uiState.value = _uiState.value.copy(errorMessage = "No ongoing breast milk feeding session to end")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                Log.i(TAG, "Ending breast milk feeding session: ${ongoingBreastFeeding.id}")
                val result = offlineActivityService.endActivity(ongoingBreastFeeding.id, babyId)
                
                result.fold(
                    onSuccess = { activity ->
                        Log.i(TAG, "Breast milk feeding ended successfully: ${activity.id}, duration: ${activity.getDurationMinutes()} minutes")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to end breast milk feeding", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to end breast milk feeding"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error ending breast milk feeding", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * Log a completed bottle feeding
     */
    fun logBottleFeeding(amount: Double, notes: String = "") {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot log bottle feeding - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                Log.i(TAG, "Logging bottle feeding for baby: $babyId, amount: ${amount}ml")
                val result = offlineActivityService.logBottleFeeding(babyId, amount, notes)
                
                result.fold(
                    onSuccess = { activity ->
                        Log.i(TAG, "Bottle feeding logged successfully: ${activity.id}")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to log bottle feeding", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to log bottle feeding"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error logging bottle feeding", e)
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
        
        Log.d(TAG, "Started feeding timer")
    }

    /**
     * Stop the timer
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(currentElapsedTime = 0L)
        Log.d(TAG, "Stopped feeding timer")
    }

    /**
     * Update the start time of the ongoing breast feeding activity
     */
    fun updateBreastFeedingStartTime(newStartTime: Date) {
        val babyId = currentBabyId
        val ongoingFeeding = _uiState.value.ongoingBreastFeeding
        if (babyId == null || ongoingFeeding == null) {
            Log.e(TAG, "Cannot update start time - no ongoing breast feeding")
            _uiState.value = _uiState.value.copy(errorMessage = "No ongoing breast feeding to update")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = offlineActivityService.updateActivityStartTime(
                ongoingFeeding.id,
                babyId,
                Timestamp(newStartTime)
            )
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Start time updated.")
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to update start time.")
                }
            )
        }
    }

    /**
     * Update a completed feeding activity (both time and notes).
     */
    fun updateCompletedFeeding(originalActivity: Activity, newStartTime: Date, newEndTime: Date?, newNotes: String?) {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot update feeding - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            // Create the updated activity object
            val updatedActivity = originalActivity.copy(
                startTime = Timestamp(newStartTime),
                endTime = if (originalActivity.isInstantActivity()) Timestamp(newStartTime) else newEndTime?.let { Timestamp(it) },
                notes = newNotes ?: originalActivity.notes
            )

            Log.i(TAG, "Updating feeding activity: ${updatedActivity.id}")
            val result = onlineActivityService.updateActivity(babyId, updatedActivity)

            result.fold(
                onSuccess = {
                    Log.i(TAG, "Feeding activity updated successfully: ${it.id}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Feeding updated successfully!"
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update feeding activity", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to update feeding"
                    )
                }
            )
        }
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

    /**
     * Clear any error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clear ongoing feeding error
     */
    fun clearOngoingFeedingError() {
        _uiState.value = _uiState.value.copy(ongoingFeedingError = null)
    }

    /**
     * Clear last feeding error
     */
    fun clearLastFeedingError() {
        _uiState.value = _uiState.value.copy(lastFeedingError = null)
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
