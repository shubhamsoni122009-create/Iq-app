package com.iqlock.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AppSettings.kt — Room entity holding all user-configurable settings.
 *
 * Only a single row is ever stored (id = 1). The SettingsViewModel reads
 * and updates this row through AppSettingsDao.
 *
 * Fields:
 *  - id: always 1 (singleton row)
 *  - isEnabled: global master switch — if false, no app is locked
 *  - unlockTimerSeconds: how many seconds the user has per challenge (default 75)
 *  - lockDurationMinutes: how long the app stays locked after failure (default 5)
 *  - maxAttempts: maximum number of challenge attempts (default 2)
 *  - difficulty: 1=Easy, 2=Medium, 3=Hard (filters riddle difficulty)
 *  - darkMode: 0=System, 1=Light, 2=Dark
 *  - showHints: if true, a hint appears after 40 seconds
 *  - riddleRotationPointer: index into the shuffled riddle order (rotation tracking)
 *  - shuffledRiddleOrder: comma-separated riddle IDs in current rotation order
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "unlock_timer_seconds")
    val unlockTimerSeconds: Int = 75,

    @ColumnInfo(name = "lock_duration_minutes")
    val lockDurationMinutes: Int = 5,

    @ColumnInfo(name = "max_attempts")
    val maxAttempts: Int = 2,

    @ColumnInfo(name = "difficulty")
    val difficulty: Int = 2,

    @ColumnInfo(name = "dark_mode")
    val darkMode: Int = 0,

    @ColumnInfo(name = "show_hints")
    val showHints: Boolean = true,

    @ColumnInfo(name = "riddle_rotation_pointer")
    val riddleRotationPointer: Int = 0,

    @ColumnInfo(name = "shuffled_riddle_order")
    val shuffledRiddleOrder: String = ""     // "34,12,87,..." — comma-separated riddle IDs
)
