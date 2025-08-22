package com.github.slamdev.babyroutinetracker.model

import com.google.firebase.Timestamp

/**
 * User preferences for partner activity notifications
 */
data class NotificationPreferences(
    val id: String = "",
    val userId: String = "",
    val babyId: String = "",
    val enablePartnerNotifications: Boolean = true,
    val notifySleepActivities: Boolean = true,
    val notifyFeedingActivities: Boolean = true,
    val notifyDiaperActivities: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00", // HH:MM format
    val quietHoursEnd: String = "06:00",   // HH:MM format
    val enableNotificationSound: Boolean = true,
    val enableNotificationVibration: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
