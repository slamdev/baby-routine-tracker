package com.github.slamdev.babyroutinetracker.service

import android.content.Context
import android.util.Log
import com.github.slamdev.babyroutinetracker.R
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.model.User
import com.github.slamdev.babyroutinetracker.util.LocalizedMessageProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Service for sending partner activity notifications with full internationalization support
 */
class PartnerNotificationService(
    private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    // Configure Firebase Functions to use europe-west1 region
    private val functions = Firebase.functions("europe-west1")
    private val userService = UserService()
    private val notificationPreferencesService = NotificationPreferencesService(context)
    private val messageProvider = LocalizedMessageProvider(context)

    companion object {
        private const val TAG = "PartnerNotificationService"
    }

    /**
     * Send notification to partners when an activity is logged
     */
    suspend fun notifyPartnersOfActivity(activity: Activity, baby: Baby, context: Context? = null) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Cannot send notifications - user not authenticated")
                return
            }

            // Get current user's name for the notification
            val currentUserDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val currentUserData = currentUserDoc.toObject(User::class.java)
            val senderName = currentUserData?.displayName ?: context.getString(R.string.default_partner_name)

            // Get FCM tokens for all partners (excluding current user)
            val partnerTokens = userService.getPartnerFcmTokens(baby.id, excludeCurrentUser = true)
            
            if (partnerTokens.isEmpty()) {
                Log.d(TAG, "No partner tokens found for baby: ${baby.id}")
                return
            }

            // Check each partner's notification preferences
            val tokensToNotify = mutableListOf<String>()
            for (token in partnerTokens) {
                // Find user ID by token (this could be optimized with a reverse lookup)
                val usersQuery = firestore.collection("users")
                    .whereEqualTo("fcmToken", token)
                    .get()
                    .await()
                
                if (!usersQuery.isEmpty) {
                    val userId = usersQuery.documents.first().id
                    val shouldNotify = notificationPreferencesService.shouldSendNotification(
                        babyId = baby.id,
                        activityType = activity.type.displayName,
                        recipientUserId = userId
                    )
                    
                    if (shouldNotify) {
                        tokensToNotify.add(token)
                    }
                }
            }

            if (tokensToNotify.isEmpty()) {
                Log.d(TAG, "No partners to notify after preference filtering for baby: ${baby.id}")
                return
            }

            // Create notification payload
            val notificationData = 
                createNotificationPayload(activity, baby, senderName)
            
            // Send notifications via Firebase Functions
            sendNotificationsViaCloudFunction(tokensToNotify, notificationData)
            
            Log.d(TAG, "Sent notifications to ${tokensToNotify.size} partners for activity: ${activity.type.displayName}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify partners of activity", e)
        }
    }

    /**
     * Create notification payload based on activity type
     */
    private fun createNotificationPayload(
        activity: Activity, 
        baby: Baby, 
        senderName: String,
        context: Context
    ): Map<String, Any> {
        val activityTypeDisplay = when (activity.type) {
            ActivityType.SLEEP -> if (activity.isOngoing()) 
                context.getString(R.string.notification_activity_started_sleep) 
            else 
                context.getString(R.string.notification_activity_ended_sleep)
            ActivityType.FEEDING -> when (activity.feedingType) {
                "breast_milk" -> if (activity.isOngoing()) 
                    context.getString(R.string.notification_activity_started_breast_feeding) 
                else 
                    context.getString(R.string.notification_activity_finished_breast_feeding)
                "bottle" -> context.getString(R.string.notification_activity_gave_bottle, activity.amount.toInt())
                else -> context.getString(R.string.notification_activity_logged_feeding)
            }
            ActivityType.DIAPER -> context.getString(R.string.notification_activity_changed_diaper)
        }

        val title = context.getString(R.string.notification_title_new_activity, baby.name)
        val body = context.getString(R.string.notification_body_activity, senderName, activityTypeDisplay)
        
        // Add duration for completed activities
        val bodyWithDetails = if (!activity.isOngoing() && activity.getDurationMinutes() != null) {
            val duration = activity.getDurationMinutes()!!
            val durationText = if (duration >= 60) {
                "${duration / 60}h ${duration % 60}m"
            } else {
                "${duration}m"
            }
            "$body (${durationText})"
        } else {
            body
        }

        return mapOf(
            "title" to title,
            "body" to bodyWithDetails,
            "babyId" to baby.id,
            "babyName" to baby.name,
            "activityType" to activity.type.displayName,
            "activityId" to activity.id,
            "partnerName" to senderName,
            "timestamp" to System.currentTimeMillis().toString(),
            "icon" to getActivityIcon(activity.type)
        )
    }

    /**
     * Create notification payload with localized strings
     */
    private fun createNotificationPayload(
        activity: Activity, 
        baby: Baby, 
        senderName: String
    ): Map<String, Any> {
        val activityTypeDisplay = when (activity.type) {
            ActivityType.SLEEP -> if (activity.isOngoing()) {
                messageProvider.getActivityStartedSleepMessage()
            } else {
                messageProvider.getActivityEndedSleepMessage()
            }
            ActivityType.FEEDING -> when (activity.feedingType) {
                "breast_milk" -> if (activity.isOngoing()) {
                    messageProvider.getActivityStartedBreastFeedingMessage()
                } else {
                    messageProvider.getActivityFinishedBreastFeedingMessage()
                }
                "bottle" -> messageProvider.getGaveBottleNotification(activity.amount.toInt())
                else -> messageProvider.getActivityLoggedFeedingMessage()
            }
            ActivityType.DIAPER -> messageProvider.getActivityChangedDiaperMessage()
        }

        val title = messageProvider.getNewActivityNotificationTitle(baby.name)
        val body = messageProvider.getPartnerActivityNotificationBody(senderName, activityTypeDisplay)
        
        // Add duration for completed activities
        val bodyWithDetails = if (!activity.isOngoing() && activity.getDurationMinutes() != null) {
            val duration = activity.getDurationMinutes()!!
            val durationText = if (duration >= 60) {
                "${duration / 60}h ${duration % 60}m"
            } else {
                "${duration}m"
            }
            "$body (${durationText})"
        } else {
            body
        }

        return mapOf(
            "title" to title,
            "body" to bodyWithDetails,
            "babyId" to baby.id,
            "babyName" to baby.name,
            "activityType" to activity.type.displayName,
            "activityId" to activity.id,
            "partnerName" to senderName,
            "timestamp" to System.currentTimeMillis().toString(),
            "icon" to getActivityIcon(activity.type)
        )
    }

    /**
     * Get emoji icon for activity type
     */
    private fun getActivityIcon(activityType: ActivityType): String {
        return when (activityType) {
            ActivityType.SLEEP -> "😴"
            ActivityType.FEEDING -> "🍼"
            ActivityType.DIAPER -> "💩"
        }
    }

    /**
     * Send notifications via Firebase Cloud Function
     * This will be called by a Firebase Cloud Function to actually send the FCM messages
     */
    private suspend fun sendNotificationsViaCloudFunction(
        tokens: List<String>,
        notificationData: Map<String, Any>
    ) {
        try {
            val data = hashMapOf(
                "tokens" to tokens,
                "notification" to notificationData
            )

            // Call Firebase Cloud Function to send notifications
            // Note: This requires a Cloud Function to be deployed
            functions.getHttpsCallable("sendPartnerNotifications")
                .call(data)
                .await()
                
            Log.d(TAG, "Successfully called cloud function to send notifications")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call cloud function for notifications", e)
            
            // Fallback: Try to send notifications directly using FCM Admin SDK
            // This would require different implementation and security considerations
            Log.w(TAG, "Cloud function failed, notifications may not be sent")
        }
    }

    /**
     * Test notification functionality
     */
    suspend fun sendTestNotification(babyId: String, context: Context? = null): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception(messageProvider.getUserNotAuthenticatedMessage()))
            }

            val partnerTokens = userService.getPartnerFcmTokens(babyId, excludeCurrentUser = true)
            
            if (partnerTokens.isEmpty()) {
                return Result.failure(Exception(messageProvider.getNoPartnersFoundToNotifyMessage()))
            }

            val testData = if (context != null) {
                mapOf(
                    "title" to context.getString(R.string.notification_test_title),
                    "body" to context.getString(R.string.notification_test_body),
                    "babyId" to babyId,
                    "timestamp" to System.currentTimeMillis().toString()
                )
            } else {
                mapOf(
                    "title" to messageProvider.getTestNotificationTitle(),
                    "body" to messageProvider.getTestNotificationBody(),
                    "babyId" to babyId,
                    "timestamp" to System.currentTimeMillis().toString()
                )
            }

            sendNotificationsViaCloudFunction(partnerTokens, testData)
            Result.success("Test notification sent to ${partnerTokens.size} partners")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send test notification", e)
            Result.failure(e)
        }
    }
}
