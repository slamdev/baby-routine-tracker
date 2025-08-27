# Baby Routine Tracker - Implementation Guide

## Project Overview
This is an Android Kotlin app using Jetpack Compose and Firebase for baby activity tracking with multi-user collaboration.

## ⚠️ Important: Offline Support Removed

**As of December 2024**: Offline support has been completely removed from this project per user requirements. The app now requires an active internet connection for all functionality.

### What was removed:
- Room database dependencies
- WorkManager for background sync
- OfflineManager and related offline services
- NetworkConnectivityService
- Local data caching and sync queue
- Offline status indicators in UI

### Current behavior:
- App requires internet connection for all operations
- All data operations use Firebase Firestore directly
- Real-time synchronization across devices (online only)
- Graceful error handling for network issues
- User-friendly error messages when connection is unavailable

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

### Modern Android App Design Pattern

The app follows modern Android design patterns with a clean, user-friendly interface:

#### Profile Icon in TopAppBar
- **UPDATED UX**: Removed the large welcome card from the main dashboard
- **NEW PATTERN**: Added compact profile icon in top-right corner of the app bar
- **Implementation**: 
  - `ProfileIcon` composable displays user's Google profile picture or initials
  - Dropdown menu on tap shows user info and sign-out option
  - Follows Material Design 3 guidelines
  - Saves valuable screen space for main content

#### Dashboard Layout Pattern - **IMPLEMENTED** ✅ **ENHANCED FOR LANDSCAPE**
- **NEW UX**: 4 separate activity cards in a responsive 2x2 grid layout
- **CARDS**: Sleep (😴), Breast Feeding (🤱), Bottle Feeding (🍼), Poop (💩)
- **RESPONSIVE**: Cards adapt size based on screen dimensions - bigger on larger phones, more compact on smaller phones
- **LANDSCAPE OPTIMIZATION**: Enhanced responsive logic for better landscape support:
  - **Smart Column Logic**: In landscape mode, prefers 4 columns if space allows, otherwise 2; in portrait, sticks to 2 columns for optimal usability
  - **Adaptive Card Heights**: In landscape mode, allows slightly smaller cards (80% of minimum height) for better fit
  - **Orientation Detection**: Uses `maxWidth > maxHeight` to detect landscape mode and apply appropriate layout adjustments
- **NO SCROLLING**: All 4 cards fit within the visible screen area without vertical scrolling
- **IMPLEMENTATION**: 
  - Use LazyVerticalGrid with dynamic column calculation based on orientation
  - Enhanced responsive logic: `val isLandscape = maxWidth > maxHeight`
  - Smart column selection: landscape prefers 4 or 2 columns, portrait uses 2 columns max
  - Dynamic card height calculation with landscape-specific minimum height reduction
  - Consistent visual design across all 4 cards
  - Real-time synchronization for all activity types

#### Separated Feeding Cards Pattern - **IMPLEMENTED** ✅ **ENHANCED**
- **CHANGE**: Split previous combined FeedingTrackingCard into two separate cards
- **UX IMPROVEMENTS**: 
  - **Simplified Design**: Removed button text, keeping only descriptive icons for cleaner appearance
  - **Improved Layout**: Action buttons positioned immediately after card titles for better accessibility
  - **Enhanced Touch Targets**: Square buttons (aspectRatio 1:1) for larger, easier-to-click interface
  - **Consistent Styling**: All 4 cards now follow identical design patterns:
    - Header font size: 18sp (consistent across all cards)
    - Padding: 16.dp (consistent across all cards) 
    - Button width: 80% using fillMaxWidth(0.8f) (consistent across all cards)
    - **Button aspect ratio: 1:1 (square)** for better usability
    - **Button icon size: 32.dp** proportional to larger button size for better visibility
    - **Action button placement**: Right after title for immediate access
    - **Icon-only buttons**: Clean, minimalist design without text
- **BREAST FEEDING CARD**: 
  - Action button (Start/Stop) positioned after title
  - Displays ongoing breast feeding timer (if active) using `formatElapsedTime(uiState.currentElapsedTime)`
  - Last breast feeding session details (duration in minutes)
  - Editable start time for ongoing sessions
- **BOTTLE FEEDING CARD**:
  - Action button (Log Bottle) positioned after title  
  - Quick log bottle feeding with amount and optional notes
  - Last bottle feeding details with notes display (amount in ml)
  - Instant activity logging (no ongoing timer)
  - Dedicated BottleFeedingDialog for input
