package com.iqlock.app.data.dao

import androidx.room.*
import com.iqlock.app.data.entity.Riddle
import kotlinx.coroutines.flow.Flow

/**
 * RiddleDao.kt — Data Access Object for riddle operations.
 *
 * Provides all SQL queries related to the riddles table:
 *  - Insert seed data on first launch
 *  - Retrieve two random riddles for each unlock attempt
 *  - Track which riddles have been shown for rotation logic
 *  - Update show-count and last-shown timestamp after each session
 */
@Dao
interface RiddleDao {

    /** Insert a list of riddles (used during initial database seeding). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(riddles: List<Riddle>)

    /** Returns total number of riddles in the database. */
    @Query("SELECT COUNT(*) FROM riddles")
    suspend fun count(): Int

    /** Get all riddles ordered by how often they have been shown (least-used first). */
    @Query("SELECT * FROM riddles ORDER BY times_shown ASC, last_shown_at ASC")
    suspend fun getAllOrderedByUsage(): List<Riddle>

    /** Get riddles filtered by difficulty level (1=Easy, 2=Medium, 3=Hard). */
    @Query("SELECT * FROM riddles WHERE difficulty <= :maxDifficulty ORDER BY times_shown ASC, last_shown_at ASC")
    suspend fun getByMaxDifficulty(maxDifficulty: Int): List<Riddle>

    /** Get all riddles as a LiveData flow for observation. */
    @Query("SELECT * FROM riddles ORDER BY id ASC")
    fun getAllFlow(): Flow<List<Riddle>>

    /** Get two least-recently-shown riddles of different types. */
    @Query("""
        SELECT * FROM riddles 
        WHERE difficulty <= :maxDifficulty
        ORDER BY times_shown ASC, last_shown_at ASC 
        LIMIT :limit
    """)
    suspend fun getLeastShown(maxDifficulty: Int, limit: Int = 10): List<Riddle>

    /** Get a riddle by its ID. */
    @Query("SELECT * FROM riddles WHERE id = :id")
    suspend fun getById(id: Int): Riddle?

    /** Update the usage tracking fields after a riddle has been shown. */
    @Query("""
        UPDATE riddles 
        SET times_shown = times_shown + 1, last_shown_at = :timestamp 
        WHERE id = :id
    """)
    suspend fun markShown(id: Int, timestamp: Long = System.currentTimeMillis())

    /** Reset all show counts — called when every riddle has been shown at least once. */
    @Query("UPDATE riddles SET times_shown = 0, last_shown_at = 0")
    suspend fun resetAllShowCounts()

    /** Get riddles belonging to a specific type (LOGIC, PATTERN, SEQUENCE, VISUAL, DEDUCTION). */
    @Query("SELECT * FROM riddles WHERE type = :type AND difficulty <= :maxDifficulty ORDER BY times_shown ASC LIMIT :limit")
    suspend fun getByType(type: String, maxDifficulty: Int, limit: Int = 5): List<Riddle>
}
