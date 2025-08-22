package com.github.slamdev.babyroutinetracker.offline

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for sync operations queue
 */
@Dao
interface SyncOperationDao {
    
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getAllOperations(): List<SyncOperation>
    
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    fun getAllOperationsFlow(): Flow<List<SyncOperation>>
    
    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingOperationsCountFlow(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun getPendingOperationsCount(): Int
    
    @Query("SELECT * FROM sync_queue WHERE attempts < maxRetries ORDER BY createdAt ASC")
    suspend fun getRetryableOperations(): List<SyncOperation>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: SyncOperation)
    
    @Update
    suspend fun updateOperation(operation: SyncOperation)
    
    @Delete
    suspend fun deleteOperation(operation: SyncOperation)
    
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteOperationById(id: String)
    
    @Query("DELETE FROM sync_queue WHERE activityId = :activityId")
    suspend fun deleteOperationsForActivity(activityId: String)
    
    @Query("DELETE FROM sync_queue")
    suspend fun deleteAllOperations()
}
