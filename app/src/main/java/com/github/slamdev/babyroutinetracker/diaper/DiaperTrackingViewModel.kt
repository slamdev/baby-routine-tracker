package com.github.slamdev.babyroutinetracker.diaper

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.github.slamdev.babyroutinetracker.service.ActivityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiaperTrackingUiState(
    val isLoading: Boolean = false,
    val lastDiaper: Activity? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val lastDiaperError: String? = null,
    val isLoadingLastDiaper: Boolean = false
)

class DiaperTrackingViewModel : ViewModel() {
    private val activityService = ActivityService()
    
    private val _uiState = MutableStateFlow(DiaperTrackingUiState())
    val uiState: StateFlow<DiaperTrackingUiState> = _uiState.asStateFlow()
    
    private var currentBabyId: String? = null
    
    companion object {
        private const val TAG = "DiaperTrackingViewModel"
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
        Log.d(TAG, "Initializing diaper tracking for baby: $babyId")
        
        // Start listening to last diaper activity
        viewModelScope.launch {
            activityService.getLastActivityFlow(babyId, ActivityType.DIAPER)
                .collect { lastDiaperState ->
                    when (lastDiaperState) {
                        is OptionalUiState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastDiaper = true,
                                lastDiaperError = null
                            )
                        }
                        is OptionalUiState.Success -> {
                            Log.d(TAG, "Last diaper activity updated: found")
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastDiaper = false,
                                lastDiaper = lastDiaperState.data,
                                lastDiaperError = null
                            )
                        }
                        is OptionalUiState.Empty -> {
                            Log.d(TAG, "No last diaper activity found")
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastDiaper = false,
                                lastDiaper = null,
                                lastDiaperError = null
                            )
                        }
                        is OptionalUiState.Error -> {
                            Log.e(TAG, "Error getting last diaper activity", lastDiaperState.exception)
                            _uiState.value = _uiState.value.copy(
                                isLoadingLastDiaper = false,
                                lastDiaper = null,
                                lastDiaperError = lastDiaperState.message
                            )
                        }
                    }
                }
        }
    }

    /**
     * Log a poop diaper change
     */
    fun logPoop(notes: String = "") {
        val babyId = currentBabyId
        if (babyId == null) {
            Log.e(TAG, "Cannot log poop - no baby selected")
            _uiState.value = _uiState.value.copy(errorMessage = "No baby profile selected")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
                
                Log.i(TAG, "Logging poop for baby: $babyId")
                val result = activityService.logPoop(babyId, notes)
                
                result.fold(
                    onSuccess = { activity ->
                        Log.i(TAG, "Poop logged successfully: ${activity.id}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Poop logged successfully!"
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to log poop", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to log poop"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error logging poop", e)
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
     * Clear any success message
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Clear last diaper error
     */
    fun clearLastDiaperError() {
        _uiState.value = _uiState.value.copy(lastDiaperError = null)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "DiaperTrackingViewModel cleared")
    }
}
