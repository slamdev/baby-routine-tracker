package com.github.slamdev.babyroutinetracker.datavisualization

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.service.ActivityService
import com.github.slamdev.babyroutinetracker.util.ErrorUtils
import com.github.slamdev.babyroutinetracker.util.LocalizedMessageProvider
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class DataVisualizationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sleepData: List<DailySleepData> = emptyList(),
    val feedingData: List<DailyFeedingData> = emptyList(),
    val diaperData: List<DailyDiaperData> = emptyList(),
    val selectedDateRange: DateRange = DateRange.LAST_WEEK
)

data class DailySleepData(
    val date: Date,
    val totalHours: Float,
    val sleepSessions: Int
)

data class DailyFeedingData(
    val date: Date,
    val totalFeedings: Int,
    val breastFeedings: Int,
    val bottleFeedings: Int
)

data class DailyDiaperData(
    val date: Date,
    val totalDiapers: Int,
    val poopDiapers: Int
)

enum class DateRange(val days: Int) {
    LAST_WEEK(7),
    LAST_TWO_WEEKS(14),
    LAST_MONTH(30)
}

class DataVisualizationViewModel(application: Application) : AndroidViewModel(application) {

    private val activityService: ActivityService = ActivityService(application)
    
    private val messageProvider = LocalizedMessageProvider(application)
    
    companion object {
        private const val TAG = "DataVisualizationViewModel"
    }
    
    private val _uiState = MutableStateFlow(DataVisualizationUiState())
    val uiState: StateFlow<DataVisualizationUiState> = _uiState.asStateFlow()
    
    fun initialize(babyId: String) {
        loadDataForDateRange(babyId, _uiState.value.selectedDateRange)
    }
    
    fun onDateRangeChanged(babyId: String, dateRange: DateRange) {
        _uiState.value = _uiState.value.copy(selectedDateRange = dateRange)
        loadDataForDateRange(babyId, dateRange)
    }
    
