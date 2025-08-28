package com.github.slamdev.babyroutinetracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.slamdev.babyroutinetracker.MainActivity
import com.github.slamdev.babyroutinetracker.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for handling push notifications
 */
class PushNotificationService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PushNotificationService"
        private const val CHANNEL_ID = "partner_activity_notifications"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Called when Firebase generates a new token
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated: $token")
        
        // Update user's FCM token in Firestore
        updateUserToken(token)
    }

    /**
     * Called when a message is received
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Handle notification payload (always show notification)
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message notification body: ${notification.body}")
            Log.d(TAG, "Showing notification with title: ${notification.title}")
            showNotification(
                title = notification.title ?: getString(R.string.notification_default_title),
                body = notification.body ?: getString(R.string.notification_default_body),
                data = remoteMessage.data
            )
        }
        
        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        } else {
            Log.d(TAG, "No data payload, notification should have been shown above")
        }
    }

    /**
     * Show notification to user
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        Log.d(TAG, "Creating notification - Title: '$title', Body: '$body'")
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Check if notifications are enabled
        val notificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
        Log.d(TAG, "Notifications enabled: $notificationsEnabled")
        
        // Check channel importance (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            Log.d(TAG, "Notification channel exists: ${channel != null}")
            Log.d(TAG, "Channel importance: ${channel?.importance}")
        }
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data to intent if needed for deep linking
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Changed to HIGH for better visibility
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Add sound, vibration, lights
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationId = System.currentTimeMillis().toInt()
        
        Log.d(TAG, "Posting notification with ID: $notificationId")
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notification posted successfully")
    }

    /**
     * Handle data-only messages
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val activityType = data["activityType"]
        val babyName = data["babyName"]
        val partnerName = data["partnerName"]
        val timestamp = data["timestamp"]
        
        Log.d(TAG, "Handling data message - activityType: '$activityType', babyName: '$babyName', partnerName: '$partnerName'")
        
        // Check if we have meaningful data (not null and not empty)
        if (!activityType.isNullOrBlank() && !babyName.isNullOrBlank() && !partnerName.isNullOrBlank()) {
            val title = getString(R.string.notification_new_activity_title, activityType)
            val body = getString(R.string.notification_baby_activity_title, babyName)
            
            Log.d(TAG, "Showing data-based notification: $title - $body")
            showNotification(title, body, data)
        } else {
            Log.d(TAG, "Data message has empty values, skipping data-based notification")
        }
    }

    /**
     * Create notification channel for Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH // Changed to HIGH for better visibility
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created with ID: $CHANNEL_ID")
        }
    }

    /**
     * Update user's FCM token in Firestore
     */
    private fun updateUserToken(token: String) {
        // We'll implement this with UserService
        val userService = UserService()
        userService.updateFcmToken(token)
    }
}