- **DIAPER TRACKING CARD**:
  - Action button (Log Poop) positioned after title
  - Last diaper change details with notes display
  - Instant activity logging (no ongoing timer)
  - Dedicated PoopDialog for input

#### Time Ago Display Feature
All activity cards now display user-friendly "time ago" information showing when the last activity happened:

- **Format Examples**: "Happened 23m ago", "Happened 1h 23m ago", "Happened 1d ago", "Happened 2w ago"
- **Smart Timestamp Selection**: 
  - For completed activities (sleep, breast feeding): uses end time
  - For ongoing activities: uses start time  
  - For instant activities (bottle, diaper): uses logged time
- **Real-time Updates**: Time ago information updates automatically when new activities are logged
- **Implementation**: Uses `TimeUtils.formatTimeAgo()` utility function with `TimeUtils.getRelevantTimestamp()`

#### Card Design Patterns
- **Consistent Height**: All cards use dynamic height calculation for responsive design
- **Modernized Styling**: 
  - Header with emoji (18sp, FontWeight.Bold, primary color)
  - Padding: 16.dp across all cards for consistent spacing
  - **Action button positioned right after card title** for immediate access
  - **Modern action buttons** using sophisticated indigo colors (`MaterialTheme.colorScheme.extended.actionButton`)
  - **Square buttons** using aspectRatio(1f) for better touch targets and easier clicking
  - **Icon-only buttons** with larger icons (32.dp) proportional to button size for better visibility
  - **Enhanced elevation** (6.dp) for better visual hierarchy and depth
  - Action button (80% width using fillMaxWidth(0.8f), square aspect ratio, 32.dp icon size)
  - Last activity summary (14sp, clickable with edit icon) positioned after action button
- **Visual Hierarchy**: Clean, focused layout with modern action buttons that harmonize with both light and dark themes
- **Theme Integration**: All components use theme-aware colors for seamless light/dark mode transitions
- **Error Handling**: CompactErrorDisplay for user feedback
- **Loading States**: CircularProgressIndicator with proportional sizing (24.dp) for larger buttons

#### Profile Icon Implementation Example
```kotlin
@Composable
private fun ProfileIcon(
    user: FirebaseUser?,
    onSignOut: () -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    Box {
        // Circular profile image/avatar (40dp)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { showDropdownMenu = true }
        ) {
            // Google profile image or initials fallback
        }
        
        // Dropdown menu with user info and sign out
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false }
        ) {
            // User display name and email
            // Sign out option with icon
        }
    }
}
```

### Activity Color System - **ENHANCED** ✅

The app uses a sophisticated color system that provides distinct, pleasing colors for each activity type:

#### **Refined Color Palette**
- **Sleep (😴)**: Modern blue/indigo tones - calming and sophisticated
  - Light: Soft mint-blue backgrounds (`#E8F4FD`)
  - Dark: Refined indigo (`#2C3E7A`) instead of harsh blues
  - Better contrast and less eye strain
  
- **Feeding (🤱🍼)**: Warm coral/peach tones - nurturing and modern
  - Light: Soft peachy backgrounds (`#FFF4F0`)
  - Dark: Warmer brown-orange (`#8D4E2A`) instead of jarring bright orange
  - More harmonious with overall app palette
  
- **Diaper (💩)**: Fresh mint/teal tones - clean and modern
  - Light: Mint-tinged backgrounds (`#F0FAF5`)
  - Dark: Sophisticated forest green (`#2E5D4A`)
  - More refined than pure green

#### **Modern Design Principles**
- **Harmonious Palette**: All colors work together cohesively in both themes
- **Reduced Eye Strain**: Softer, more muted colors in dark mode
- **Better Accessibility**: Improved contrast ratios for text readability
- **Material Design 3**: Follows modern Android design language

### Theme System Implementation - **IMPLEMENTED** ✅

The app features a comprehensive theme system that automatically switches between light and dark modes based on the system settings:

#### Theme Architecture
- **Automatic Theme Detection**: Uses `isSystemInDarkTheme()` to detect system preference
- **Real-time Updates**: Theme changes instantly when system setting is modified
- **Dynamic Colors**: Supports Android 12+ dynamic color theming when available
- **Extended Colors**: Custom success colors for states not covered by Material 3