    fun retryLoading(babyId: String) {
        loadDataForDateRange(babyId, _uiState.value.selectedDateRange)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun loadDataForDateRange(babyId: String, dateRange: DateRange) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val calendar = Calendar.getInstance()
                val endDate = Timestamp(Date()) // Today
                
                // Go back the specified number of days
                calendar.add(Calendar.DAY_OF_YEAR, -dateRange.days)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = Timestamp(calendar.time)
                
                // Load sleep activities
                val sleepResult = activityService.getActivitiesInDateRange(
                    babyId = babyId,
                    startDate = startDate,
                    endDate = endDate,
                    activityType = ActivityType.SLEEP
                )
                
                // Load feeding activities
                val feedingResult = activityService.getActivitiesInDateRange(
                    babyId = babyId,
                    startDate = startDate,
                    endDate = endDate,
                    activityType = ActivityType.FEEDING
                )
                
                // Load diaper activities
                val diaperResult = activityService.getActivitiesInDateRange(
                    babyId = babyId,
                    startDate = startDate,
                    endDate = endDate,
                    activityType = ActivityType.DIAPER
                )
                
                if (sleepResult.isSuccess && feedingResult.isSuccess && diaperResult.isSuccess) {
                    val sleepActivities = sleepResult.getOrNull() ?: emptyList()
                    val feedingActivities = feedingResult.getOrNull() ?: emptyList()
                    val diaperActivities = diaperResult.getOrNull() ?: emptyList()
                    
                    val sleepData = processSleepData(sleepActivities, startDate.toDate(), endDate.toDate())
                    val feedingData = processFeedingData(feedingActivities, startDate.toDate(), endDate.toDate())
                    val diaperData = processDiaperData(diaperActivities, startDate.toDate(), endDate.toDate())
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sleepData = sleepData,
                        feedingData = feedingData,
                        diaperData = diaperData,
                        errorMessage = null
                    )
                    
                    Log.d(TAG, "Data loaded successfully: ${sleepData.size} sleep days, ${feedingData.size} feeding days, ${diaperData.size} diaper days")
                } else {
                    val error = sleepResult.exceptionOrNull() ?: feedingResult.exceptionOrNull() ?: diaperResult.exceptionOrNull()
                    ErrorUtils.logError(TAG, "load activity data for visualization", error ?: Exception(messageProvider.getUnknownErrorMessage()), mapOf("babyId" to babyId, "dateRange" to _uiState.value.selectedDateRange.name))
                    
                    val userMessage = error?.let { 
                        ErrorUtils.getFirebaseErrorMessage(it, "view baby activities", "activity data") 
                    } ?: messageProvider.getUnableToLoadActivityDataErrorMessage()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = userMessage
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = messageProvider.getFailedToProcessActivityDataErrorMessage()
                )
            }
        }
    }
    
    private fun processSleepData(activities: List<Activity>, startDate: Date, endDate: Date): List<DailySleepData> {
        val calendar = Calendar.getInstance()
        val dailyData = mutableMapOf<String, MutableList<Activity>>()
        
        // Group activities by date
        activities.forEach { activity ->
            calendar.time = activity.startTime.toDate()
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            dailyData.getOrPut(dateKey) { mutableListOf() }.add(activity)
        }
        
        // Create data for each day in the range
        val result = mutableListOf<DailySleepData>()
        calendar.time = startDate
        
        while (calendar.time <= endDate) {
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            val dayActivities = dailyData[dateKey] ?: emptyList()
            
            val totalMinutes = dayActivities.sumOf { activity ->
                activity.getDurationMinutes() ?: 0L
            }
            val totalHours = totalMinutes / 60.0f
            
            result.add(
                DailySleepData(
                    date = calendar.time.clone() as Date,
                    totalHours = totalHours,
                    sleepSessions = dayActivities.size
                )
            )
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return result
    }
    
    private fun processFeedingData(activities: List<Activity>, startDate: Date, endDate: Date): List<DailyFeedingData> {
        val calendar = Calendar.getInstance()
        val dailyData = mutableMapOf<String, MutableList<Activity>>()
        
        // Group activities by date
        activities.forEach { activity ->
            calendar.time = activity.startTime.toDate()
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            dailyData.getOrPut(dateKey) { mutableListOf() }.add(activity)
        }
        
        // Create data for each day in the range
        val result = mutableListOf<DailyFeedingData>()
        calendar.time = startDate
        
        while (calendar.time <= endDate) {
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            val dayActivities = dailyData[dateKey] ?: emptyList()
            
            val breastFeedings = dayActivities.count { it.feedingType == "breast_milk" }
            val bottleFeedings = dayActivities.count { it.feedingType == "bottle" }
            
            result.add(
                DailyFeedingData(
                    date = calendar.time.clone() as Date,
                    totalFeedings = dayActivities.size,
                    breastFeedings = breastFeedings,
                    bottleFeedings = bottleFeedings
                )
            )
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return result
    }
    
    private fun processDiaperData(activities: List<Activity>, startDate: Date, endDate: Date): List<DailyDiaperData> {
        val calendar = Calendar.getInstance()
        val dailyData = mutableMapOf<String, MutableList<Activity>>()
        
        // Group activities by date
        activities.forEach { activity ->
            calendar.time = activity.startTime.toDate()
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            dailyData.getOrPut(dateKey) { mutableListOf() }.add(activity)
        }
        
        // Create data for each day in the range
        val result = mutableListOf<DailyDiaperData>()
        calendar.time = startDate
        
        while (calendar.time <= endDate) {
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            val dayActivities = dailyData[dateKey] ?: emptyList()
            
            val poopDiapers = dayActivities.count { it.diaperType == "poop" }
            
            result.add(
                DailyDiaperData(
                    date = calendar.time.clone() as Date,
                    totalDiapers = poopDiapers, // Only count poop diapers as total
                    poopDiapers = poopDiapers
                )
            )
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return result
    }
}
