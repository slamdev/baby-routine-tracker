# Baby Routine Tracker - Implementation Guide

## Project Overview
This is an Android Kotlin app using Jetpack Compose and Firebase for baby activity tracking with multi-user collaboration.

## 🔧 Development Commands

### Compile Project
```bash
./gradlew :app:compileDebugKotlin
```
Always run this command after making code changes to verify compilation success.

## 🗄️ Data Models & Firebase Integration

### Firebase Field Exclusion Rule
**CRITICAL**: When creating data models that will be stored in Firebase, use `@Exclude` annotations for computed properties.

#### ✅ **DO**: Exclude computed/derived fields
```kotlin
import com.google.firebase.firestore.Exclude

data class MyEntity(
    val id: String = "",
    val name: String = "",
    val createdAt: Timestamp = Timestamp.now()
) {
    @Exclude
    fun isValid(): Boolean {
        return name.isNotBlank()
    }
    
    @Exclude
    fun getDisplayName(): String {
        return name.uppercase()
    }
    
    @Exclude
    val computedProperty: String
        get() = "Computed: $name"
}
```

#### ❌ **DON'T**: Store computed properties in Firebase
```kotlin
// This will create unwanted fields in Firebase:
data class BadExample(
    val id: String = "",
    val name: String = "",
    val isValid: Boolean = false,  // ❌ This will be stored
    val displayName: String = ""   // ❌ This will be stored
)
```

### Firestore Collections Structure
```
users/{userId}
babies/{babyId}
babies/{babyId}/activities/{activityId}
babies/{babyId}/sleepPlans/{planId}
invitations/{invitationId}
```

## 🔐 Security Rules Pattern

Always apply security rules that follow this pattern:
- Users can only access their own documents
- Baby data accessible only to parents (users in `parentIds` array)
- Invitation system allows temporary access for joining

## 🎨 UI Guidelines

### Button Consistency
When creating buttons that should have equal sizes:
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    OutlinedButton(
        onClick = { /* action */ },
        modifier = Modifier
            .weight(1f)
            .height(40.dp)  // Fixed height for consistency
    ) {
        Text(
            text = "Button Text",
            maxLines = 1,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
```

### Navigation Pattern
```kotlin
// In MainActivity.kt navigation setup
composable("screen_name") {
    MyScreen(
        onNavigateBack = { navController.popBackStack() },
        onSuccess = { 
            navController.navigate("destination") {
                popUpTo("current") { inclusive = true }
            }
        }
    )
}
```

## 🔧 Service Layer Pattern

### Repository/Service Structure
```kotlin
import android.util.Log

class MyService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "MyService"
        private const val COLLECTION_NAME = "myCollection"
    }
    
    suspend fun createItem(item: MyItem): Result<MyItem> {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(item.id)
                .set(item)
                .await()
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create item: ${item.id}", e)
            Result.failure(e)
        }
    }
}
```

### Permission-Safe Operations
For operations that might face permission issues during multi-user scenarios:

```kotlin
// Use FieldValue operations to avoid reading documents first
batch.update(
    documentRef,
    mapOf(
        "arrayField" to FieldValue.arrayUnion(newValue),
        "updatedAt" to Timestamp.now()
    )
)
```

## 🐛 Error Handling & Logging

### CRITICAL: Always Log Exceptions

**NEVER** silently swallow exceptions - this makes debugging impossible.

### UI State Management for Error Handling

The app uses a comprehensive error handling system with standardized UI states:

#### UiState Pattern
```kotlin
// Core state types
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable, val message: String) : UiState<Nothing>()
}

