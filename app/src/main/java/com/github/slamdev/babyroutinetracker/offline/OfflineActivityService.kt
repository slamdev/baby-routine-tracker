package com.github.slamdev.babyroutinetracker.offline

import android.content.Context
import android.util.Log
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.github.slamdev.babyroutinetracker.service.ActivityService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID

/**
 * Offline-first activity service that handles activity operations with offline support
 */
class OfflineActivityService(
    private val context: Context,
    private val onlineActivityService: ActivityService,
    private val offlineDatabase: OfflineDatabase,
    private val networkConnectivityService: NetworkConnectivityService,
    private val syncService: SyncService
) {
    
    companion object {
        private const val TAG = "OfflineActivityService"
    }
    
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()
    private val activityDao = offlineDatabase.activityDao()
    private val syncOperationDao = offlineDatabase.syncOperationDao()
    
    /**
     * Start a new activity with offline support
     */
    suspend fun startActivity(
        babyId: String,
        type: ActivityType,
        notes: String = ""
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to start activity - user not authenticated", error)
                return Result.failure(error)
            }
            
            // Check for existing ongoing activity locally first
            val existingActivity = activityDao.getOngoingActivity(babyId, type.name)
            if (existingActivity != null) {
                val error = Exception("There is already an ongoing ${type.displayName.lowercase()} activity")
                Log.w(TAG, "Attempted to start ${type.displayName} while one is already ongoing", error)
                return Result.failure(error)
            }
            
            val activityId = UUID.randomUUID().toString()
            val activity = Activity(
                id = activityId,
                type = type,
                babyId = babyId,
                startTime = Timestamp.now(),
                endTime = null,
                notes = notes,
                loggedBy = currentUser.uid,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            // Save locally first
            val localActivity = LocalActivity.fromActivity(activity, isSynced = false)
            activityDao.insertActivity(localActivity)
            
            // Try to sync online if network is available
            if (networkConnectivityService.isNetworkAvailable()) {
                try {
                    val result = onlineActivityService.startActivity(babyId, type, notes)
                    if (result.isSuccess) {
                        // Mark as synced
                        activityDao.markAsSynced(activityId)
                        Log.d(TAG, "Activity started and synced online: ${type.displayName}")
                    } else {
                        // Queue for later sync
                        queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                        Log.d(TAG, "Activity started offline, queued for sync: ${type.displayName}")
                    }
                } catch (e: Exception) {
                    // Queue for later sync
                    queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                    Log.d(TAG, "Activity started offline due to network error, queued for sync: ${type.displayName}")
                }
            } else {
                // Queue for later sync
                queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                Log.d(TAG, "Activity started offline (no network), queued for sync: ${type.displayName}")
            }
            
            Result.success(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start activity: ${type.displayName} for baby: $babyId", e)
            Result.failure(e)
        }
    }
    
    /**
     * End an ongoing activity with offline support
     */
    suspend fun endActivity(activityId: String, babyId: String): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to end activity - user not authenticated", error)
                return Result.failure(error)
            }
            
            // Get activity from local database
            val localActivity = activityDao.getActivityById(activityId)
            if (localActivity == null) {
                val error = Exception("Activity not found")
                Log.e(TAG, "Failed to end activity - activity not found: $activityId", error)
                return Result.failure(error)
            }
            
            if (localActivity.endTime != null) {
                val error = Exception("Activity is already completed")
                Log.w(TAG, "Attempted to end already completed activity: $activityId", error)
                return Result.failure(error)
            }
            
            // Update locally first
            val endTime = System.currentTimeMillis()
            val updatedLocalActivity = localActivity.copy(
                endTime = endTime,
                updatedAt = endTime,
                isSynced = false
            )
            activityDao.updateActivity(updatedLocalActivity)
            
            // Try to sync online if network is available
            if (networkConnectivityService.isNetworkAvailable()) {
                try {
                    val result = onlineActivityService.endActivity(activityId, babyId)
                    if (result.isSuccess) {
                        // Mark as synced
                        activityDao.markAsSynced(activityId)
                        Log.d(TAG, "Activity ended and synced online: $activityId")
                    } else {
                        // Queue for later sync
                        queueSyncOperation(SyncOperationType.END_ACTIVITY, activityId, babyId, "")
                        Log.d(TAG, "Activity ended offline, queued for sync: $activityId")
                    }
                } catch (e: Exception) {
                    // Queue for later sync
                    queueSyncOperation(SyncOperationType.END_ACTIVITY, activityId, babyId, "")
                    Log.d(TAG, "Activity ended offline due to network error, queued for sync: $activityId")
                }
            } else {
                // Queue for later sync
                queueSyncOperation(SyncOperationType.END_ACTIVITY, activityId, babyId, "")
                Log.d(TAG, "Activity ended offline (no network), queued for sync: $activityId")
            }
            
            Result.success(updatedLocalActivity.toActivity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end activity: $activityId for baby: $babyId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get ongoing activity flow with offline support
     */
    fun getOngoingActivityFlow(babyId: String, type: ActivityType): Flow<OptionalUiState<Activity>> {
        return combine(
            activityDao.getOngoingActivityFlow(babyId, type.name),
            networkConnectivityService.networkStatusFlow()
        ) { localActivity, networkStatus ->
            try {
                when {
                    localActivity != null -> {
                        OptionalUiState.Success(localActivity.toActivity())
                    }
                    networkStatus.isUnavailable -> {
                        // No local data and no network - show empty state
                        OptionalUiState.Empty
                    }
                    else -> {
                        // No local data but network available - will be fetched by online service
                        OptionalUiState.Empty
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing ongoing activity flow", e)
                OptionalUiState.Error(e, "Failed to load ongoing activity")
            }
        }
    }
    
    /**
     * Get last completed activity flow with offline support
     */
    fun getLastCompletedActivityFlow(babyId: String, type: ActivityType): Flow<OptionalUiState<Activity>> {
        return combine(
            activityDao.getLastCompletedActivityFlow(babyId, type.name),
            networkConnectivityService.networkStatusFlow()
        ) { localActivity, networkStatus ->
            try {
                when {
                    localActivity != null -> {
                        OptionalUiState.Success(localActivity.toActivity())
                    }
                    networkStatus.isUnavailable -> {
                        // No local data and no network - show empty state
                        OptionalUiState.Empty
                    }
                    else -> {
                        // No local data but network available - will be fetched by online service
                        OptionalUiState.Empty
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing last activity flow", e)
                OptionalUiState.Error(e, "Failed to load last activity")
            }
        }
    }
    
    /**
     * Get recent activities with offline support
     */
    suspend fun getRecentActivities(babyId: String, limit: Int = 50): Result<List<Activity>> {
        return try {
            val localActivities = activityDao.getRecentActivities(babyId, limit)
            val activities = localActivities.map { it.toActivity() }
            Result.success(activities)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get recent activities for baby: $babyId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get activities in date range with offline support
     */
    suspend fun getActivitiesInDateRange(
        babyId: String,
        startDate: Date,
        endDate: Date,
        activityType: ActivityType? = null
    ): Result<List<Activity>> {
        return try {
            val startMillis = startDate.time
            val endMillis = endDate.time
            
            val localActivities = if (activityType != null) {
                activityDao.getActivitiesInDateRangeByType(babyId, startMillis, endMillis, activityType.name)
            } else {
                activityDao.getActivitiesInDateRange(babyId, startMillis, endMillis)
            }
            
            val activities = localActivities.map { it.toActivity() }
            Result.success(activities)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get activities in date range for baby: $babyId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update activity start time with offline support
     */
    suspend fun updateActivityStartTime(
        activityId: String,
        babyId: String,
        newStartTime: Timestamp
    ): Result<Activity> {
        return try {
            val localActivity = activityDao.getActivityById(activityId)
            if (localActivity == null) {
                val error = Exception("Activity not found")
                Log.e(TAG, "Failed to update start time - activity not found: $activityId", error)
                return Result.failure(error)
            }
            
            // Update locally first
            val updatedLocalActivity = localActivity.copy(
                startTime = newStartTime.toDate().time,
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            activityDao.updateActivity(updatedLocalActivity)
            
            // Try to sync online or queue for later
            val operationData = gson.toJson(UpdateStartTimeData(newStartTime.toDate().time))
            if (networkConnectivityService.isNetworkAvailable()) {
                try {
                    val result = onlineActivityService.updateActivityStartTime(activityId, babyId, newStartTime)
                    if (result.isSuccess) {
                        activityDao.markAsSynced(activityId)
                    } else {
                        queueSyncOperation(SyncOperationType.UPDATE_START_TIME, activityId, babyId, operationData)
                    }
                } catch (e: Exception) {
                    queueSyncOperation(SyncOperationType.UPDATE_START_TIME, activityId, babyId, operationData)
                }
            } else {
                queueSyncOperation(SyncOperationType.UPDATE_START_TIME, activityId, babyId, operationData)
            }
            
            Result.success(updatedLocalActivity.toActivity())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update activity start time: $activityId", e)
            Result.failure(e)
        }
    }
    
    private suspend fun queueSyncOperation(
        type: SyncOperationType,
        activityId: String,
        babyId: String,
        operationData: String
    ) {
        try {
            val operation = SyncOperation(
                type = type,
                activityId = activityId,
                babyId = babyId,
                operationData = operationData
            )
            syncOperationDao.insertOperation(operation)
            Log.d(TAG, "Queued sync operation: $type for activity: $activityId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to queue sync operation", e)
        }
    }
    
    /**
     * Start a breast milk feeding session with offline support
     */
    suspend fun startBreastMilkFeeding(
        babyId: String,
        notes: String = ""
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to start breast milk feeding - user not authenticated", error)
                return Result.failure(error)
            }
            
            // Check for existing ongoing feeding activity locally first
            val existingActivity = activityDao.getOngoingActivity(babyId, ActivityType.FEEDING.name)
            if (existingActivity != null) {
                val error = Exception("There is already an ongoing feeding activity")
                Log.w(TAG, "Attempted to start breast milk feeding while one is already ongoing", error)
                return Result.failure(error)
            }
            
            val activityId = UUID.randomUUID().toString()
            val activity = Activity(
                id = activityId,
                type = ActivityType.FEEDING,
                babyId = babyId,
                startTime = Timestamp.now(),
                endTime = null,
                notes = notes,
                loggedBy = currentUser.uid,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                feedingType = "breast_milk",
                amount = 0.0
            )
            
            // Save locally first
            val localActivity = LocalActivity.fromActivity(activity, isSynced = false)
            activityDao.insertActivity(localActivity)
            
            // Try to sync online if network is available
            if (networkConnectivityService.isNetworkAvailable()) {
                try {
                    val result = onlineActivityService.startBreastMilkFeeding(babyId, notes)
                    if (result.isSuccess) {
                        // Mark as synced
                        activityDao.markAsSynced(activityId)
                        Log.d(TAG, "Breast milk feeding started and synced online")
                    } else {
                        // Queue for later sync
                        queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                        Log.d(TAG, "Breast milk feeding started offline, queued for sync")
                    }
                } catch (e: Exception) {
                    // Queue for later sync
                    queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                    Log.d(TAG, "Breast milk feeding started offline due to network error, queued for sync")
                }
            } else {
                // Queue for later sync
                queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                Log.d(TAG, "Breast milk feeding started offline (no network), queued for sync")
            }
            
            Result.success(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start breast milk feeding for baby: $babyId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Log a completed bottle feeding with offline support
     */
    suspend fun logBottleFeeding(
        babyId: String,
        amount: Double,
        notes: String = ""
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to log bottle feeding - user not authenticated", error)
                return Result.failure(error)
            }
            
            val activityId = UUID.randomUUID().toString()
            val now = Timestamp.now()
            val activity = Activity(
                id = activityId,
                type = ActivityType.FEEDING,
                babyId = babyId,
                startTime = now,
                endTime = now, // Instant completion for bottle feeding
                notes = notes,
                loggedBy = currentUser.uid,
                createdAt = now,
                updatedAt = now,
                feedingType = "bottle",
                amount = amount
            )
            
            // Save locally first
            val localActivity = LocalActivity.fromActivity(activity, isSynced = false)
            activityDao.insertActivity(localActivity)
            
            // Try to sync online if network is available
            if (networkConnectivityService.isNetworkAvailable()) {
                try {
                    val result = onlineActivityService.logCompletedFeeding(babyId, "bottle", 0, amount, notes)
                    if (result.isSuccess) {
                        // Mark as synced
                        activityDao.markAsSynced(activityId)
                        Log.d(TAG, "Bottle feeding logged and synced online: ${amount}ml")
                    } else {
                        // Queue for later sync
                        queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                        Log.d(TAG, "Bottle feeding logged offline, queued for sync: ${amount}ml")
                    }
                } catch (e: Exception) {
                    // Queue for later sync
                    queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                    Log.d(TAG, "Bottle feeding logged offline due to network error, queued for sync: ${amount}ml")
                }
            } else {
                // Queue for later sync
                queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                Log.d(TAG, "Bottle feeding logged offline (no network), queued for sync: ${amount}ml")
            }
            
            Result.success(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log bottle feeding for baby: $babyId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Log a poop diaper change with offline support
     */
    suspend fun logPoop(
        babyId: String,
        notes: String = ""
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to log poop - user not authenticated", error)
                return Result.failure(error)
            }
            
            val activityId = UUID.randomUUID().toString()
            val now = Timestamp.now()
            val activity = Activity(
                id = activityId,
                type = ActivityType.DIAPER,
                babyId = babyId,
                startTime = now,
                endTime = now, // Diaper change is logged as completed instantly
                notes = notes,
                loggedBy = currentUser.uid,
                createdAt = now,
                updatedAt = now,
                diaperType = "poop"
            )
            
            // Save locally first
            val localActivity = LocalActivity.fromActivity(activity, isSynced = false)
            activityDao.insertActivity(localActivity)
            
            // Try to sync online if network is available
            if (networkConnectivityService.isNetworkAvailable()) {
                try {
                    val result = onlineActivityService.logPoop(babyId, notes)
                    if (result.isSuccess) {
                        // Mark as synced
                        activityDao.markAsSynced(activityId)
                        Log.d(TAG, "Poop logged and synced online")
                    } else {
                        // Queue for later sync
                        queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                        Log.d(TAG, "Poop logged offline, queued for sync")
                    }
                } catch (e: Exception) {
                    // Queue for later sync
                    queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                    Log.d(TAG, "Poop logged offline due to network error, queued for sync")
                }
            } else {
                // Queue for later sync
                queueSyncOperation(SyncOperationType.CREATE_ACTIVITY, activityId, babyId, "")
                Log.d(TAG, "Poop logged offline (no network), queued for sync")
            }
            
            Result.success(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log poop for baby: $babyId", e)
            Result.failure(e)
        }
    }
}
