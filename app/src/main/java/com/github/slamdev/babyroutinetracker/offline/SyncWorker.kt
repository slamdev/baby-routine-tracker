package com.github.slamdev.babyroutinetracker.offline

import android.content.Context
import android.util.Log
import androidx.work.*
import com.github.slamdev.babyroutinetracker.service.ActivityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for background sync operations
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "sync_offline_data"
        
        /**
         * Schedule periodic sync work
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val periodicWork = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15, // 15 minutes minimum for periodic work
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
            
            Log.d(TAG, "Scheduled periodic sync work")
        }
        
        /**
         * Schedule immediate one-time sync
         */
        fun scheduleImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val oneTimeWork = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "immediate_sync",
                ExistingWorkPolicy.REPLACE,
                oneTimeWork
            )
            
            Log.d(TAG, "Scheduled immediate sync work")
        }
        
        /**
         * Cancel all sync work
         */
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork("immediate_sync")
            Log.d(TAG, "Cancelled all sync work")
        }
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting background sync")
            
            // Initialize services
            val database = OfflineDatabase.getDatabase(applicationContext)
            val activityService = ActivityService()
            val networkService = NetworkConnectivityService(applicationContext)
            val syncService = SyncService(applicationContext, activityService, database, networkService)
            
            // Check network availability
            if (!networkService.isNetworkAvailable()) {
                Log.d(TAG, "Network unavailable, sync will be retried later")
                return@withContext Result.retry()
            }
            
            // Perform sync
            val syncResult = syncService.syncPendingOperations()
            
            when (syncResult) {
                is SyncResult.Success -> {
                    Log.i(TAG, "Background sync completed: ${syncResult.successCount} successful, ${syncResult.failureCount} failed")
                    
                    // Clean up old data periodically
                    syncService.cleanupOldData()
                    
                    // If there were failures, retry later
                    if (syncResult.failureCount > 0) {
                        Result.retry()
                    } else {
                        Result.success()
                    }
                }
                SyncResult.NetworkUnavailable -> {
                    Log.d(TAG, "Network became unavailable during sync")
                    Result.retry()
                }
                is SyncResult.Failure -> {
                    Log.e(TAG, "Background sync failed", syncResult.error)
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during background sync", e)
            Result.failure()
        }
    }
}
