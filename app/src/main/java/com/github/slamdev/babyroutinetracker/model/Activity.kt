package com.github.slamdev.babyroutinetracker.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

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
    val diaperType: String = "",   // "poop" for adjusted requirement
) {
    @Exclude
    fun isOngoing(): Boolean = endTime == null
    
    @Exclude
    fun getDurationMinutes(): Long? = endTime?.let { end ->
        ((end.seconds - startTime.seconds) / 60)
    }
}

enum class ActivityType(val displayName: String) {
    SLEEP("Sleep"),
    FEEDING("Feeding"),
    DIAPER("Diaper")
}
