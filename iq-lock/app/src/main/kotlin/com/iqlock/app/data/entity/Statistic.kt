package com.iqlock.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Statistic.kt — Room entity storing daily usage and unlock statistics per protected app.
 *
 * One row is created per (packageName, date) pair.
 * Date is stored as "YYYY-MM-DD" string for easy GROUP BY querying.
 *
 * Fields:
 *  - id: auto-generated primary key
 *  - packageName: the app this stat row belongs to ("com.instagram.android")
 *  - date: ISO date string "YYYY-MM-DD"
 *  - totalUsageTimeMs: cumulative foreground time for this app on this date (milliseconds)
 *  - unlockAttempts: total number of challenge attempts started
 *  - successfulUnlocks: challenges solved correctly within time limit
 *  - failedUnlocks: challenges where user ran out of time or exhausted attempts
 *  - blockedMinutes: total minutes the app was locked out after failed attempts
 */
@Entity(tableName = "statistics")
data class Statistic(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "date")
    val date: String,                        // "2025-01-15"

    @ColumnInfo(name = "total_usage_time_ms")
    val totalUsageTimeMs: Long = 0L,

    @ColumnInfo(name = "unlock_attempts")
    val unlockAttempts: Int = 0,

    @ColumnInfo(name = "successful_unlocks")
    val successfulUnlocks: Int = 0,

    @ColumnInfo(name = "failed_unlocks")
    val failedUnlocks: Int = 0,

    @ColumnInfo(name = "blocked_minutes")
    val blockedMinutes: Int = 0
)
