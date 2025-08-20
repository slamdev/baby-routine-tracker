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
    
    suspend fun updateBaby(baby: Baby): Result<Baby> {
        return try {
            firestore.collection("babies")
                .document(baby.id)
                .set(baby)
                .await()
            Result.success(baby)
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.w(TAG, "Permission denied updating baby: ${baby.id}", e)  // ✅ Warning level
                }
                else -> {
                    Log.e(TAG, "Firestore error updating baby: ${baby.id}", e)  // ✅ Error level
                }
            }
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error updating baby: ${baby.id}", e)  // ✅ Catch-all with logging
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

### Firebase Permission Errors
- **Problem**: `PERMISSION_DENIED` during multi-user operations
- **Solution**: Use `FieldValue.arrayUnion()` instead of reading + updating
- **Security**: Ensure update rules allow user to add themselves to arrays

### Icon Import Errors
- **Problem**: Material icons not found (e.g., `ContentCopy`, `CalendarToday`)
- **Solution**: Use available icons or text-only buttons
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

### Test Command Sequence
```bash
# Compile
./gradlew :app:compileDebugKotlin

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

## 🔄 Future Updates

When adding new entities or features:
1. Follow the established patterns in this guide
2. Update this guide with new patterns discovered
3. Maintain consistency with existing code structure
4. Always consider multi-user collaboration implications

---

**Remember**: This guide is living documentation. Update it as the project evolves!
