package com.github.slamdev.babyroutinetracker.offline

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Room database for offline data storage
 */
@Database(
    entities = [LocalActivity::class, SyncOperation::class],
    version = 1,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {
    
    abstract fun activityDao(): LocalActivityDao
    abstract fun syncOperationDao(): SyncOperationDao
    
    companion object {
        @Volatile
        private var INSTANCE: OfflineDatabase? = null
        
        fun getDatabase(context: Context): OfflineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "baby_routine_offline_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
