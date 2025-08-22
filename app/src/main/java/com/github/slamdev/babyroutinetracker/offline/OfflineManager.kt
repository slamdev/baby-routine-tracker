package com.github.slamdev.babyroutinetracker.offline

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.github.slamdev.babyroutinetracker.service.ActivityService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Centralized manager for offline functionality
 */
class OfflineManager private constructor(
    private val context: Context
) : DefaultLifecycleObserver {
    
    companion object {
        private const val TAG = "OfflineManager"
        
        @Volatile
        private var INSTANCE: OfflineManager? = null
        
        fun getInstance(context: Context): OfflineManager {
            return INSTANCE ?: synchronized(this) {
                val instance = OfflineManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Core services
    private val offlineDatabase by lazy { OfflineDatabase.getDatabase(context) }
    private val networkService by lazy { NetworkConnectivityService(context) }
    private val activityService by lazy { ActivityService() }
    private val syncService by lazy { 
        SyncService(context, activityService, offlineDatabase, networkService) 
    }
    private val activityServiceOffline by lazy {
        OfflineActivityService(context, activityService, offlineDatabase, networkService, syncService)
    }
    
    // State flows
    private val _offlineState = MutableStateFlow(OfflineState())
    val offlineState: StateFlow<OfflineState> = _offlineState.asStateFlow()
    
    private var isInitialized = false
    private var networkMonitoringJob: Job? = null
    private var syncJob: Job? = null
    
    init {
        // Register for app lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    
    /**
     * Initialize offline manager
     */
    fun initialize() {
        if (isInitialized) return
        
        Log.d(TAG, "Initializing OfflineManager")
        
        // Start network monitoring
        startNetworkMonitoring()
        
        // Schedule periodic sync
        SyncWorker.schedulePeriodicSync(context)
        
        isInitialized = true
        Log.i(TAG, "OfflineManager initialized")
    }
    
    /**
     * Get offline-first activity service
     */
    fun getOfflineActivityService(): OfflineActivityService = activityServiceOffline
    
    /**
     * Get network connectivity service
     */
    fun getNetworkConnectivityService(): NetworkConnectivityService = networkService
    
    /**
     * Manually trigger sync
     */
    fun triggerSync() {
        if (networkService.isNetworkAvailable()) {
            Log.d(TAG, "Manually triggering sync")
            SyncWorker.scheduleImmediateSync(context)
            
            // Also trigger immediate sync in background
            scope.launch {
                try {
                    _offlineState.value = _offlineState.value.copy(isSyncing = true)
                    val result = syncService.syncPendingOperations()
                    
                    when (result) {
                        is SyncResult.Success -> {
                            Log.i(TAG, "Manual sync completed: ${result.successCount} successful, ${result.failureCount} failed")
                            _offlineState.value = _offlineState.value.copy(
                                isSyncing = false,
                                lastSyncSuccess = result.failureCount == 0
                            )
                        }
                        else -> {
                            _offlineState.value = _offlineState.value.copy(
                                isSyncing = false,
                                lastSyncSuccess = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during manual sync", e)
                    _offlineState.value = _offlineState.value.copy(
                        isSyncing = false,
                        lastSyncSuccess = false
                    )
                }
            }
        } else {
            Log.d(TAG, "Cannot trigger sync - network unavailable")
        }
    }
    
    /**
     * Sync recent data from Firebase for offline access
     */
    fun syncFromFirebase(babyId: String) {
        scope.launch {
            syncService.syncFromFirebase(babyId)
        }
    }
    
    private fun startNetworkMonitoring() {
        networkMonitoringJob?.cancel()
        networkMonitoringJob = scope.launch {
            combine(
                networkService.networkStatusFlow(),
                offlineDatabase.syncOperationDao().getPendingOperationsCountFlow()
            ) { networkStatus, pendingCount ->
                _offlineState.value = _offlineState.value.copy(
                    isOnline = networkStatus.isAvailable,
                    pendingOperationsCount = pendingCount
                )
                
                // Trigger sync when network becomes available and there are pending operations
                if (networkStatus.isAvailable && pendingCount > 0 && !_offlineState.value.isSyncing) {
                    Log.d(TAG, "Network available with $pendingCount pending operations - triggering sync")
                    triggerSync()
                }
                
                networkStatus to pendingCount
            }.collect { (networkStatus, pendingCount) ->
                Log.d(TAG, "Network status: ${networkStatus.isAvailable}, Pending operations: $pendingCount")
            }
        }
    }
    
    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "App moved to foreground")
        // App came to foreground - check for pending sync
        if (networkService.isNetworkAvailable() && _offlineState.value.pendingOperationsCount > 0) {
            triggerSync()
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "App moved to background")
        // App moved to background - sync operations will continue via WorkManager
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up OfflineManager")
        networkMonitoringJob?.cancel()
        syncJob?.cancel()
        SyncWorker.cancelSync(context)
        scope.cancel()
    }
}

/**
 * Data class representing the current offline state
 */
data class OfflineState(
    val isOnline: Boolean = true,
    val pendingOperationsCount: Int = 0,
    val isSyncing: Boolean = false,
    val lastSyncSuccess: Boolean = true
) {
    val isOffline: Boolean get() = !isOnline
    val hasPendingOperations: Boolean get() = pendingOperationsCount > 0
}
