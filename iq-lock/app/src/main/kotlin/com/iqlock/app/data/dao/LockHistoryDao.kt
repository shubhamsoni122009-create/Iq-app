package com.iqlock.app.data.dao

import androidx.room.*
import com.iqlock.app.data.entity.LockHistory
import kotlinx.coroutines.flow.Flow

/**
 * LockHistoryDao.kt — DAO for lock/unlock event history.
 *
 * Records every time a protected app is opened and the user is challenged.
 * Used by the StatisticsActivity to show a timeline of events.
 */
@Dao
interface LockHistoryDao {

    /** Insert a new history entry. Returns the newly assigned row ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LockHistory): Long

    /** Update an existing history entry (e.g., setting outcome after attempt). */
    @Update
    suspend fun update(entry: LockHistory)

    /** Get a single history entry by ID. */
    @Query("SELECT * FROM lock_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): LockHistory?

    /**
     * Get the most recent lock entry for a specific app, regardless of outcome.
     * Used by the accessibility service to check if an app is currently locked out.
     */
    @Query("""
        SELECT * FROM lock_history 
        WHERE package_name = :packageName 
        ORDER BY attempted_at DESC 
        LIMIT 1
    """)
    suspend fun getLatestForApp(packageName: String): LockHistory?

    /**
     * Check if a package is currently in lockout period.
     * Returns entries where locked_until > current time.
     */
    @Query("""
        SELECT * FROM lock_history 
        WHERE package_name = :packageName AND locked_until > :currentTime
        ORDER BY locked_until DESC 
        LIMIT 1
    """)
    suspend fun getActiveLockout(packageName: String, currentTime: Long): LockHistory?

    /** Get all history entries as a Flow, newest first, for the history screen. */
    @Query("SELECT * FROM lock_history ORDER BY attempted_at DESC LIMIT 200")
    fun getAllFlow(): Flow<List<LockHistory>>

    /** Get history entries for a specific package, newest first. */
    @Query("""
        SELECT * FROM lock_history 
        WHERE package_name = :packageName 
        ORDER BY attempted_at DESC 
        LIMIT 50
    """)
    fun getForPackageFlow(packageName: String): Flow<List<LockHistory>>

    /** Count successful unlocks since a timestamp (used for daily/weekly totals). */
    @Query("""
        SELECT COUNT(*) FROM lock_history 
        WHERE outcome = 'SOLVED' AND attempted_at >= :since
    """)
    suspend fun countSuccessesSince(since: Long): Int

    /** Count failed unlocks since a timestamp. */
    @Query("""
        SELECT COUNT(*) FROM lock_history 
        WHERE outcome = 'FAILED' AND attempted_at >= :since
    """)
    suspend fun countFailuresSince(since: Long): Int

    /** Delete entries older than 30 days to keep the database lean. */
    @Query("DELETE FROM lock_history WHERE attempted_at < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
