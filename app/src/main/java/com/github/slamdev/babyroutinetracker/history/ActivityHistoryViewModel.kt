package com.github.slamdev.babyroutinetracker.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
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
        
        loadActivities()
    }
    
    /**
     * Load recent activities for the baby
     */
    private fun loadActivities() {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot load activities - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                Log.i(TAG, "Loading recent activities for baby: $babyId")
                val result = activityService.getRecentActivities(babyId, limit = 50)
                
                result.fold(
                    onSuccess = { activities ->
                        Log.i(TAG, "Loaded ${activities.size} activities successfully")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            activities = activities,
                            filteredActivities = filterActivities(activities, _uiState.value.selectedActivityType)
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to load activities", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load activities"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading activities", e)
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
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
                        // Reload activities to get fresh data
                        loadActivities()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update activity times", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update activity times"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating activity times", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val newTimestamp = Timestamp(newTime)
                Log.i(TAG, "Updating instant activity time: ${activity.id}")
                val result = activityService.updateInstantActivityTime(activity.id, babyId, newTimestamp)
                
                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Instant activity time updated successfully: ${updatedActivity.id}")
                        // Reload activities to get fresh data
                        loadActivities()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update instant activity time", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update activity time"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating instant activity time", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                Log.i(TAG, "Updating activity notes: ${activity.id}")
                val result = activityService.updateActivityNotes(activity.id, babyId, newNotes)
                
                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Activity notes updated successfully: ${updatedActivity.id}")
                        // Reload activities to get fresh data
                        loadActivities()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update activity notes", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update activity notes"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating activity notes", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                Log.i(TAG, "Updating activity: ${activity.id}")
                val result = activityService.updateActivity(babyId, activity)

                result.fold(
                    onSuccess = { updatedActivity ->
                        Log.i(TAG, "Activity updated successfully: ${updatedActivity.id}")
                        // Reload activities to get fresh data
                        loadActivities()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to update activity", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to update activity"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating activity", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                Log.i(TAG, "Deleting activity: ${activity.id}")
                val result = activityService.deleteActivity(activity.id, babyId)

                result.fold(
                    onSuccess = {
                        Log.i(TAG, "Activity deleted successfully: ${activity.id}")
                        // Reload activities to get fresh data
                        loadActivities()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to delete activity", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to delete activity"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error deleting activity", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
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