#### Theme Files Structure
```kotlin
// Theme.kt - Main theme composable
@Composable
fun BabyroutinetrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)

// Color.kt - Theme color definitions
val Purple80 = Color(0xFFD0BCFF)  // Dark theme colors
val Purple40 = Color(0xFF6650a4)  // Light theme colors
val SuccessLight = Color(0xFF4CAF50)  // Custom success colors
val SuccessDark = Color(0xFF81C784)

// ExtendedColors.kt - Additional theme colors
@Stable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color
)
```

#### Using Theme Colors
Instead of hardcoded colors, always use theme-aware colors:

```kotlin
// ✅ DO: Use theme colors
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.extended.successContainer
    )
) {
    Text(
        text = "Success message",
        color = MaterialTheme.colorScheme.extended.onSuccessContainer
    )
}

// ❌ DON'T: Hardcode colors
Card(
    colors = CardDefaults.cardColors(
        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)  // ❌ Wrong
    )
) {
    Text(
        text = "Success message", 
        color = Color(0xFF2E7D32)  // ❌ Wrong
    )
}
```

#### Extended Colors Usage
Access extended colors via the `extended` property:
```kotlin
// Success states
MaterialTheme.colorScheme.extended.success
MaterialTheme.colorScheme.extended.onSuccess
MaterialTheme.colorScheme.extended.successContainer
MaterialTheme.colorScheme.extended.onSuccessContainer

// Action button colors (modern, theme-aware)
MaterialTheme.colorScheme.extended.actionButton        // Modern indigo for buttons
MaterialTheme.colorScheme.extended.onActionButton      // White/light color for button content
MaterialTheme.colorScheme.extended.actionButtonPressed // Pressed state color
MaterialTheme.colorScheme.extended.surfaceElevated     // Elevated surface for better contrast
```

#### Color Scheme Completeness
Both light and dark themes include full color definitions:
- Primary, secondary, tertiary colors
- Background, surface colors with appropriate contrast
- Error colors with proper readability
- Extended success colors for positive feedback states
- **Modern action button colors**: Sophisticated indigo colors that work harmoniously in both themes
- **Elevated surfaces**: Better visual hierarchy with appropriate contrast

### Landscape Mode & Responsive Design - **IMPLEMENTED** ✅

The app provides comprehensive landscape mode support with intelligent responsive design:

#### AndroidManifest.xml Configuration
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:theme="@style/Theme.Babyroutinetracker"
    android:resizeableActivity="true"
    android:supportsPictureInPicture="false">
```

#### Responsive Layout Patterns
- **Orientation Detection**: `val isLandscape = maxWidth > maxHeight` using BoxWithConstraints
- **Smart Column Logic**: Dashboard adapts columns based on orientation and available space
- **Adaptive Spacing**: UI elements use different padding and spacing in landscape vs portrait
- **Content Optimization**: Text sizes and card dimensions adjust for better landscape utilization

#### Dashboard Landscape Enhancements
```kotlin
// Enhanced column calculation for landscape
val columns = if (isLandscape) {
    // In landscape, prefer 4 columns if space allows, otherwise 2
    when {
        rawColumns >= 4 -> 4
        rawColumns >= 2 -> 2
        else -> 1
    }
} else {
    // In portrait, stick to 2 columns for optimal usability
    rawColumns.coerceAtMost(2)
}

