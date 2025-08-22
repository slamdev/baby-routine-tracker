package com.github.slamdev.babyroutinetracker.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.slamdev.babyroutinetracker.model.Activity
import com.github.slamdev.babyroutinetracker.model.ActivityType
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Local database entity for storing activities offline
 */
@Entity(tableName = "activities")
data class LocalActivity(
    @PrimaryKey
    val id: String,
    val type: String, // ActivityType as string
    val babyId: String,
    val startTime: Long, // Timestamp as millis
    val endTime: Long?, // Timestamp as millis
    val notes: String,
    val loggedBy: String,
    val createdAt: Long, // Timestamp as millis
    val updatedAt: Long, // Timestamp as millis
    
    // Feeding-specific fields
    val feedingType: String,
    val amount: Double,
    
    // Diaper-specific fields
    val diaperType: String,
    
    // Sync status
    val isSynced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null
) {
    /**
     * Convert LocalActivity to Activity model
     */
    fun toActivity(): Activity {
        return Activity(
            id = id,
            type = ActivityType.valueOf(type),
            babyId = babyId,
            startTime = Timestamp(Date(startTime)),
            endTime = endTime?.let { Timestamp(Date(it)) },
            notes = notes,
            loggedBy = loggedBy,
            createdAt = Timestamp(Date(createdAt)),
            updatedAt = Timestamp(Date(updatedAt)),
            feedingType = feedingType,
            amount = amount,
            diaperType = diaperType
        )
    }

    companion object {
        /**
         * Convert Activity model to LocalActivity
         */
        fun fromActivity(activity: Activity, isSynced: Boolean = false): LocalActivity {
            return LocalActivity(
                id = activity.id,
                type = activity.type.name,
                babyId = activity.babyId,
                startTime = activity.startTime.toDate().time,
                endTime = activity.endTime?.toDate()?.time,
                notes = activity.notes,
                loggedBy = activity.loggedBy,
                createdAt = activity.createdAt.toDate().time,
                updatedAt = activity.updatedAt.toDate().time,
                feedingType = activity.feedingType,
                amount = activity.amount,
                diaperType = activity.diaperType,
                isSynced = isSynced
            )
        }
    }
}