// For optional data (might be empty)
sealed class OptionalUiState<out T> {
    object Loading : OptionalUiState<Nothing>()
    object Empty : OptionalUiState<Nothing>()
    data class Success<T>(val data: T) : OptionalUiState<T>()
    data class Error(val exception: Throwable, val message: String) : OptionalUiState<Nothing>()
}
```

### Service Layer Error Handling

#### ✅ **DO**: Real-time Flow error handling
```kotlin
fun getActivityFlow(babyId: String, type: ActivityType): Flow<OptionalUiState<Activity>> = callbackFlow {
    // Emit loading state initially
    trySend(OptionalUiState.Loading)
    
    val listenerRegistration = firestore.collection("activities")
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to activities", error)
                
                // Provide user-friendly error messages based on error type
                val userMessage = when {
                    error.message?.contains("PERMISSION_DENIED") == true -> 
                        "You don't have permission to view this data"
                    error.message?.contains("UNAVAILABLE") == true -> 
                        "Unable to connect to server. Check your internet connection"
                    error.message?.contains("FAILED_PRECONDITION") == true ->
                        "Database is being set up. Please try again in a few minutes"
                    else -> "Unable to load activity data"
                }
                
                trySend(OptionalUiState.Error(error, userMessage))
                return@addSnapshotListener
            }
            
            // Process successful data
            if (snapshot != null) {
                try {
                    val activity = snapshot.documents.firstOrNull()?.toObject<Activity>()
                    if (activity != null) {
                        trySend(OptionalUiState.Success(activity))
                    } else {
                        trySend(OptionalUiState.Empty)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing snapshot", e)
                    trySend(OptionalUiState.Error(e, "Failed to process activity data"))
                }
            }
        }
    
    awaitClose { listenerRegistration.remove() }
}
```

### ViewModel Error State Management

#### Comprehensive UI State
```kotlin
data class TrackingUiState(
    // Action-level loading and errors
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // Data-specific loading and errors  
    val isLoadingOngoingActivity: Boolean = false,
    val ongoingActivityError: String? = null,
    val ongoingActivity: Activity? = null,
    
    val isLoadingLastActivity: Boolean = false,
    val lastActivityError: String? = null,
    val lastActivity: Activity? = null
)
```

#### ViewModel Flow Handling
```kotlin
class TrackingViewModel : ViewModel() {
    fun initialize(babyId: String) {
        viewModelScope.launch {
            activityService.getActivityFlow(babyId, ActivityType.SLEEP)
                .collect { activityState ->
                    when (activityState) {
                        is OptionalUiState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingActivity = true,
                                ongoingActivityError = null
                            )
                        }
                        is OptionalUiState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingActivity = false,
                                ongoingActivity = activityState.data,
                                ongoingActivityError = null
                            )
                        }
                        is OptionalUiState.Empty -> {
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingActivity = false,
                                ongoingActivity = null,
                                ongoingActivityError = null
                            )
                        }
                        is OptionalUiState.Error -> {
                            Log.e(TAG, "Error getting activity", activityState.exception)
                            _uiState.value = _uiState.value.copy(
                                isLoadingOngoingActivity = false,
                                ongoingActivity = null,
                                ongoingActivityError = activityState.message
                            )
                        }
                    }
                }
        }
    }
    
    // Error clearing functions
    fun clearOngoingActivityError() {
        _uiState.value = _uiState.value.copy(ongoingActivityError = null)
    }
}
```

### UI Error Display Components

#### Reusable Error Components
```kotlin
// For prominent error displays
@Composable
fun ErrorStateComponent(
    errorMessage: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) { /* Implementation in ErrorStateComponent.kt */ }

// For compact inline errors
@Composable  
fun CompactErrorDisplay(
    errorMessage: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) { /* Implementation in ErrorStateComponent.kt */ }