// Adaptive card heights with landscape-specific minimum
val effectiveMinHeight = if (isLandscape) (minCardHeight * 0.8f) else minCardHeight
```

#### TopAppBar Landscape Optimization
- **Responsive Title Layout**: Uses BoxWithConstraints for orientation-aware spacing
- **Adaptive Navigation Chips**: Increased spacing (16.dp vs 8.dp) in landscape mode
- **Text Overflow Handling**: Baby name truncates with ellipsis in narrow spaces
- **Weight Distribution**: Navigation chips use weight(1f, fill = false) for flexible sizing

#### Activity History Landscape Layout
- **Wide Screen Detection**: `val isWideScreen = maxWidth > 600.dp` for tablet/landscape optimization
- **Horizontal Information Layout**: Activity details spread horizontally instead of vertically
- **Responsive Weight Distribution**: 
  - Activity type: 25% width
  - Time information: 40% width  
  - Details: 25% width
  - Edit button: 10% width
- **Compact Text Display**: Shortened labels ("🤱 Breast" vs "🤱 Breast Feeding") for landscape

#### Coming Soon Screens Landscape Support
- **Adaptive Card Sizing**: Cards constrained to 80% width in landscape for better proportions
- **Responsive Typography**: Smaller text sizes in landscape (20.sp vs 24.sp for titles)
- **Flexible Padding**: Reduced padding (24.dp vs 32.dp) in landscape mode

#### Implementation Pattern
```kotlin
@Composable
fun ResponsiveScreen(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val padding = if (isLandscape) 32.dp else 24.dp
        
        // Layout adaptation based on orientation
        if (isLandscape) {
            // Landscape-specific layout
        } else {
            // Portrait-specific layout
        }
    }
}
```

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

### Time Utilities

#### TimeUtils Class
Centralized utility for user-friendly time formatting:

```kotlin
object TimeUtils {
    /**
     * Format time elapsed since the given date in a user-friendly way
     * Examples: "Happened 23m ago", "Happened 1h 23m ago", "Happened 1d ago"
     */
    fun formatTimeAgo(pastDate: Date): String {
        val now = Date()
        val diffMillis = abs(now.time - pastDate.time)
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        
        return when {
            diffMinutes < 1 -> "Happened now"
            diffMinutes < 60 -> "Happened ${diffMinutes}m ago"
            diffHours < 24 -> {
                val hours = diffHours
                val remainingMinutes = diffMinutes % 60
                if (remainingMinutes == 0L) {
                    "Happened ${hours}h ago"
                } else {
                    "Happened ${hours}h ${remainingMinutes}m ago"
                }
            }
            diffDays < 7 -> "Happened ${diffDays}d ago"
            else -> {
                val weeks = diffDays / 7
                "Happened ${weeks}w ago"
            }
        }
    }
    
    /**
     * Get the most relevant timestamp for an activity to calculate "time ago"
     * For completed activities, use end time. For ongoing activities, use start time.
     */
    fun getRelevantTimestamp(startTime: Date, endTime: Date?): Date {
        return endTime ?: startTime
    }
}
```

#### Usage in Activity Cards
```kotlin
// In any tracking card
val timeAgo = TimeUtils.formatTimeAgo(
    TimeUtils.getRelevantTimestamp(
        activity.startTime.toDate(),
        activity.endTime?.toDate()
    )
)

Text(
    text = timeAgo,
    fontSize = 12.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
)
```

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

### Real-time Synchronization Issues ✅ **FIXED**
- **Problem**: Partner devices not seeing edits/deletions in activity history until app restart
- **Root Cause**: Using one-time data fetch (`getRecentActivities()`) instead of real-time listeners
- **Solution**: Replace with `getRecentActivitiesFlow()` using Firebase `addSnapshotListener`
- **Implementation**: Use `OptionalUiState<List<Activity>>` flow pattern for real-time list updates
- **Result**: Changes now appear on all devices within seconds without manual refresh

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

## 🍼 Baby Profile Management Implementation

### Enhanced Baby Model with Due Date & Default Bottle Amount Support

The Baby model has been enhanced to support both regular babies and premature babies with sophisticated age calculations, plus default feeding preferences:

```kotlin
data class Baby(
    val id: String = "",
    val name: String = "",
    val birthDate: Timestamp = Timestamp.now(),
    val dueDate: Timestamp? = null,  // Optional due date for premature babies
    val parentIds: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val defaultBottleAmount: Double? = null  // NEW: Default bottle feeding amount in ml
) {
    @Exclude
    fun getRealAge(): AgeInfo {
        return calculateAge(birthDate.toDate())
    }
    
    @Exclude
    fun getAdjustedAge(): AgeInfo? {
        return dueDate?.let { calculateAge(it.toDate()) }
    }
    
    @Exclude
    fun wasBornEarly(): Boolean {
        return dueDate != null && birthDate.seconds < dueDate.seconds
    }
}
```

### Age Calculation System

#### AgeInfo Data Class
```kotlin
data class AgeInfo(
    val years: Int,
    val months: Int,
    val weeks: Int,
    val days: Int,
    val totalDays: Long,
    val totalWeeks: Long,
    val totalMonths: Long
)
```

#### Age Display Components

**BabyAgeDisplay**: Full age information with both real and corrected ages
```kotlin
@Composable
fun BabyAgeDisplay(baby: Baby, modifier: Modifier = Modifier) {
    val realAge = baby.getRealAge()
    val adjustedAge = baby.getAdjustedAge()
    
    Column(modifier = modifier) {
        Text("Real age: ${baby.getFormattedRealAge()}")
        if (adjustedAge != null) {
            Text("Corrected age: ${baby.getFormattedAdjustedAge()}")
        }
    }
}
```

**CompactBabyAgeDisplay**: Space-efficient version for cards
**DashboardAgeDisplay**: Optimized for dashboard titles

### Service Layer Enhancements

#### Fixed Firebase Document ID Mapping
**CRITICAL FIX**: Baby objects now properly include their Firebase document IDs:

```kotlin
// Before (missing ID):
document.toObject<Baby>()

