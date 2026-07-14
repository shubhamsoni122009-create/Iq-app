package com.iqlock.app.data.dao

import androidx.room.*
import com.iqlock.app.data.entity.Statistic
import kotlinx.coroutines.flow.Flow

/**
 * StatisticDao.kt — DAO for daily/weekly usage statistics.
 *
 * One row per (packageName, date). Supports upsert pattern:
 * insert-or-ignore on first use, then individual column increments.
 */
@Dao
interface StatisticDao {

    /** Insert a new stat row. IGNORE if the (packageName, date) row already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(stat: Statistic): Long

    /** Replace an existing stat row with updated values. */
    @Update
    suspend fun update(stat: Statistic)

    /** Get the stat row for a specific app on a specific date ("YYYY-MM-DD"). */
    @Query("SELECT * FROM statistics WHERE package_name = :packageName AND date = :date LIMIT 1")
    suspend fun getForDate(packageName: String, date: String): Statistic?

    /** Get all stat rows for a specific date, ordered by package name. */
    @Query("SELECT * FROM statistics WHERE date = :date ORDER BY package_name ASC")
    suspend fun getAllForDate(date: String): List<Statistic>

    /** Get stats for the last N days as a Flow for live observation. */
    @Query("""
        SELECT * FROM statistics 
        WHERE date >= :fromDate 
        ORDER BY date DESC, package_name ASC
    """)
    fun getStatsFromDateFlow(fromDate: String): Flow<List<Statistic>>

    /** Aggregate total unlock attempts across all apps for a date range. */
    @Query("""
        SELECT SUM(unlock_attempts) FROM statistics 
        WHERE date >= :fromDate
    """)
    suspend fun totalAttemptsFrom(fromDate: String): Int

    /** Aggregate total successful unlocks across all apps for a date range. */
    @Query("""
        SELECT SUM(successful_unlocks) FROM statistics 
        WHERE date >= :fromDate
    """)
    suspend fun totalSuccessesFrom(fromDate: String): Int

    /** Aggregate total usage time in milliseconds for a specific app, date range. */
    @Query("""
        SELECT SUM(total_usage_time_ms) FROM statistics 
        WHERE package_name = :packageName AND date >= :fromDate
    """)
    suspend fun totalUsageForApp(packageName: String, fromDate: String): Long

    /**
     * Increment unlock_attempts by 1 for an existing row.
     * Returns 0 if the row doesn't exist (caller should insert first).
     */
    @Query("""
        UPDATE statistics 
        SET unlock_attempts = unlock_attempts + 1 
        WHERE package_name = :packageName AND date = :date
    """)
    suspend fun incrementAttempts(packageName: String, date: String): Int

    /** Increment successful_unlocks by 1 for an existing row. */
    @Query("""
        UPDATE statistics 
        SET successful_unlocks = successful_unlocks + 1 
        WHERE package_name = :packageName AND date = :date
    """)
    suspend fun incrementSuccesses(packageName: String, date: String): Int

    /** Increment failed_unlocks by 1 for an existing row. */
    @Query("""
        UPDATE statistics 
        SET failed_unlocks = failed_unlocks + 1 
        WHERE package_name = :packageName AND date = :date
    """)
    suspend fun incrementFailures(packageName: String, date: String): Int

    /** Add usage time to the running total for an existing row. */
    @Query("""
        UPDATE statistics 
        SET total_usage_time_ms = total_usage_time_ms + :additionalMs 
        WHERE package_name = :packageName AND date = :date
    """)
    suspend fun addUsageTime(packageName: String, date: String, additionalMs: Long): Int

    /** Get distinct dates that have any data, for history display. */
    @Query("SELECT DISTINCT date FROM statistics ORDER BY date DESC LIMIT 30")
    suspend fun getRecentDates(): List<String>

    /** Delete old statistics older than 90 days. */
    @Query("DELETE FROM statistics WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)
}
