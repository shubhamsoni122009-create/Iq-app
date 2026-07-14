package com.iqlock.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iqlock.app.data.dao.*
import com.iqlock.app.data.entity.*

/**
 * AppDatabase.kt — Room database singleton for IQ Lock.
 *
 * Declares all entities (tables) and their corresponding DAO interfaces.
 * The singleton is constructed once via double-checked locking and reused throughout
 * the app's lifetime. Hilt provides AppDatabase through DatabaseModule.
 *
 * ENTITIES:
 *  - Riddle: stores the 100 IQ challenge questions and their usage counters
 *  - Statistic: daily per-app usage and unlock stats
 *  - LockHistory: event log for every lock/unlock attempt
 *  - ProtectedApp: user-selected apps to protect
 *  - AppSettings: user preferences (singleton row id=1)
 *
 * Version history:
 *  1 — initial schema
 */
@Database(
    entities = [
        Riddle::class,
        Statistic::class,
        LockHistory::class,
        ProtectedApp::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun riddleDao(): RiddleDao
    abstract fun statisticDao(): StatisticDao
    abstract fun lockHistoryDao(): LockHistoryDao
    abstract fun protectedAppDao(): ProtectedAppDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        private const val DATABASE_NAME = "iqlock.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance. Creates it on first call.
         * Pass the application context to prevent memory leaks.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // acceptable for v1 — add proper migrations before v2
                .build()
    }
}