// After (with proper ID):
document.toObject<Baby>()?.copy(id = document.id)
```

#### Baby Profile Service Methods
```kotlin
// Create baby with due date support
suspend fun createBabyProfile(name: String, birthDate: Timestamp, dueDate: Timestamp? = null): Result<Baby>

// Update baby with due date and default bottle amount support  
suspend fun updateBabyProfile(
    babyId: String, 
    name: String, 
    birthDate: Timestamp, 
    dueDate: Timestamp? = null,
    defaultBottleAmount: Double? = null
): Result<Baby>

// Load baby for editing
suspend fun getBabyProfile(babyId: String): Baby?

// Real-time babies flow with proper ID mapping
fun getUserBabiesFlow(): Flow<Result<List<Baby>>>
```

### Default Bottle Amount Feature

The app now supports setting a default bottle feeding amount at the baby profile level:

#### Implementation Details
- **Storage**: Default amount stored as `defaultBottleAmount: Double?` in Baby model
- **Access**: Configurable from EditBabyProfileScreen with dedicated "Feeding Preferences" section
- **Usage**: BottleFeedingDialog pre-populates with default amount when available
- **Override**: Users can easily change the amount during individual logging sessions

#### UI Integration
```kotlin
// Baby profile editing includes default amount
OutlinedTextField(
    value = uiState.defaultBottleAmount,
    onValueChange = { viewModel.updateDefaultBottleAmount(it) },
    label = { Text("Default Bottle Amount (ml)") },
    placeholder = { Text("e.g., 120") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
)

// Bottle feeding dialog uses default amount
BottleFeedingDialog(
    defaultAmount = baby?.defaultBottleAmount,
    onConfirm = { amount, notes -> /* ... */ }
)
```

#### ViewModel Support
```kotlin
// UI state includes default bottle amount
data class InvitationUiState(
    // ... other fields
    val defaultBottleAmount: String = ""
)

// Update methods
fun updateDefaultBottleAmount(amount: String)
fun updateBabyProfile(babyId: String, name: String, birthDate: Timestamp, dueDate: Timestamp? = null, defaultBottleAmount: Double? = null)
```

### Navigation & UI Implementation

#### Enhanced Edit Navigation
The edit baby functionality was fixed to handle proper data loading:

```kotlin
// Navigation route
composable(
    "edit_baby/{babyId}",
    arguments = listOf(navArgument("babyId") { type = NavType.StringType })
) { backStackEntry ->
    val babyId = backStackEntry.arguments?.getString("babyId") ?: ""
    EditBabyProfileScreen(
        babyId = babyId,
        onNavigateBack = { navController.popBackStack() },
        onUpdateSuccess = { navController.navigate("dashboard") },
        viewModel = invitationViewModel
    )
}
```

#### ViewModel Baby Loading
```kotlin
fun loadBabyForEditing(babyId: String) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // First check if baby is already in the list
        val existingBaby = _uiState.value.babies.find { it.id == babyId }
        if (existingBaby != null) {
            startEditingBaby(existingBaby)
        } else {
            // Load directly from service if not in cache
            val baby = invitationService.getBabyProfile(babyId)
            if (baby != null) {
                startEditingBaby(baby)
            }
        }
        
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
}
```

### Profile Icon Integration

The ProfileIcon component includes baby profile management options:

```kotlin
@Composable
fun ProfileIcon(
    user: FirebaseUser?,
    babies: List<Baby>,
    selectedBaby: Baby?,
    onNavigateToEditBaby: (Baby) -> Unit,
    // ... other callbacks
) {
    // Profile dropdown includes:
    // - "Create Baby Profile" 
    // - "Join Profile"
    // - "Edit [Baby Name]" (when baby selected)
    // - "Invite Partner" (when babies exist)
    // - "Sign Out"
}
```

### Age Display Integration

#### Dashboard Title Integration
```kotlin
TopAppBar(
    title = {
        Text(
            text = selectedBaby?.let { "${it.name} (${it.getFormattedRealAge()})" } 
                ?: "Baby Routine Tracker"
        )
    }
)
```

#### Profile Screen Age Display
```kotlin
BabyAgeDisplay(
    baby = baby,
    modifier = Modifier.padding(16.dp)
)
```

### Key Implementation Patterns

1. **Firebase ID Mapping**: Always use `.copy(id = document.id)` when converting Firestore documents
2. **Age Calculations**: Use `@Exclude` for computed age properties to prevent Firebase storage
3. **Due Date Support**: Optional field with proper null handling throughout the UI
4. **Navigation Safety**: Load baby data in edit screens rather than relying on cached lists
5. **Real-time Sync**: All profile updates sync automatically across devices

## 🧪 Testing Guidelines

### Always Test These Scenarios
1. **Multi-user sync** - Changes appear on both devices
2. **~~Offline behavior~~ - ~~App works without internet~~** ❌ **REMOVED** - App now requires internet connection
3. **Permission edge cases** - New users joining profiles
4. **UI responsiveness** - Portrait/landscape/split-screen modes
5. **Error scenarios** - Database connection issues, permission errors
6. **Index creation** - Test app behavior before and after Firestore indexes exist

### Error Scenario Testing

// Test network errors (UPDATED - no offline support)
1. Turn off wifi/data while using the app
2. Verify error messages appear with "check your internet connection" 
3. Verify app gracefully handles connection loss (no data loss, clear error messages)
4. Verify functionality returns when connection is restored (no manual refresh needed)

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

## 🎉 Success Message Implementation

### Enhanced UX for Activity Confirmations

The app uses a sophisticated success message system that provides prominent feedback when activities are logged successfully, addressing the user story: *"As a parent, I want confirmation when important actions are completed (like successfully logging an activity) so I know it worked."*

#### Key UX Improvements

**Before**: Success messages appeared as small overlay cards at the bottom of activity cards, competing with main content and often partially visible.

**After**: Success messages replace the main card content temporarily, providing clear, prominent confirmation that actions were successful.

#### Success Message Implementation Pattern

##### 1. SuccessContentDisplay Component
```kotlin
@Composable
fun SuccessContentDisplay(
    successMessage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Success",
            tint = MaterialTheme.colorScheme.extended.success,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Success message
        Text(
            text = successMessage,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.extended.success,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

##### 2. ActivityCard Content State Priority
```kotlin
// In ActivityCard component - success messages take priority over normal content
when {
    state.successMessage != null -> {
        // Show success message as main content (replaces normal content)
        SuccessContentDisplay(
            successMessage = state.successMessage,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
    state.contentError != null -> {
        CompactErrorDisplay(/* ... */)
    }
    else -> {
        // Normal card content
        ActivityCardContentDisplay(config = config, content = content)
    }
}
```

##### 3. ViewModel Success State Management
```kotlin
// In activity ViewModels (e.g., DiaperTrackingViewModel, FeedingTrackingViewModel)
result.fold(
    onSuccess = { activity ->
        Log.i(TAG, "Activity logged successfully: ${activity.id}")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            successMessage = "Activity logged successfully!"
        )
    },
    onFailure = { exception ->
        // Error handling...
    }
)

// Auto-clear success messages after 3 seconds
LaunchedEffect(successMessage) {
    kotlinx.coroutines.delay(3000)
    onDismissSuccess()
}

// Clear success when starting new operations
fun logActivity() {
    _uiState.value = _uiState.value.copy(
        isLoading = true, 
        errorMessage = null, 
        successMessage = null  // Clear previous success
    )
}
```

#### Success Message Usage Guidelines

**Use success messages for:**
- ✅ Instant activities (bottle feeding, diaper changes) - immediate completion confirmation
- ✅ Actions where users need confirmation that data was saved
- ✅ Activities where timing/forgetfulness could be an issue

**Don't use success messages for:**
- ❌ Ending ongoing activities (sleep, breast feeding) - state change is sufficient feedback
- ❌ Navigation actions or other UI state changes
- ❌ Real-time updates from other users

#### Visual Design Principles

- **Prominence**: Success messages replace main content temporarily, ensuring visibility
- **Clarity**: Large check icon and clear text with success theme colors
- **Non-intrusive**: Auto-dismiss after 3 seconds to avoid blocking user workflow
- **Consistency**: Same visual treatment across all activity types
- **Theme Integration**: Uses `MaterialTheme.colorScheme.extended.success` colors

#### Implementation Checklist for New Activities

When adding success messages to new activity types:

- [ ] Add `successMessage: String?` to ViewModel UI state
- [ ] Set success message in `onSuccess` callback when logging activity
- [ ] Clear success message when starting new activity logging
- [ ] Add `clearSuccessMessage()` function to ViewModel
- [ ] Pass success message to ActivityCard component
- [ ] Use `onDismissSuccess` callback in UI component
- [ ] Test auto-dismissal timing (3 seconds)
- [ ] Verify success message doesn't interfere with error handling

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

## ��️ Enhanced Activity History Screen

### Real-time Synchronization Fix ✅ **IMPLEMENTED**

**Issue Fixed**: Activity history screen was using one-time data loading instead of real-time listeners, causing partner devices to not see edits/deletions until app restart.

**Solution**: Replaced `getRecentActivities()` one-time fetch with `getRecentActivitiesFlow()` real-time listener.

#### Key Changes Made:
- **New Service Method**: Added `getRecentActivitiesFlow(babyId: String, limit: Int): Flow<OptionalUiState<List<Activity>>>` to ActivityService
- **Real-time Listener**: Uses Firebase Firestore `addSnapshotListener` for automatic updates
- **ViewModel Update**: ActivityHistoryViewModel now uses `setupRealtimeActivitiesListener()` instead of `loadActivities()`
- **Removed Manual Reloads**: Update/delete methods no longer call `loadActivities()` since real-time listener handles updates
- **State Management**: Proper loading/success/error states handled by the flow

```kotlin
// New real-time flow in ActivityService
fun getRecentActivitiesFlow(babyId: String, limit: Int = 50): Flow<OptionalUiState<List<Activity>>> = callbackFlow {
    // Emit loading state initially
    trySend(OptionalUiState.Loading)
    
    val listenerRegistration = firestore.collection(BABIES_COLLECTION)
        .document(babyId)
        .collection(ACTIVITIES_SUBCOLLECTION)
        .orderBy("startTime", Query.Direction.DESCENDING)
        .limit(limit.toLong())
        .addSnapshotListener { snapshot, error ->
            // Handle real-time updates for all activity changes
        }
    
    awaitClose { listenerRegistration.remove() }
}

// Updated ViewModel pattern
class ActivityHistoryViewModel : ViewModel() {
    private fun setupRealtimeActivitiesListener() {
        viewModelScope.launch {
            activityService.getRecentActivitiesFlow(babyId, limit = 50)
                .collect { activitiesState ->
                    when (activitiesState) {
                        is OptionalUiState.Success -> {
                            // Automatically update UI with latest data
                        }
                        // Handle loading, empty, error states
                    }
                }
        }
    }
}
```

**Result**: Partner devices now see activity edits and deletions in real-time without requiring app restart.

### Overview
The activity history screen has been significantly enhanced with deletion, advanced date/time editing, and filtering capabilities.

### Key Features

#### 1. Activity Deletion with Confirmation
- **Delete Button**: Each activity item displays a trash icon (red) alongside the edit button
- **Confirmation Dialog**: Prevents accidental deletion with clear warning message
- **Real-time Sync**: Deleted activities disappear from all connected devices immediately
- **Permanent Deletion**: Activities are permanently removed from Firebase Firestore

```kotlin
// Service Layer - Delete functionality
suspend fun deleteActivity(activityId: String, babyId: String): Result<Unit> {
    // Verifies user authentication and baby access
    // Permanently deletes from Firebase
    // Real-time listeners update all connected devices
}
```

#### 2. Enhanced Date & Time Editing
- **Date Pickers**: Material 3 DatePickerDialog for selecting different dates
- **Combined Date/Time**: Preserves time when changing dates and vice versa  
- **Responsive Layout**: Date and time pickers arranged in responsive rows
- **Activity Type Specific**:
  - **Instant Activities** (bottle, diaper): Single date/time picker pair
  - **Duration Activities** (sleep, breast feeding): Separate start/end date/time pickers

```kotlin
// Enhanced EditActivityDialog supports both date and time editing
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityDialog(
    activity: Activity,
    onDismiss: () -> Unit,
    onSave: (Activity, Date, Date?, String?) -> Unit
) {
    // Date picker state management
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startTime.time
    )
    
    // Combined date/time logic preserves existing time components
    // Responsive UI adapts to activity type (instant vs duration)
}
```

#### 3. Activity Type Filtering
- **Filter Chips**: Horizontal scrollable filter controls at top of screen
- **Filter Options**: "All", "😴 Sleep", "🍼 Feeding", "💩 Diaper"
- **Real-time Filtering**: Updates list immediately without refresh
- **Empty State**: Shows helpful message when no activities match filter
- **State Management**: Filter resets when navigating away from history screen

```kotlin
// ViewModel State includes filtering
data class ActivityHistoryUiState(
    val activities: List<Activity> = emptyList(),
    val filteredActivities: List<Activity> = emptyList(),
    val selectedActivityType: ActivityType? = null, // null = show all
    val errorMessage: String? = null
)

// Filtering Logic
private fun filterActivities(activities: List<Activity>, activityType: ActivityType?): List<Activity> {
    return if (activityType == null) {
        activities
    } else {
        activities.filter { it.type == activityType }
    }
}
```

### UI Implementation Details

#### Responsive Action Buttons Layout
- **Wide Screen** (landscape): Edit and delete buttons arranged horizontally
- **Narrow Screen** (portrait): Edit and delete buttons stacked vertically
- **Color Coding**: Edit button uses primary color, delete button uses error color
- **Touch Targets**: Adequate spacing and sizing for easy interaction

#### Confirmation Dialog Design
```kotlin
AlertDialog(
    title = { Text("Delete Activity") },
    text = { Text("Are you sure you want to delete this activity? This action cannot be undone.") },
    confirmButton = {
        Button(
            onClick = { onDeleteActivity() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Delete")
        }
    },
    dismissButton = {
        TextButton(onClick = { showDeleteConfirmation = false }) {
            Text("Cancel")
        }
    }
)
```

#### Filter Chips Implementation
```kotlin
@Composable
private fun ActivityTypeFilter(
    selectedType: ActivityType?,
    onTypeSelected: (ActivityType?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text("All") }
            )
        }
        // Individual activity type chips...
    }
}
```

### Technical Implementation

#### Service Layer Updates
- **Delete Method**: Added `deleteActivity()` with proper authentication and access control
- **Enhanced Query**: `getRecentActivities()` loads up to 50 recent activities for history view
- **Real-time Sync**: All changes (edits, deletions) sync automatically across devices

#### ViewModel Enhancements  
- **Filter State**: Manages selected activity type and filtered list
- **Delete Operations**: Handles deletion with proper error handling and loading states
- **State Consistency**: Ensures UI always reflects current data after any changes

#### UI Component Updates
- **Enhanced ActivityHistoryItem**: Supports both edit and delete actions
- **Advanced EditActivityDialog**: Includes both date and time pickers
- **Filter Controls**: Activity type filter chips with visual feedback

### Error Handling

#### Delete Operations
- **Authentication Check**: Verifies user has permission to delete
- **Network Errors**: Graceful handling with retry capability  
- **Confirmation Required**: Prevents accidental deletion
- **Success Feedback**: Clear indication when deletion succeeds

#### Enhanced Editing
- **Date/Time Validation**: Ensures start time ≤ end time across date boundaries
- **Data Persistence**: All changes saved to Firebase with proper error handling
- **Real-time Updates**: Changes appear on all devices without manual refresh

## �🔄 Future Updates

When adding new entities or features:
1. Follow the established patterns in this guide
2. Update this guide with new patterns discovered
3. Maintain consistency with existing code structure
4. Always consider multi-user collaboration implications
5. **Implement proper error handling** using the three-layer system above
6. **Test error scenarios** as part of development workflow

---

**Remember**: This guide is living documentation. Update it as the project evolves!
