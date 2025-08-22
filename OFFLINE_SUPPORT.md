# Offline Support Implementation

This document explains the offline support implementation for the Baby Routine Tracker app.

## Overview

The app now supports offline-first activity tracking with automatic synchronization when the network connection is restored. Users can continue logging activities (sleep, feeding, diaper changes) even without internet connection.

## User Stories Implemented

### ✅ **User Story 1**: Continue logging activities offline
> **As a parent with unreliable internet, I want to continue logging activities even when offline so I don't miss recording important events.**

**Implementation:**
- All activity logging functions work offline by storing data locally in Room database
- Activities are queued for sync when network connection is restored
- Users receive visual feedback about offline mode and pending sync operations

### ✅ **User Story 2**: Clear offline indicator
> **As a user, I want to see a clear indicator when the app is offline so I know my data isn't syncing yet.**

**Implementation:**
- Prominent offline status indicator at the top of the screen when network is unavailable
- Shows number of pending operations waiting to sync
- Progress indicator during sync operations
- Success feedback when sync completes

## Key Components

### 1. Offline Database (Room)
- **LocalActivity**: Offline storage for activities with sync status
- **SyncOperation**: Queue for pending sync operations
- **OfflineDatabase**: Room database with automatic migrations

### 2. Network Monitoring
- **NetworkConnectivityService**: Real-time network status monitoring
- **NetworkStatus**: Sealed class representing online/offline states
- Automatic sync triggering when connection is restored

### 3. Sync Management
- **SyncService**: Handles uploading offline data to Firebase
- **SyncWorker**: Background sync using WorkManager
- **OfflineManager**: Coordinates all offline functionality
- Exponential backoff retry mechanism for failed operations

### 4. Offline-First Services
- **OfflineActivityService**: Offline-first activity operations
- Falls back to local storage when network is unavailable
- Automatic sync when connection is restored
- Preserves all existing functionality with offline support

### 5. UI Components
- **OfflineStatusIndicator**: Shows offline status and pending operations count
- **SyncProgressIndicator**: Shows sync progress with count/total
- **CompactOfflineIndicator**: Smaller indicator for space-constrained areas
- **SyncSuccessIndicator**: Temporary success message after sync completion

## User Experience Flow

### Normal Operation (Online)
1. User logs activity
2. Data is saved locally and synced to Firebase immediately
3. Real-time updates appear on all connected devices

### Offline Operation
1. User logs activity while offline
2. **Offline indicator appears** showing "You're offline" with pending count
3. Data is saved locally with sync status = false
4. Operation is queued for later sync
5. User sees success message: "Activity started (offline - will sync when connected)"

### Connection Restored
1. **Sync indicator appears** showing "Syncing data..." with progress
2. Background sync uploads all pending operations
3. **Success indicator appears** showing "Synchronized X activities"
4. All devices receive updated data via Firebase real-time listeners

## Technical Architecture

### Data Flow
```
User Action → OfflineActivityService → Local Database → Sync Queue
                                    ↓
                              Firebase (when online)
                                    ↓
                            Real-time listeners update UI
```

### Error Handling
- **Network errors**: Operations queued for retry with exponential backoff
- **Permission errors**: User-friendly error messages with context
- **Data validation**: Local validation before queuing for sync
- **Conflict resolution**: Firestore transactions ensure data consistency

### Performance Optimizations
- **Lazy initialization**: Services created only when needed
- **Background sync**: WorkManager handles sync without blocking UI
- **Data pruning**: Old synced data automatically cleaned up
- **Efficient queries**: Room database optimized for offline operations

## Integration Guide for Developers

### Using OfflineActivityService in ViewModels

```kotlin
class MyTrackingViewModel(context: Context) : ViewModel() {
    private val offlineManager = OfflineManager.getInstance(context)
    private val offlineActivityService = offlineManager.getOfflineActivityService()
    
    fun startActivity(babyId: String, type: ActivityType) {
        viewModelScope.launch {
            val result = offlineActivityService.startActivity(babyId, type)
            // Handle result - works same way online or offline
        }
    }
}
```

### Adding Offline Indicators to Screens

```kotlin
@Composable
fun MyScreen() {
    val context = LocalContext.current
    val offlineManager = remember { OfflineManager.getInstance(context) }
    val offlineState by offlineManager.offlineState.collectAsState()
    
    Column {
        OfflineStatusIndicator(
            isOffline = offlineState.isOffline,
            pendingOperationsCount = offlineState.pendingOperationsCount
        )
        
        SyncProgressIndicator(
            isVisible = offlineState.isSyncing
        )
        
        // Your screen content
    }
}
```

## Testing Offline Functionality

### Scenario 1: Basic Offline Operation
1. Turn off device network (airplane mode or disable WiFi/data)
2. Open app - should show offline indicator
3. Log activities (sleep, feeding, diaper) - should work normally
4. Check indicator shows pending operations count
5. Restore network connection
6. Verify sync indicator appears and data syncs to Firebase

### Scenario 2: Multi-Device Sync
1. Device A goes offline, logs activities
2. Device B (online) logs different activities  
3. Device A comes back online
4. Verify both devices show all activities from both sources

### Scenario 3: Partial Network Failures
1. Simulate intermittent network (toggle airplane mode rapidly)
2. Log activities during network instability
3. Verify retry mechanism works with exponential backoff
4. Check all activities eventually sync when network stabilizes

## Future Enhancements

### Phase 1 (Completed)
- ✅ Basic offline activity logging
- ✅ Network status monitoring
- ✅ Offline indicators and UI feedback
- ✅ Background sync with WorkManager

### Phase 2 (Potential Future Work)
- 🔄 Conflict resolution for simultaneous edits from multiple devices
- 🔄 Selective sync (only sync specific activity types)
- 🔄 Sync progress per operation type
- 🔄 Manual sync controls (force sync, pause sync)
- 🔄 Offline data export/backup functionality

## Dependencies Added

```gradle
// Room Database for offline support
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// WorkManager for background sync
implementation "androidx.work:work-runtime-ktx:2.9.1"

// Lifecycle Process for app lifecycle monitoring
implementation "androidx.lifecycle:lifecycle-process:2.6.1"

// JSON serialization for sync operations
implementation "com.google.code.gson:gson:2.10.1"
```

## Permissions Added

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

This implementation provides a robust offline experience while maintaining the existing real-time collaboration features when online.
