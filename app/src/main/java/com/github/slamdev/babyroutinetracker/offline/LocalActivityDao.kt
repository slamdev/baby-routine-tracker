package com.github.slamdev.babyroutinetracker.offline

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for offline activities
 */
@Dao
interface LocalActivityDao {
    
    @Query("SELECT * FROM activities WHERE babyId = :babyId ORDER BY startTime DESC")
    fun getActivitiesFlow(babyId: String): Flow<List<LocalActivity>>
    
    @Query("SELECT * FROM activities WHERE babyId = :babyId ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentActivities(babyId: String, limit: Int = 50): List<LocalActivity>
    
    @Query("SELECT * FROM activities WHERE babyId = :babyId AND startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    suspend fun getActivitiesInDateRange(babyId: String, startTime: Long, endTime: Long): List<LocalActivity>
    
    @Query("SELECT * FROM activities WHERE babyId = :babyId AND type = :type AND startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    suspend fun getActivitiesInDateRangeByType(babyId: String, startTime: Long, endTime: Long, type: String): List<LocalActivity>
    
    @Query("SELECT * FROM activities WHERE babyId = :babyId AND type = :type AND endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getOngoingActivity(babyId: String, type: String): LocalActivity?
    
    @Query("SELECT * FROM activities WHERE babyId = :babyId AND type = :type AND endTime IS NOT NULL ORDER BY endTime DESC LIMIT 1")
    suspend fun getLastCompletedActivity(babyId: String, type: String): LocalActivity?
    
    @Query("SELECT * FROM activities WHERE babyId = :babyId AND type = :type AND endTime IS NOT NULL ORDER BY endTime DESC LIMIT 1")
    fun getLastCompletedActivityFlow(babyId: String, type: String): Flow<LocalActivity?>
    
    @Query("SELECT * FROM activities WHERE babyId = :babyId AND type = :type AND endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getOngoingActivityFlow(babyId: String, type: String): Flow<LocalActivity?>
    
    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: String): LocalActivity?
    
    @Query("SELECT * FROM activities WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedActivities(): List<LocalActivity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: LocalActivity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<LocalActivity>)
    
    @Update
    suspend fun updateActivity(activity: LocalActivity)
    
    @Query("UPDATE activities SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("UPDATE activities SET syncAttempts = :attempts, lastSyncAttempt = :timestamp WHERE id = :id")
    suspend fun updateSyncAttempt(id: String, attempts: Int, timestamp: Long)
    
    @Delete
    suspend fun deleteActivity(activity: LocalActivity)
    
    @Query("DELETE FROM activities WHERE babyId = :babyId")
    suspend fun deleteAllActivitiesForBaby(babyId: String)
    
    @Query("DELETE FROM activities WHERE isSynced = 1 AND createdAt < :cutoffTime")
    suspend fun deleteOldSyncedActivities(cutoffTime: Long)
}
