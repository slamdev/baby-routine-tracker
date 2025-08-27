package com.github.slamdev.babyroutinetracker.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.github.slamdev.babyroutinetracker.service.ActivityService
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class ActivityHistoryUiState(
    val isLoading: Boolean = false,
    val activities: List<Activity> = emptyList(),
    val filteredActivities: List<Activity> = emptyList(),
    val selectedActivityType: ActivityType? = null, // null means show all
    val errorMessage: String? = null
)

class ActivityHistoryViewModel : ViewModel() {
    private val activityService = ActivityService()
    
    private val _uiState = MutableStateFlow(ActivityHistoryUiState())
    val uiState: StateFlow<ActivityHistoryUiState> = _uiState.asStateFlow()
    
    private var currentBabyId: String? = null
    
    companion object {
        private const val TAG = "ActivityHistoryViewModel"
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
        Log.d(TAG, "Initializing activity history for baby: $babyId")
        
        setupRealtimeActivitiesListener()
    }
    
    /**
     * Set up real-time listener for activities
     */
    private fun setupRealtimeActivitiesListener() {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot setup activities listener - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                Log.i(TAG, "Setting up real-time activities listener for baby: $babyId")
                
                activityService.getRecentActivitiesFlow(babyId, limit = 50)
                    .collect { activitiesState ->
                        when (activitiesState) {
                            is OptionalUiState.Loading -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = true,
                                    errorMessage = null
                                )
                            }
                            is OptionalUiState.Success -> {
                                Log.i(TAG, "Real-time activities update: ${activitiesState.data.size} activities")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    activities = activitiesState.data,
                                    filteredActivities = filterActivities(activitiesState.data, _uiState.value.selectedActivityType),
                                    errorMessage = null
                                )
                            }
                            is OptionalUiState.Empty -> {
                                Log.i(TAG, "Real-time activities update: no activities found")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    activities = emptyList(),
                                    filteredActivities = emptyList(),
                                    errorMessage = null
                                )
                            }
                            is OptionalUiState.Error -> {
                                Log.e(TAG, "Real-time activities error", activitiesState.exception)
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = activitiesState.message
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in activities listener", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update activity times
     */
    fun updateActivityTimes(activity: Activity, newStartTime: Date, newEndTime: Date) {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot update activity - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                val newStartTimestamp = Timestamp(newStartTime)
                val newEndTimestamp = Timestamp(newEndTime)
                
                Log.i(TAG, "Updating activity times: ${activity.id}")
                val result = activityService.updateActivityTimes(
                    activity.id, 
                    babyId, 
                    newStartTimestamp, 
                    newEndTimestamp
                )
                
                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Activity times updated successfully: ${updatedActivity.id}")
                        // Real-time listener will automatically update the UI
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update activity times", exception)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = exception.message ?: "Failed to update activity times"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating activity times", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update instant activity time (bottle feeding, diaper)
     */
    fun updateInstantActivityTime(activity: Activity, newTime: Date) {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot update instant activity time - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                val newTimestamp = Timestamp(newTime)
                Log.i(TAG, "Updating instant activity time: ${activity.id}")
                val result = activityService.updateInstantActivityTime(activity.id, babyId, newTimestamp)
                
                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Instant activity time updated successfully: ${updatedActivity.id}")
                        // Real-time listener will automatically update the UI
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update instant activity time", exception)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = exception.message ?: "Failed to update activity time"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating instant activity time", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * Update activity notes
     */
    fun updateActivityNotes(activity: Activity, newNotes: String) {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot update activity notes - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                Log.i(TAG, "Updating activity notes: ${activity.id}")
                val result = activityService.updateActivityNotes(activity.id, babyId, newNotes)
                
                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Activity notes updated successfully: ${updatedActivity.id}")
                        // Real-time listener will automatically update the UI
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update activity notes", exception)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = exception.message ?: "Failed to update activity notes"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating activity notes", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update an activity
     */
    fun updateActivity(activity: Activity) {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot update activity - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                Log.i(TAG, "Updating activity: ${activity.id}")
                val result = activityService.updateActivity(babyId, activity)

                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Activity updated successfully: ${updatedActivity.id}")
                        // Real-time listener will automatically update the UI
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update activity", exception)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = exception.message ?: "Failed to update activity"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating activity", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Filter activities by type
     */
    fun filterByActivityType(activityType: ActivityType?) {
        _uiState.value = _uiState.value.copy(
            selectedActivityType = activityType,
            filteredActivities = filterActivities(_uiState.value.activities, activityType)
        )
    }

    /**
     * Delete an activity
     */
    fun deleteActivity(activity: Activity) {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot delete activity - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                Log.i(TAG, "Deleting activity: ${activity.id}")
                val result = activityService.deleteActivity(activity.id, babyId)

                result.fold(
                    onSuccess = {
                        Log.i(TAG, "Activity deleted successfully: ${activity.id}")
                        // Real-time listener will automatically update the UI
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to delete activity", exception)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = exception.message ?: "Failed to delete activity"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error deleting activity", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /**
     * Filter activities based on selected type
     */
    private fun filterActivities(activities: List<Activity>, activityType: ActivityType?): List<Activity> {
        return if (activityType == null) {
            activities
        } else {
            activities.filter { it.type == activityType }
        }
    }
}
