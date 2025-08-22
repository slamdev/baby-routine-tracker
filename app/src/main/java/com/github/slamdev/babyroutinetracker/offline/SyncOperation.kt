package com.github.slamdev.babyroutinetracker.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity for tracking pending sync operations
 */
@Entity(tableName = "sync_queue")
data class SyncOperation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: SyncOperationType,
    val activityId: String,
    val babyId: String,
    val operationData: String, // JSON data for the operation
    val createdAt: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
    val lastAttempt: Long? = null,
    val maxRetries: Int = 3
) {
    fun shouldRetry(): Boolean = attempts < maxRetries
    
    fun canRetryNow(): Boolean {
        if (!shouldRetry()) return false
        
        // Exponential backoff: wait 1min, 5min, 15min between retries
        val backoffDelays = listOf(60_000L, 300_000L, 900_000L)
        val delayIndex = (attempts - 1).coerceAtMost(backoffDelays.size - 1)
        val requiredDelay = if (attempts == 0) 0L else backoffDelays[delayIndex]
        
        return lastAttempt?.let { 
            System.currentTimeMillis() - it >= requiredDelay 
        } ?: true
    }
}

enum class SyncOperationType {
    CREATE_ACTIVITY,
    UPDATE_ACTIVITY,
    END_ACTIVITY,
    UPDATE_START_TIME,
    UPDATE_TIMES,
    UPDATE_NOTES
}
