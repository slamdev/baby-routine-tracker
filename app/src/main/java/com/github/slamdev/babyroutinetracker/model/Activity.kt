package com.github.slamdev.babyroutinetracker.model

import com.google.firebase.Timestamp

data class Activity(
    val id: String = "",
    val type: ActivityType = ActivityType.SLEEP,
    val babyId: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp? = null,
    val notes: String = "",
    val loggedBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    
    // Feeding-specific fields
    val feedingType: String = "",  // "breast_milk", "bottle"
    val amount: Double = 0.0,      // ml, for bottle feeding
    
    // Diaper-specific fields
    val diaperType: String = "",   // "wet", "dirty", "both"
) {
    val isOngoing: Boolean
        get() = endTime == null
    
    val durationMinutes: Long?
        get() = endTime?.let { end ->
            ((end.seconds - startTime.seconds) / 60)
        }
}

enum class ActivityType(val displayName: String) {
    SLEEP("Sleep"),
    FEEDING("Feeding"),
    DIAPER("Diaper")
}
