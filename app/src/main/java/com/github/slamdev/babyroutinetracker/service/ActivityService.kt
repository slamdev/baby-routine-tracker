package com.github.slamdev.babyroutinetracker.service

import android.util.Log
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ActivityService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "ActivityService"
        private const val BABIES_COLLECTION = "babies"
        private const val ACTIVITIES_SUBCOLLECTION = "activities"
    }

    /**
     * Start a new activity (sleep, feeding, etc.)
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

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to start activity - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            // Check if there's already an ongoing activity of this type
            val ongoingActivity = getOngoingActivity(babyId, type)
            if (ongoingActivity != null) {
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
                endTime = null, // This is an ongoing activity
                notes = notes,
                loggedBy = currentUser.uid,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)
                .set(activity)
                .await()

            Log.i(TAG, "Activity started successfully: ${type.displayName} for baby: $babyId")
            Result.success(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start activity: ${type.displayName} for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * End an ongoing activity
     */
    suspend fun endActivity(activityId: String, babyId: String): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to end activity - user not authenticated", error)
                return Result.failure(error)
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to end activity - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            val activityRef = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)

            val activityDoc = activityRef.get().await()
            val activity = activityDoc.toObject<Activity>()
            if (activity == null) {
                val error = Exception("Activity not found")
                Log.e(TAG, "Failed to end activity - activity not found: $activityId", error)
                return Result.failure(error)
            }

            if (activity.endTime != null) {
                val error = Exception("Activity is already ended")
                Log.w(TAG, "Attempted to end already ended activity: $activityId", error)
                return Result.failure(error)
            }

            val updatedActivity = activity.copy(
                endTime = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            activityRef.set(updatedActivity).await()

            Log.i(TAG, "Activity ended successfully: ${activity.type.displayName} for baby: $babyId")
            Result.success(updatedActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end activity: $activityId for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Get ongoing activity of a specific type for a baby
     */
    suspend fun getOngoingActivity(babyId: String, type: ActivityType): Activity? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Failed to get ongoing activity - user not authenticated")
                return null
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                Log.e(TAG, "Failed to get ongoing activity - no access to baby: $babyId")
                return null
            }

            val querySnapshot = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .whereEqualTo("type", type.name)
                .whereEqualTo("endTime", null)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.toObject<Activity>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ongoing activity for baby: $babyId", e)
            null
        }
    }

    /**
     * Update an existing activity
     */
    suspend fun updateActivity(babyId: String, activity: Activity): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to update activity - user not authenticated", error)
                return Result.failure(error)
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to update activity - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            val activityRef = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activity.id)

            val updatedActivity = activity.copy(updatedAt = Timestamp.now())
            activityRef.set(updatedActivity).await()

            Log.i(TAG, "Activity updated successfully: ${activity.id} for baby: $babyId")
            Result.success(updatedActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update activity: ${activity.id} for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Get the most recent COMPLETED activity of a specific type for a baby
     */
    suspend fun getLastActivity(babyId: String, type: ActivityType): Activity? {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Failed to get last activity - user not authenticated")
                return null
            }

            val querySnapshot = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .whereEqualTo("type", type.name)
                .whereNotEqualTo("endTime", null)  // Only get completed activities
                .orderBy("endTime", Query.Direction.DESCENDING)  // Order by end time for completed activities
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.toObject<Activity>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last activity: ${type.displayName} for baby: $babyId", e)
            null
        }
    }

    /**
     * Get real-time updates for ongoing activities of a specific type for a baby
     */
    fun getOngoingActivityFlow(babyId: String, type: ActivityType): Flow<OptionalUiState<Activity>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val error = Exception("User not authenticated")
            Log.e(TAG, "Failed to get ongoing activity flow - user not authenticated", error)
            trySend(OptionalUiState.Error(error, "Please sign in to view activities"))
            close()
            return@callbackFlow
        }

        Log.d(TAG, "Setting up real-time listener for ongoing ${type.displayName} activity: $babyId")
        
        // Emit loading state initially
        trySend(OptionalUiState.Loading)
        
        val listenerRegistration = firestore.collection(BABIES_COLLECTION)
            .document(babyId)
            .collection(ACTIVITIES_SUBCOLLECTION)
            .whereEqualTo("type", type.name)
            .whereEqualTo("endTime", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to ongoing ${type.displayName} activity", error)
                    val userMessage = when {
                        error.message?.contains("PERMISSION_DENIED") == true -> 
                            "You don't have permission to view this baby's activities"
                        error.message?.contains("UNAVAILABLE") == true -> 
                            "Unable to connect to server. Please check your internet connection"
                        error.message?.contains("index") == true ->
                            "Database configuration issue. Please contact support"
                        else -> "Unable to load ${type.displayName.lowercase()} activities"
                    }
                    trySend(OptionalUiState.Error(error, userMessage))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val ongoingActivities = snapshot.documents.mapNotNull { document ->
                            try {
                                document.toObject<Activity>()
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to parse activity document: ${document.id}", e)
                                null
                            }
                        }
                        
                        if (ongoingActivities.size > 1) {
                            Log.w(TAG, "Found multiple ongoing ${type.displayName} activities for baby: $babyId")
                        }
                        
                        val activity = ongoingActivities.firstOrNull()
                        Log.d(TAG, "Real-time update for ongoing ${type.displayName}: ${if (activity != null) "active" else "none"}")
                        
                        if (activity != null) {
                            trySend(OptionalUiState.Success(activity))
                        } else {
                            trySend(OptionalUiState.Empty)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing ongoing ${type.displayName} activity snapshot", e)
                        trySend(OptionalUiState.Error(e, "Failed to process ${type.displayName.lowercase()} data"))
                    }
                } else {
                    Log.d(TAG, "Received null snapshot for ongoing ${type.displayName} activity")
                    trySend(OptionalUiState.Empty)
                }
            }

        awaitClose { 
            Log.d(TAG, "Removing real-time listener for ongoing ${type.displayName} activity")
            listenerRegistration.remove() 
        }
    }

    /**
     * Get real-time updates for the most recent COMPLETED activity of a specific type for a baby
     */
    fun getLastActivityFlow(babyId: String, type: ActivityType): Flow<OptionalUiState<Activity>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val error = Exception("User not authenticated")
            Log.e(TAG, "Failed to get last activity flow - user not authenticated", error)
            trySend(OptionalUiState.Error(error, "Please sign in to view activities"))
            close()
            return@callbackFlow
        }

        Log.d(TAG, "Setting up real-time listener for last completed ${type.displayName} activity: $babyId")
        
        // Emit loading state initially
        trySend(OptionalUiState.Loading)
        
        val listenerRegistration = firestore.collection(BABIES_COLLECTION)
            .document(babyId)
            .collection(ACTIVITIES_SUBCOLLECTION)
            .whereEqualTo("type", type.name)
            .whereNotEqualTo("endTime", null)  // Only get completed activities
            .orderBy("endTime", Query.Direction.DESCENDING)  // Order by end time for completed activities
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to last ${type.displayName} activity", error)
                    val userMessage = when {
                        error.message?.contains("PERMISSION_DENIED") == true -> 
                            "You don't have permission to view this baby's activities"
                        error.message?.contains("UNAVAILABLE") == true -> 
                            "Unable to connect to server. Please check your internet connection"
                        error.message?.contains("index") == true || error.message?.contains("FAILED_PRECONDITION") == true ->
                            "Database is being set up. Please try again in a few minutes"
                        else -> "Unable to load last ${type.displayName.lowercase()} activity"
                    }
                    trySend(OptionalUiState.Error(error, userMessage))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val activity = snapshot.documents.firstOrNull()?.let { document ->
                            try {
                                document.toObject<Activity>()
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to parse activity document: ${document.id}", e)
                                null
                            }
                        }
                        
                        Log.d(TAG, "Real-time update for last completed ${type.displayName}: ${if (activity != null) "found" else "none"}")
                        
                        if (activity != null) {
                            trySend(OptionalUiState.Success(activity))
                        } else {
                            trySend(OptionalUiState.Empty)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing last ${type.displayName} activity snapshot", e)
                        trySend(OptionalUiState.Error(e, "Failed to process ${type.displayName.lowercase()} data"))
                    }
                } else {
                    Log.d(TAG, "Received null snapshot for last ${type.displayName} activity")
                    trySend(OptionalUiState.Empty)
                }
            }

        awaitClose { 
            Log.d(TAG, "Removing real-time listener for last ${type.displayName} activity")
            listenerRegistration.remove() 
        }
    }

    /**
     * Start a breast milk feeding session (ongoing activity)
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

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to start breast milk feeding - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            // Check if there's already an ongoing feeding activity
            val ongoingActivity = getOngoingActivity(babyId, ActivityType.FEEDING)
            if (ongoingActivity != null) {
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
                endTime = null, // This is an ongoing activity
                notes = notes,
                loggedBy = currentUser.uid,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                feedingType = "breast_milk",
                amount = 0.0
            )

            firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)
                .set(activity)
                .await()

            Log.i(TAG, "Breast milk feeding started successfully for baby: $babyId")
            Result.success(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start breast milk feeding for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Log a completed feeding activity
     */
    suspend fun logCompletedFeeding(
        babyId: String,
        feedingType: String,
        duration: Int,
        amount: Double,
        notes: String = ""
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to log feeding - user not authenticated", error)
                return Result.failure(error)
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to log feeding - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            val activityId = UUID.randomUUID().toString()
            val now = Timestamp.now()
            
            // For breast milk feeding, calculate start time based on duration
            // For bottle feeding, use current time as both start and end
            val startTime = if (feedingType == "breast_milk" && duration > 0) {
                Timestamp(now.seconds - (duration * 60), now.nanoseconds)
            } else {
                now
            }
            
            val activity = Activity(
                id = activityId,
                type = ActivityType.FEEDING,
                babyId = babyId,
                startTime = startTime,
                endTime = now, // Feeding is logged as completed
                notes = notes,
                loggedBy = currentUser.uid,
                createdAt = now,
                updatedAt = now,
                feedingType = feedingType,
                amount = amount
            )

            firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)
                .set(activity)
                .await()

            Log.i(TAG, "Feeding activity logged successfully: $feedingType for baby: $babyId")
            Result.success(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log feeding activity: $feedingType for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Log a poop diaper change
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

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to log poop - no access to baby: $babyId", error)
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
                diaperType = "poop" // Updated for "poops only" requirement
            )

            firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)
                .set(activity)
                .await()

            Log.i(TAG, "Poop activity logged successfully for baby: $babyId")
            Result.success(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log poop activity for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Update the start time of an activity (for in-progress activities)
     */
    suspend fun updateActivityStartTime(
        activityId: String,
        babyId: String,
        newStartTime: Timestamp
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to update activity start time - user not authenticated", error)
                return Result.failure(error)
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to update activity start time - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            val activityRef = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)

            val activityDoc = activityRef.get().await()
            val activity = activityDoc.toObject<Activity>()
            if (activity == null) {
                val error = Exception("Activity not found")
                Log.e(TAG, "Failed to update activity start time - activity not found: $activityId", error)
                return Result.failure(error)
            }

            // Validate that new start time is not after end time (if activity is completed)
            if (activity.endTime != null && newStartTime.seconds > activity.endTime.seconds) {
                val error = Exception("Start time cannot be after end time")
                Log.w(TAG, "Invalid start time update - start time after end time", error)
                return Result.failure(error)
            }

            val updatedActivity = activity.copy(
                startTime = newStartTime,
                updatedAt = Timestamp.now()
            )

            activityRef.set(updatedActivity).await()

            Log.i(TAG, "Activity start time updated successfully: ${activity.type.displayName} for baby: $babyId")
            Result.success(updatedActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update activity start time: $activityId for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Update both start and end times of a completed activity
     */
    suspend fun updateActivityTimes(
        activityId: String,
        babyId: String,
        newStartTime: Timestamp,
        newEndTime: Timestamp
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to update activity times - user not authenticated", error)
                return Result.failure(error)
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to update activity times - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            // Validate times (allow start time to be equal to end time for short activities)
            if (newStartTime.seconds > newEndTime.seconds) {
                val error = Exception("Start time cannot be after end time")
                Log.w(TAG, "Invalid time update - start time after end time", error)
                return Result.failure(error)
            }

            val activityRef = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)

            val activityDoc = activityRef.get().await()
            val activity = activityDoc.toObject<Activity>()
            if (activity == null) {
                val error = Exception("Activity not found")
                Log.e(TAG, "Failed to update activity times - activity not found: $activityId", error)
                return Result.failure(error)
            }

            val updatedActivity = activity.copy(
                startTime = newStartTime,
                endTime = newEndTime,
                updatedAt = Timestamp.now()
            )

            activityRef.set(updatedActivity).await()

            Log.i(TAG, "Activity times updated successfully: ${activity.type.displayName} for baby: $babyId")
            Result.success(updatedActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update activity times: $activityId for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Update the timestamp of an instant activity (bottle feeding, diaper)
     * For these activities, startTime and endTime are the same
     */
    suspend fun updateInstantActivityTime(
        activityId: String,
        babyId: String,
        newTime: Timestamp
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to update instant activity time - user not authenticated", error)
                return Result.failure(error)
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to update instant activity time - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            val activityRef = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)

            val activityDoc = activityRef.get().await()
            val activity = activityDoc.toObject<Activity>()
            if (activity == null) {
                val error = Exception("Activity not found")
                Log.e(TAG, "Failed to update instant activity time - activity not found: $activityId", error)
                return Result.failure(error)
            }

            // For instant activities (bottle feeding, diaper), both start and end time should be the same
            val updatedActivity = activity.copy(
                startTime = newTime,
                endTime = newTime,
                updatedAt = Timestamp.now()
            )

            activityRef.set(updatedActivity).await()

            Log.i(TAG, "Instant activity time updated successfully: ${activity.type.displayName} for baby: $babyId")
            Result.success(updatedActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update instant activity time: $activityId for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Update the notes of an activity
     */
    suspend fun updateActivityNotes(
        activityId: String,
        babyId: String,
        newNotes: String
    ): Result<Activity> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to update activity notes - user not authenticated", error)
                return Result.failure(error)
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to update activity notes - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            val activityRef = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .document(activityId)

            val activityDoc = activityRef.get().await()
            val activity = activityDoc.toObject<Activity>()
            if (activity == null) {
                val error = Exception("Activity not found")
                Log.e(TAG, "Failed to update activity notes - activity not found: $activityId", error)
                return Result.failure(error)
            }

            val updatedActivity = activity.copy(
                notes = newNotes,
                updatedAt = Timestamp.now()
            )

            activityRef.set(updatedActivity).await()

            Log.i(TAG, "Activity notes updated successfully: ${activity.type.displayName} for baby: $babyId")
            Result.success(updatedActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update activity notes: $activityId for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Get a list of recent activities for a baby (for history/editing purposes)
     */
    suspend fun getRecentActivities(
        babyId: String,
        limit: Int = 20
    ): Result<List<Activity>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to get recent activities - user not authenticated", error)
                return Result.failure(error)
            }

            // Verify user has access to this baby
            if (!hasAccessToBaby(babyId)) {
                val error = Exception("No access to baby profile")
                Log.e(TAG, "Failed to get recent activities - no access to baby: $babyId", error)
                return Result.failure(error)
            }

            val querySnapshot = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val activities = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<Activity>()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse activity document: ${document.id}", e)
                    null
                }
            }

            Log.i(TAG, "Retrieved ${activities.size} recent activities for baby: $babyId")
            Result.success(activities)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get recent activities for baby: $babyId", e)
            Result.failure(e)
        }
    }

    /**
     * Check if current user has access to a baby profile
     */
    private suspend fun hasAccessToBaby(babyId: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            val babyDoc = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .get()
                .await()
            
            val parentIds = babyDoc.get("parentIds") as? List<*>
            parentIds?.contains(currentUser.uid) == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check baby access: $babyId", e)
            false
        }
    }
}
