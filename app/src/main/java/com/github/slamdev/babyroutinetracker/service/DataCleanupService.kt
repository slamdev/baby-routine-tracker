package com.github.slamdev.babyroutinetracker.service

import android.util.Log
import com.github.slamdev.babyroutinetracker.model.Baby
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await

/**
 * Service for handling account deletion and data cleanup operations.
 * Ensures GDPR compliance for data deletion requests.
 */
class DataCleanupService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "DataCleanupService"
        private const val USERS_COLLECTION = "users"
        private const val BABIES_COLLECTION = "babies"
        private const val INVITATIONS_COLLECTION = "invitations"
        private const val NOTIFICATION_PREFERENCES_COLLECTION = "notificationPreferences"
        private const val ACTIVITIES_SUBCOLLECTION = "activities"
        private const val SLEEP_PLANS_SUBCOLLECTION = "sleepPlans"
    }

    /**
     * Delete all user data and close account.
     * This performs a complete data cleanup in compliance with GDPR.
     */
    suspend fun deleteUserAccountAndData(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val userId = currentUser.uid
            Log.i(TAG, "Starting account deletion process for user: $userId")

            // Step 1: Get all babies associated with this user
            val userBabies = getUserBabies(userId)
            Log.d(TAG, "Found ${userBabies.size} baby profiles for user")

            // Step 2: Delete or update baby data
            deleteOrUpdateBabyData(userId, userBabies)

            // Step 3: Delete user-specific data
            deleteUserSpecificData(userId)

            // Step 4: Delete Firebase Authentication account
            currentUser.delete().await()
            Log.i(TAG, "Firebase Authentication account deleted successfully")

            Log.i(TAG, "Account deletion completed successfully for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user account and data", e)
            Result.failure(e)
        }
    }

    /**
     * Get all babies associated with the current user
     */
    private suspend fun getUserBabies(userId: String): List<Baby> {
        return try {
            val querySnapshot = firestore.collection(BABIES_COLLECTION)
                .whereArrayContains("parentIds", userId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                document.toObject(Baby::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user babies: $userId", e)
            emptyList()
        }
    }

    /**
     * Delete or update baby data based on parent count
     */
    private suspend fun deleteOrUpdateBabyData(userId: String, babies: List<Baby>) {
        for (baby in babies) {
            try {
                if (baby.parentIds.size == 1 && baby.parentIds.contains(userId)) {
                    // User is the only parent - delete entire baby profile and all related data
                    deleteBabyProfileCompletely(baby.id)
                    Log.d(TAG, "Deleted baby profile completely: ${baby.name} (${baby.id})")
                } else {
                    // Multiple parents - remove user from parentIds but keep baby data
                    removeUserFromBabyProfile(baby.id, userId)
                    Log.d(TAG, "Removed user from baby profile: ${baby.name} (${baby.id})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle baby data for: ${baby.id}", e)
                // Continue with other babies even if one fails
            }
        }
    }

    /**
     * Delete baby profile and all related subcollections
     */
    private suspend fun deleteBabyProfileCompletely(babyId: String) {
        // Delete in batches to handle large datasets
        val batch = firestore.batch()

        try {
            // Delete all activities
            val activitiesSnapshot = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(ACTIVITIES_SUBCOLLECTION)
                .get()
                .await()

            for (activityDoc in activitiesSnapshot.documents) {
                batch.delete(activityDoc.reference)
            }

            // Delete all sleep plans
            val sleepPlansSnapshot = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .collection(SLEEP_PLANS_SUBCOLLECTION)
                .get()
                .await()

            for (sleepPlanDoc in sleepPlansSnapshot.documents) {
                batch.delete(sleepPlanDoc.reference)
            }

            // Delete the baby document itself
            val babyDocRef = firestore.collection(BABIES_COLLECTION).document(babyId)
            batch.delete(babyDocRef)

            // Commit the batch
            batch.commit().await()
            Log.d(TAG, "Successfully deleted baby profile and all related data: $babyId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete baby profile completely: $babyId", e)
            throw e
        }
    }

    /**
     * Remove user from baby's parentIds list
     */
    private suspend fun removeUserFromBabyProfile(babyId: String, userId: String) {
        try {
            val babyDocRef = firestore.collection(BABIES_COLLECTION).document(babyId)
            
            // Use FieldValue.arrayRemove to safely remove user from parentIds
            babyDocRef.update(
                mapOf(
                    "parentIds" to com.google.firebase.firestore.FieldValue.arrayRemove(userId),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            ).await()

            Log.d(TAG, "Removed user $userId from baby profile $babyId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove user from baby profile: $babyId", e)
            throw e
        }
    }

    /**
     * Delete all user-specific data
     */
    private suspend fun deleteUserSpecificData(userId: String) {
        val batch = firestore.batch()

        try {
            // Delete user document
            val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)
            batch.delete(userDocRef)

            // Delete all invitations created by this user
            val invitationsSnapshot = firestore.collection(INVITATIONS_COLLECTION)
                .whereEqualTo("invitedBy", userId)
                .get()
                .await()

            for (invitationDoc in invitationsSnapshot.documents) {
                batch.delete(invitationDoc.reference)
            }

            // Delete notification preferences for all babies this user was associated with
            val notificationPrefsSnapshot = firestore.collection(NOTIFICATION_PREFERENCES_COLLECTION)
                .whereGreaterThanOrEqualTo("__name__", "${userId}_")
                .whereLessThan("__name__", "${userId}_\uf8ff")
                .get()
                .await()

            for (prefsDoc in notificationPrefsSnapshot.documents) {
                batch.delete(prefsDoc.reference)
            }

            // Commit all user-specific deletions
            batch.commit().await()
            Log.d(TAG, "Successfully deleted all user-specific data for: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user-specific data: $userId", e)
            throw e
        }
    }

    /**
     * Audit log entry for deletion operation (for compliance tracking)
     */
    private suspend fun logDeletionAudit(userId: String, deletionType: String, details: String) {
        try {
            // In a production app, you might want to log this to a separate audit collection
            // or external logging service for compliance purposes
            Log.i("AUDIT_LOG", "DATA_DELETION: userId=$userId, type=$deletionType, details=$details, timestamp=${System.currentTimeMillis()}")
        } catch (e: Exception) {
            // Don't fail the deletion if audit logging fails
            Log.w(TAG, "Failed to log deletion audit", e)
        }
    }
}
