package com.iqlock.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * LockHistory.kt — Room entity that records every lock event.
 *
 * Every time a user attempts to unlock a protected app, a row is inserted.
 * This powers the usage-history screen and helps track behavioral patterns.
 *
 * Fields:
 *  - id: auto-generated primary key
 *  - packageName: app that triggered the lock ("com.instagram.android")
 *  - appLabel: human-readable app name ("Instagram")
 *  - attemptedAt: epoch millis when the lock screen appeared
 *  - solvedAt: epoch millis when the correct answers were given (0 = not solved)
 *  - outcome: SOLVED, FAILED, EXPIRED, or CANCELLED
 *  - riddleIds: comma-separated IDs of riddles shown in this session
 *  - timeTakenMs: how long from challenge start to resolution
 *  - lockedUntil: epoch millis until which the app is blocked (0 = not locked)
 */
@Entity(tableName = "lock_history")
data class LockHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "app_label")
    val appLabel: String,

    @ColumnInfo(name = "attempted_at")
    val attemptedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "solved_at")
    val solvedAt: Long = 0L,

    @ColumnInfo(name = "outcome")
    val outcome: String = LockOutcome.PENDING.name,

    @ColumnInfo(name = "riddle_ids")
    val riddleIds: String = "",         // "12,45"

    @ColumnInfo(name = "time_taken_ms")
    val timeTakenMs: Long = 0L,

    @ColumnInfo(name = "locked_until")
    val lockedUntil: Long = 0L
)

enum class LockOutcome {
    PENDING, SOLVED, FAILED, EXPIRED, CANCELLED
}