```

#### UI Error Handling Pattern
```kotlin
@Composable
fun TrackingCard(viewModel: TrackingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle different states for each data source
    when {
        uiState.activityError != null -> {
            CompactErrorDisplay(
                errorMessage = uiState.activityError,
                onDismiss = { viewModel.clearActivityError() }
            )
        }
        uiState.isLoadingActivity -> {
            Row {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading...")
            }
        }
        uiState.activity != null -> {
            // Display data
        }
        else -> {
            Text("No data available")
        }
    }
}
```

### Smart Cast Prevention

When using delegated properties in `when` expressions, create local variables to avoid smart cast issues:

#### ❌ **DON'T**: Direct property access in when
```kotlin
when {
    uiState.error != null -> {
        // ❌ Smart cast to String is impossible
        CompactErrorDisplay(errorMessage = uiState.error, onDismiss = {})
    }
}
```

#### ✅ **DO**: Local variable extraction
```kotlin
val errorMessage = uiState.error
when {
    errorMessage != null -> {
        // ✅ Smart cast works with local variable
        CompactErrorDisplay(errorMessage = errorMessage, onDismiss = {})
    }
}
```

#### ❌ **DON'T**: Silent exception handling
```kotlin
// BAD - Exception is completely lost
suspend fun getBabyProfile(babyId: String): Baby? {
    return try {
        val document = firestore.collection("babies")
            .document(babyId)
            .get()
            .await()
        document.toObject<Baby>()
    } catch (e: Exception) {
        null  // ❌ Error is lost forever!
    }
}

// BAD - No context about what failed
suspend fun createBaby(baby: Baby): Result<Baby> {
    return try {
        firestore.collection("babies").add(baby).await()
        Result.success(baby)
    } catch (e: Exception) {
        Result.failure(e)  // ❌ No logging, hard to debug
    }
}
```

#### ✅ **DO**: Proper exception logging
```kotlin
import android.util.Log

class BabyService {
    companion object {
        private const val TAG = "BabyService"
    }
    
    suspend fun getBabyProfile(babyId: String): Baby? {
        return try {
            val document = firestore.collection("babies")
                .document(babyId)
                .get()
                .await()
            document.toObject<Baby>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get baby profile: $babyId", e)  // ✅ Log with context
            null
        }
    }
    
    suspend fun createBaby(baby: Baby): Result<Baby> {
        return try {
            firestore.collection("babies").add(baby).await()
            Log.d(TAG, "Successfully created baby: ${baby.name}")  // ✅ Success logging
            Result.success(baby)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create baby: ${baby.name}", e)  // ✅ Error with context
            Result.failure(e)
        }
    }
}
```

### Logging Levels Guidelines

Use appropriate log levels based on severity:

```kotlin
// DEBUG - Development information
Log.d(TAG, "Starting baby profile creation for user: ${userId}")

// INFO - General information  
Log.i(TAG, "Baby profile created successfully: ${baby.name}")

// WARN - Recoverable issues, business logic problems
Log.w(TAG, "Invitation code expired, user can request new one: ${code}")

// ERROR - Exceptions, critical failures
Log.e(TAG, "Failed to save baby profile to database", exception)

// VERBOSE - Detailed tracing (use sparingly)
Log.v(TAG, "Processing invitation step 3 of 5: ${invitationId}")
```

### Exception Handling Patterns

#### For Service Methods Returning Result<T>
```kotlin
suspend fun performDatabaseOperation(): Result<Data> {
    return try {
        // Database operation
        val result = database.performOperation()
        Log.d(TAG, "Operation completed successfully")
        Result.success(result)
    } catch (e: SpecificException) {
        Log.w(TAG, "Known issue occurred, retrying...", e)
        // Handle specific case
        Result.failure(e)
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error in database operation", e)
        Result.failure(e)
    }
}
```

#### For Service Methods Returning Nullable
```kotlin
suspend fun findOptionalData(id: String): Data? {
    return try {
        database.findById(id)
    } catch (e: NotFoundException) {
        Log.i(TAG, "Data not found: $id")  // ✅ Info level - this might be expected
        null
    } catch (e: Exception) {
        Log.e(TAG, "Error searching for data: $id", e)  // ✅ Unexpected errors are logged
        null
    }
}
```

#### For ViewModel Exception Handling
```kotlin
class MyViewModel : ViewModel() {
    companion object {
        private const val TAG = "MyViewModel"
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val data = service.getData()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    data = data
                )
                Log.d(TAG, "Data loaded successfully: ${data.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load data in ViewModel", e)  // ✅ Always log
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data: ${e.message}"  // User-friendly message
                )
            }
        }
    }
}
```

### Debugging Best Practices

1. **Always include context** in log messages (user ID, document ID, operation type)
2. **Use consistent TAG naming** (usually class name)
3. **Log both successes and failures** for critical operations
4. **Include the exception object** to preserve stack traces
5. **Use appropriate log levels** to filter during debugging
6. **Consider user privacy** - don't log sensitive data in production

## 📱 ViewModel Pattern

### State Management
```kotlin
data class MyUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val data: List<MyItem> = emptyList()
)

