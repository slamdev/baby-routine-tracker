package com.github.slamdev.babyroutinetracker.offline

import android.content.Context
import android.util.Log
import com.github.slamdev.babyroutinetracker.service.ActivityService
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * Service for syncing offline data with Firebase
 */
class SyncService(
    private val context: Context,
    private val activityService: ActivityService,
    private val offlineDatabase: OfflineDatabase,
    private val networkConnectivityService: NetworkConnectivityService
) {
    
    companion object {
        private const val TAG = "SyncService"
    }
    
    private val gson = Gson()
    private val activityDao = offlineDatabase.activityDao()
    private val syncOperationDao = offlineDatabase.syncOperationDao()
    
    /**
     * Sync all pending operations with Firebase
     */
    suspend fun syncPendingOperations(): SyncResult {
        if (!networkConnectivityService.isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable, skipping sync")
            return SyncResult.NetworkUnavailable
        }
        
        val operations = syncOperationDao.getRetryableOperations()
        Log.d(TAG, "Starting sync for ${operations.size} pending operations")
        
        var successCount = 0
        var failureCount = 0
        
        for (operation in operations) {
            if (!operation.canRetryNow()) {
                Log.d(TAG, "Operation ${operation.id} not ready for retry yet")
                continue
            }
            
            try {
                val success = when (operation.type) {
                    SyncOperationType.CREATE_ACTIVITY -> syncCreateActivity(operation)
                    SyncOperationType.UPDATE_ACTIVITY -> syncUpdateActivity(operation)
                    SyncOperationType.END_ACTIVITY -> syncEndActivity(operation)
                    SyncOperationType.UPDATE_START_TIME -> syncUpdateStartTime(operation)
                    SyncOperationType.UPDATE_TIMES -> syncUpdateTimes(operation)
                    SyncOperationType.UPDATE_NOTES -> syncUpdateNotes(operation)
                }
                
                if (success) {
                    syncOperationDao.deleteOperation(operation)
                    successCount++
                    Log.d(TAG, "Successfully synced operation ${operation.id}")
                } else {
                    updateOperationAttempt(operation)
                    failureCount++
                    Log.w(TAG, "Failed to sync operation ${operation.id}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing operation ${operation.id}", e)
                updateOperationAttempt(operation)
                failureCount++
            }
        }
        
        Log.i(TAG, "Sync completed: $successCount successful, $failureCount failed")
        return SyncResult.Success(successCount, failureCount)
    }
    
    /**
     * Sync recent Firebase activities to local database for offline access
     */
    suspend fun syncFromFirebase(babyId: String) {
        if (!networkConnectivityService.isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable, skipping Firebase sync")
            return
        }
        
        try {
            Log.d(TAG, "Syncing recent activities from Firebase for baby: $babyId")
            
            val result = activityService.getRecentActivities(babyId, limit = 100)
            if (result.isSuccess) {
                val activities = result.getOrNull() ?: emptyList()
                val localActivities = activities.map { activity ->
                    LocalActivity.fromActivity(activity, isSynced = true)
                }
                
                activityDao.insertActivities(localActivities)
                Log.i(TAG, "Successfully synced ${activities.size} activities from Firebase")
            } else {
                Log.w(TAG, "Failed to fetch activities from Firebase", result.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firebase", e)
        }
    }
    
    private suspend fun syncCreateActivity(operation: SyncOperation): Boolean {
        val localActivity = activityDao.getActivityById(operation.activityId) ?: return false
        val activity = localActivity.toActivity()
        
        val result = activityService.startActivity(
            babyId = activity.babyId,
            type = activity.type,
            notes = activity.notes
        )
        
        if (result.isSuccess) {
            activityDao.markAsSynced(operation.activityId)
            return true
        }
        
        return false
    }
    
    private suspend fun syncUpdateActivity(operation: SyncOperation): Boolean {
        try {
            val data = gson.fromJson(operation.operationData, UpdateActivityData::class.java)
            
            // Get the current activity first
            val localActivity = activityDao.getActivityById(operation.activityId) ?: return false
            val activity = localActivity.toActivity()
            
            val result = activityService.updateActivity(
                babyId = operation.babyId,
                activity = activity
            )
            
            if (result.isSuccess) {
                activityDao.markAsSynced(operation.activityId)
                return true
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Invalid operation data for update activity", e)
        }
        
        return false
    }
    
    private suspend fun syncEndActivity(operation: SyncOperation): Boolean {
        val result = activityService.endActivity(
            activityId = operation.activityId,
            babyId = operation.babyId
        )
        
        if (result.isSuccess) {
            activityDao.markAsSynced(operation.activityId)
            return true
        }
        
        return false
    }
    
    private suspend fun syncUpdateStartTime(operation: SyncOperation): Boolean {
        try {
            val data = gson.fromJson(operation.operationData, UpdateStartTimeData::class.java)
            val result = activityService.updateActivityStartTime(
                activityId = operation.activityId,
                babyId = operation.babyId,
                newStartTime = Timestamp(Date(data.newStartTime))
            )
            
            if (result.isSuccess) {
                activityDao.markAsSynced(operation.activityId)
                return true
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Invalid operation data for update start time", e)
        }
        
        return false
    }
    
    private suspend fun syncUpdateTimes(operation: SyncOperation): Boolean {
        try {
            val data = gson.fromJson(operation.operationData, UpdateTimesData::class.java)
            val result = activityService.updateActivityTimes(
                activityId = operation.activityId,
                babyId = operation.babyId,
                newStartTime = Timestamp(Date(data.newStartTime)),
                newEndTime = Timestamp(Date(data.newEndTime))
            )
            
            if (result.isSuccess) {
                activityDao.markAsSynced(operation.activityId)
                return true
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Invalid operation data for update times", e)
        }
        
        return false
    }
    
    private suspend fun syncUpdateNotes(operation: SyncOperation): Boolean {
        try {
            val data = gson.fromJson(operation.operationData, UpdateNotesData::class.java)
            val result = activityService.updateActivityNotes(
                activityId = operation.activityId,
                babyId = operation.babyId,
                newNotes = data.newNotes
            )
            
            if (result.isSuccess) {
                activityDao.markAsSynced(operation.activityId)
                return true
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Invalid operation data for update notes", e)
        }
        
        return false
    }
    
    private suspend fun updateOperationAttempt(operation: SyncOperation) {
        val updatedOperation = operation.copy(
            attempts = operation.attempts + 1,
            lastAttempt = System.currentTimeMillis()
        )
        syncOperationDao.updateOperation(updatedOperation)
    }
    
    /**
     * Clean up old synced data to free up space
     */
    suspend fun cleanupOldData() {
        try {
            // Delete synced activities older than 30 days
            val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
            activityDao.deleteOldSyncedActivities(cutoffTime)
            Log.d(TAG, "Cleaned up old synced activities")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old data", e)
        }
    }
}

/**
 * Data classes for sync operation payloads
 */
data class UpdateActivityData(val updates: Map<String, Any>)
data class UpdateStartTimeData(val newStartTime: Long)
data class UpdateTimesData(val newStartTime: Long, val newEndTime: Long)
data class UpdateNotesData(val newNotes: String)

/**
 * Result of a sync operation
 */
sealed class SyncResult {
    object NetworkUnavailable : SyncResult()
    data class Success(val successCount: Int, val failureCount: Int) : SyncResult()
    data class Failure(val error: Throwable) : SyncResult()
}
