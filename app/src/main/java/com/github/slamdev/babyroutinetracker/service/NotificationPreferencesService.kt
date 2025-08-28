package com.github.slamdev.babyroutinetracker.service

import android.content.Context
import android.util.Log
import com.github.slamdev.babyroutinetracker.model.NotificationPreferences
import com.github.slamdev.babyroutinetracker.model.OptionalUiState
import com.github.slamdev.babyroutinetracker.util.LocalizedMessageProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Service for managing notification preferences
 */
class NotificationPreferencesService(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messageProvider = LocalizedMessageProvider(context)

    companion object {
        private const val TAG = "NotificationPreferencesService"
        private const val PREFERENCES_COLLECTION = "notificationPreferences"
    }

    /**
     * Get notification preferences for current user and baby
     */
    fun getNotificationPreferencesFlow(babyId: String): Flow<OptionalUiState<NotificationPreferences>> = callbackFlow {
        trySend(OptionalUiState.Loading)
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(OptionalUiState.Error(Exception("User not authenticated"), messageProvider.getPleaseSignInToViewPreferencesMessage()))
            close()
            return@callbackFlow
        }

        val preferencesId = "${currentUser.uid}_$babyId"
        val listenerRegistration = firestore.collection(PREFERENCES_COLLECTION)
            .document(preferencesId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to notification preferences", error)
                    val userMessage = when {
                        error.message?.contains("PERMISSION_DENIED") == true -> 
                            "                            messageProvider.getNoPermissionToViewPreferencesMessage()"
                        error.message?.contains("UNAVAILABLE") == true -> 
                            "                            messageProvider.getUnableToConnectToServerMessage()"
                        else -> messageProvider.getUnableToLoadNotificationPreferencesMessage()
                    }
                    trySend(OptionalUiState.Error(error, userMessage))
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val preferences = snapshot.toObject<NotificationPreferences>()?.copy(id = snapshot.id)
                        if (preferences != null) {
                            trySend(OptionalUiState.Success(preferences))
                        } else {
                            trySend(OptionalUiState.Empty)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing notification preferences snapshot", e)
                        trySend(OptionalUiState.Error(e, messageProvider.getFailedToProcessNotificationPreferencesMessage()))
                    }
                } else {
                    // Create default preferences if none exist
                    val defaultPreferences = NotificationPreferences(
                        id = preferencesId,
                        userId = currentUser.uid,
                        babyId = babyId
                    )
                    trySend(OptionalUiState.Success(defaultPreferences))
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Update notification preferences
     */
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<NotificationPreferences> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val error = Exception("User not authenticated")
                Log.e(TAG, "Failed to update preferences - user not authenticated", error)
                return Result.failure(error)
            }

            val preferencesId = "${currentUser.uid}_${preferences.babyId}"
            val updatedPreferences = preferences.copy(
                id = preferencesId,
                userId = currentUser.uid,
                updatedAt = Timestamp.now()
            )

            firestore.collection(PREFERENCES_COLLECTION)
                .document(preferencesId)
                .set(updatedPreferences)
                .await()

            Log.d(TAG, "Successfully updated notification preferences for user: ${currentUser.uid}, baby: ${preferences.babyId}")
            Result.success(updatedPreferences)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification preferences", e)
            Result.failure(e)
        }
    }

    /**
     * Check if notifications should be sent based on preferences and quiet hours
     */
    suspend fun shouldSendNotification(
        babyId: String, 
        activityType: String,
        recipientUserId: String
    ): Boolean {
        return try {
            val preferencesId = "${recipientUserId}_$babyId"
            val preferencesDoc = firestore.collection(PREFERENCES_COLLECTION)
                .document(preferencesId)
                .get()
                .await()

            val preferences = if (preferencesDoc.exists()) {
                preferencesDoc.toObject<NotificationPreferences>()
            } else {
                // Use default preferences if none exist
                NotificationPreferences(userId = recipientUserId, babyId = babyId)
            }

            if (preferences == null || !preferences.enablePartnerNotifications) {
                return false
            }

            // Check activity type preferences
            val shouldNotifyForActivity = when (activityType.lowercase()) {
                "sleep" -> preferences.notifySleepActivities
                "feeding" -> preferences.notifyFeedingActivities
                "diaper" -> preferences.notifyDiaperActivities
                else -> false
            }

            if (!shouldNotifyForActivity) {
                return false
            }

            // Check quiet hours
            if (preferences.quietHoursEnabled) {
                val currentTime = Calendar.getInstance()
                val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                val currentMinute = currentTime.get(Calendar.MINUTE)
                val currentTimeMinutes = currentHour * 60 + currentMinute

                val startTime = parseTime(preferences.quietHoursStart)
                val endTime = parseTime(preferences.quietHoursEnd)

                // Handle quiet hours that span midnight
                val isInQuietHours = if (startTime <= endTime) {
                    currentTimeMinutes >= startTime && currentTimeMinutes <= endTime
                } else {
                    currentTimeMinutes >= startTime || currentTimeMinutes <= endTime
                }

                if (isInQuietHours) {
                    Log.d(TAG, "Notification blocked due to quiet hours for user: $recipientUserId")
                    return false
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking notification preferences", e)
            // Default to sending notification if we can't check preferences
            true
        }
    }

    /**
     * Parse time string (HH:MM) to minutes since midnight
     */
    private fun parseTime(timeString: String): Int {
        return try {
            val parts = timeString.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            hours * 60 + minutes
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse time string: $timeString", e)
            0 // Default to midnight
        }
    }
}