class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
```

## 🚨 Common Issues & Solutions

### Firestore Index Errors
- **Problem**: `FAILED_PRECONDITION: The query requires an index`
- **Root Cause**: Queries with `whereEqualTo() + whereNotEqualTo() + orderBy()` need composite indexes
- **Solution**: 
  1. Click the URL in the error message to auto-create the index
  2. Or manually create in Firebase Console: Firestore Database → Indexes
  3. Required index pattern: `type (Ascending) + endTime (Ascending) + __name__ (Ascending)`
- **Wait Time**: Index creation takes 5-15 minutes
- **Documentation**: See `FIRESTORE_INDEX_SETUP.md` for detailed instructions

### Firebase Permission Errors
- **Problem**: `PERMISSION_DENIED` during multi-user operations
- **Solution**: Use `FieldValue.arrayUnion()` instead of reading + updating
- **Security**: Ensure update rules allow user to add themselves to arrays

### Smart Cast Compilation Errors
- **Problem**: "Smart cast to 'String' is impossible, because 'property' is a delegated property"
- **Solution**: Extract to local variable before `when` expression
- **Example**: `val error = uiState.error; when { error != null -> { /* use error */ } }`

### Flow Error Handling
- **Problem**: Errors in Firebase listeners not visible to users
- **Solution**: Use `OptionalUiState` pattern to emit proper error states
- **Pattern**: Always emit Loading → Success/Empty/Error states in flows

### Icon Import Errors
- **Problem**: Material icons not found (e.g., `ContentCopy`, `CalendarToday`, `ErrorOutline`)
- **Solution**: Use available icons like `Icons.Default.Warning` or text-only buttons
- **Check**: Look at existing codebase for working icon imports

### Button Size Inconsistencies
- **Problem**: Buttons with different text lengths appear different sizes
- **Solution**: Use fixed `.height()` and `maxLines = 1` for text

## 📚 Dependencies

### Key Libraries Used
- Jetpack Compose (UI)
- Firebase Auth & Firestore (Backend)
- Navigation Compose (Navigation)
- Coil (Image Loading)
- Material3 (Design System)

### Adding New Dependencies
1. Update `gradle/libs.versions.toml`
2. Add to `app/build.gradle`
3. Run compile command to verify

## 🧪 Testing Guidelines

### Always Test These Scenarios
1. **Multi-user sync** - Changes appear on both devices
2. **Offline behavior** - App works without internet
3. **Permission edge cases** - New users joining profiles
4. **UI responsiveness** - Portrait/landscape/split-screen modes
5. **Error scenarios** - Database connection issues, permission errors
6. **Index creation** - Test app behavior before and after Firestore indexes exist

### Error Scenario Testing

// Test network errors
1. Turn off wifi/data while using the app
2. Verify error messages appear with "check your internet connection"
3. Verify retry functionality works when connection restored

// Test permission errors  
1. Temporarily modify Firestore security rules to deny access
2. Verify "permission denied" messages appear
3. Restore rules and verify functionality returns

// Test index errors
1. Delete Firestore indexes temporarily
2. Verify "database is being set up" messages appear
3. Recreate indexes and verify data loads normally

// Test UI error states
1. Each tracking card should show loading indicators
2. Error messages should be dismissible
3. Errors should not crash the app

### Test Command Sequence
```bash
# Compile and check for errors
./gradlew :app:compileDebugKotlin

# Run full build to catch additional issues
./gradlew :app:assembleDebug

# If issues, check lints
# (Use linting tools in IDE)

