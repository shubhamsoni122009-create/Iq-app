package com.iqlock.app.data.dao

import androidx.room.*
import com.iqlock.app.data.entity.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * AppSettingsDao.kt — DAO for the singleton settings row (id = 1).
 *
 * Provides atomic update helpers for individual settings so the
 * SettingsViewModel doesn't need to read-modify-write the entire row.
 */
@Dao
interface AppSettingsDao {

    /** Insert the default settings on first launch. IGNORE if already present. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(settings: AppSettings)

    /** Replace the entire settings row (used for bulk updates). */
    @Update
    suspend fun update(settings: AppSettings)

    /** Get the current settings as a suspending call (one-shot). */
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun get(): AppSettings?

    /** Get the current settings as a Flow (for reactive UI observation). */
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getFlow(): Flow<AppSettings?>

    /** Toggle the global enabled/disabled master switch. */
    @Query("UPDATE app_settings SET is_enabled = :enabled WHERE id = 1")
    suspend fun setEnabled(enabled: Boolean)

    /** Update the unlock timer duration in seconds. */
    @Query("UPDATE app_settings SET unlock_timer_seconds = :seconds WHERE id = 1")
    suspend fun setUnlockTimer(seconds: Int)

    /** Update how long an app is locked after failed attempts. */
    @Query("UPDATE app_settings SET lock_duration_minutes = :minutes WHERE id = 1")
    suspend fun setLockDuration(minutes: Int)

    /** Update the riddle difficulty level (1=Easy, 2=Medium, 3=Hard). */
    @Query("UPDATE app_settings SET difficulty = :difficulty WHERE id = 1")
    suspend fun setDifficulty(difficulty: Int)

    /** Update the dark mode preference (0=System, 1=Light, 2=Dark). */
    @Query("UPDATE app_settings SET dark_mode = :darkMode WHERE id = 1")
    suspend fun setDarkMode(darkMode: Int)

    /** Update whether hints are shown after 40 seconds. */
    @Query("UPDATE app_settings SET show_hints = :show WHERE id = 1")
    suspend fun setShowHints(show: Boolean)

    /** Persist the riddle rotation pointer and shuffled order after a rotation update. */
    @Query("""
        UPDATE app_settings 
        SET riddle_rotation_pointer = :pointer, shuffled_riddle_order = :order 
        WHERE id = 1
    """)
    suspend fun updateRiddleRotation(pointer: Int, order: String)
}
