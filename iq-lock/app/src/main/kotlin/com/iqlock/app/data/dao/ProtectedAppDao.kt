package com.iqlock.app.data.dao

import androidx.room.*
import com.iqlock.app.data.entity.ProtectedApp
import kotlinx.coroutines.flow.Flow

/**
 * ProtectedAppDao.kt — DAO for the list of apps chosen by the user for protection.
 *
 * The accessibility service calls getEnabledPackageNames() on every foreground
 * change event, so that query must be fast (indexed on package_name PK).
 */
@Dao
interface ProtectedAppDao {

    /** Add a new app to the protection list. IGNORE if already present. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(app: ProtectedApp)

    /** Update an existing entry (e.g., toggling isEnabled, refreshing the icon). */
    @Update
    suspend fun update(app: ProtectedApp)

    /** Remove a single app from protection by its package name. */
    @Query("DELETE FROM protected_apps WHERE package_name = :packageName")
    suspend fun deleteByPackage(packageName: String)

    /** Remove all apps from the protection list. */
    @Query("DELETE FROM protected_apps")
    suspend fun deleteAll()

    /**
     * Returns only the package names of actively-enabled protected apps.
     * This is the hot-path called by the accessibility service — returns a
     * simple Set<String> for O(1) lookup, not a Flow.
     */
    @Query("SELECT package_name FROM protected_apps WHERE is_enabled = 1")
    suspend fun getEnabledPackageNames(): List<String>

    /** Check whether a specific package is currently protected and enabled. */
    @Query("SELECT COUNT(*) > 0 FROM protected_apps WHERE package_name = :packageName AND is_enabled = 1")
    suspend fun isProtected(packageName: String): Boolean

    /** Get a specific protected app entry by package name. */
    @Query("SELECT * FROM protected_apps WHERE package_name = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): ProtectedApp?

    /**
     * Get all protected apps as a Flow for live UI updates.
     * Ordered by app label for easy reading.
     */
    @Query("SELECT * FROM protected_apps ORDER BY app_label ASC")
    fun getAllFlow(): Flow<List<ProtectedApp>>

    /** Toggle the isEnabled flag for a specific app. */
    @Query("""
        UPDATE protected_apps 
        SET is_enabled = :enabled 
        WHERE package_name = :packageName
    """)
    suspend fun setEnabled(packageName: String, enabled: Boolean)
}