```

## 📝 Documentation Standards

### Code Comments
- Document complex business logic
- Explain Firebase security rule requirements
- Note permission-sensitive operations

### Git Commit Messages
```
feat: add partner invitation system
fix: resolve Firebase permission denied error
ui: make buttons consistent size
security: update Firestore rules for invitations
```

## 🎯 Development Workflow

1. **Read this guide** before starting new features
2. **Plan data models** - identify what should/shouldn't be stored
3. **Add @Exclude annotations** for computed properties
4. **Implement service layer** with proper error handling
5. **Create ViewModel** with proper state management
6. **Build UI** following consistency guidelines
7. **Test compilation** with provided commands
8. **Test functionality** on multiple devices
9. **Update security rules** if needed
10. **Document new patterns** in this guide

## 🔄 Error Handling Architecture (Current Implementation)

### Three-Layer Error Handling System

Our app implements a comprehensive three-layer error handling system:

#### Layer 1: Service Layer (Data Source)
- **ActivityService** emits `OptionalUiState<T>` for real-time flows
- **Error Categorization**: Network, permission, database setup, parsing errors
- **User-Friendly Messages**: Convert technical errors to understandable text
- **Proper Logging**: All exceptions logged with context [[memory:6766862]]

#### Layer 2: ViewModel Layer (State Management)  
- **Separate Error States**: Different errors for ongoing vs. last activity data
- **Loading States**: Individual loading indicators for each data source
- **Error Clearing**: Functions to dismiss specific error messages
- **State Consistency**: Ensure UI state always reflects current data/error status

#### Layer 3: UI Layer (User Experience)
- **ErrorStateComponent**: Full-screen error displays with retry/dismiss actions
- **CompactErrorDisplay**: Inline error messages for smaller UI areas
- **Loading Indicators**: Consistent loading states across all tracking cards
- **Smart Cast Safety**: Local variables to prevent Kotlin smart cast issues

### Error Handling Checklist for New Features

When implementing new features, ensure:

- [ ] Service methods return `OptionalUiState<T>` for flows or `Result<T>` for operations
- [ ] All exceptions are logged with context and appropriate severity level
- [ ] User-friendly error messages for common error types (network, permissions, setup)
- [ ] ViewModel handles all possible states (Loading, Success, Empty, Error)
- [ ] UI displays loading indicators while data is being fetched
- [ ] Error messages are dismissible and don't break the user flow
- [ ] Local variables used in `when` expressions to prevent smart cast issues

### Common Error Types & Messages

```kotlin
// Database setup issues (Firestore indexes)
"Database is being set up. Please try again in a few minutes"

// Network connectivity
"Unable to connect to server. Please check your internet connection"

// Permission issues
"You don't have permission to view this baby's activities"

// Authentication issues  
"Please sign in to view activities"

// Data processing errors
"Failed to process [activity type] data"
```

## ✏️ Activity Editing Implementation

### Overview
The app supports comprehensive activity editing capabilities for both ongoing and completed activities across all activity types (Sleep, Feeding, Diaper).

### Edit Access Points

#### 1. Ongoing Activities (Dashboard)
- **Sleep**: Tap "Started at [time]" to edit start time
- **Breast Feeding**: Tap "Started at [time]" to edit start time
- **Bottle Feeding**: Not applicable (completed immediately)
- **Diaper**: Not applicable (completed immediately)

#### 2. Last Activities (Dashboard)  
- **All Activity Types**: Tap on the last activity summary to open edit dialog
- Shows recent completed activity with edit icon
- Supports time and notes editing where applicable

#### 3. Activity History (Dedicated Screen)
- Access via "View Activity History" button on dashboard
- Lists all recent activities (50 most recent)
- Edit button available for all completed activities
- Full editing capabilities (time + notes)

### Editing Capabilities by Activity Type

| Activity Type | Time Editing | Notes | Access Points |
|---------------|------------|--------|---------------|
| Sleep | ✅ Start & End Times | ❌ | Ongoing (dashboard), Last (dashboard), History |
| Breast Feeding | ✅ Start & End Times | ❌ | Ongoing (dashboard), Last (dashboard), History |
| Bottle Feeding | ✅ Single Timestamp* | ✅ | Last (dashboard), History |
| Diaper | ✅ Single Timestamp* | ✅ | Last (dashboard), History |

*Instant activities (bottle feeding, diaper) show single time picker since they occur at one moment in time.

### Service Layer Methods

```kotlin
// Update start time for ongoing activities
suspend fun updateActivityStartTime(activityId: String, babyId: String, newStartTime: Timestamp): Result<Activity>

// Update both start and end times for completed duration activities (sleep, breast feeding)
suspend fun updateActivityTimes(activityId: String, babyId: String, newStartTime: Timestamp, newEndTime: Timestamp): Result<Activity>

// Update single timestamp for instant activities (bottle feeding, diaper)
suspend fun updateInstantActivityTime(activityId: String, babyId: String, newTime: Timestamp): Result<Activity>

// Update notes for activities that support notes
suspend fun updateActivityNotes(activityId: String, babyId: String, newNotes: String): Result<Activity>

// Get recent activities for history view
suspend fun getRecentActivities(babyId: String, limit: Int = 20): Result<List<Activity>>
```

### UI Components

#### TimePickerDialog
- Material 3 time picker with 24-hour format
- Shows current time and allows selection
- Validates input and provides save/cancel options

#### EditActivityDialog  
- Comprehensive dialog for editing completed activities
- Dynamically shows relevant fields based on activity type
- **Instant Activities**: Single time picker for bottle feeding and diaper changes
- **Duration Activities**: Separate start/end time pickers for sleep and breast feeding
- Time validation (start ≤ end time to allow very short activities)
- Notes editing for supported activity types
- Separate save callbacks for different update types

### Activity Type Classification

#### Duration Activities (Have Start & End Times)
- **Sleep**: Can last from minutes to hours
- **Breast Feeding**: Tracked with start/stop timer

*UI Behavior*: Show separate start time and end time pickers when editing

#### Instant Activities (Single Timestamp)  
- **Bottle Feeding**: Recorded at the moment it occurs
- **Diaper Changes**: Recorded at the moment they occur

*UI Behavior*: Show single time picker since both startTime and endTime are identical

```kotlin
// Helper method in Activity model
@Exclude
fun isInstantActivity(): Boolean {
    return (type == ActivityType.DIAPER) || 
           (type == ActivityType.FEEDING && feedingType == "bottle")
}
```

### ViewModel Pattern for Editing

Each tracking ViewModel includes methods for updating both ongoing and completed activities:

```kotlin
// For ongoing activities (start time only)
fun updateStartTime(newStartTime: Date)

// For completed duration activities (sleep, breast feeding)
fun updateCompletedActivityTimes(activity: Activity, newStartTime: Date, newEndTime: Date)
fun updateCompletedActivityNotes(activity: Activity, newNotes: String)

// For instant activities (bottle feeding, diaper) 
fun updateInstantActivityTime(activity: Activity, newTime: Date)
```

### Real-time Synchronization

All activity updates trigger real-time synchronization:
- Changes appear on all connected devices within seconds
- Firestore listeners automatically update UI with latest data
- No manual refresh required

### Error Handling for Editing

- **Validation Errors**: Start time cannot be after end time (≤ allowed for short activities)
- **Permission Errors**: User must have access to baby profile
- **Network Errors**: Graceful handling with retry capability
- **Data Consistency**: Transaction-based updates ensure data integrity
- **Activity Type Detection**: Automatic detection of instant vs. duration activities for appropriate UI

## 🔄 Future Updates

When adding new entities or features:
1. Follow the established patterns in this guide
2. Update this guide with new patterns discovered
3. Maintain consistency with existing code structure
4. Always consider multi-user collaboration implications
5. **Implement proper error handling** using the three-layer system above
6. **Test error scenarios** as part of development workflow

---

**Remember**: This guide is living documentation. Update it as the project evolves!
