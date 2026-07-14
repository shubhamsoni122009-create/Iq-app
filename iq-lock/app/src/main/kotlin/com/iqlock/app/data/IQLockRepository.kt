package com.iqlock.app.data

import com.iqlock.app.data.dao.*
import com.iqlock.app.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * IQLockRepository.kt — Single source of truth for all data access.
 *
 * All ViewModels interact with data through this class only.
 * Heavy operations run on Dispatchers.IO to avoid blocking the main thread.
 * Exposes Flows for reactive UI updates and suspend functions for one-shot reads/writes.
 */
@Singleton
class IQLockRepository @Inject constructor(
    private val riddleDao: RiddleDao,
    private val statisticDao: StatisticDao,
    private val lockHistoryDao: LockHistoryDao,
    private val protectedAppDao: ProtectedAppDao,
    private val appSettingsDao: AppSettingsDao
) {
    // ── Date Helper ─────────────────────────────────────────────────────────
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    fun today(): String = dateFormat.format(Date())
    fun daysAgo(n: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -n) }
        return dateFormat.format(cal.time)
    }

    // ── Settings ─────────────────────────────────────────────────────────────
    val settingsFlow: Flow<AppSettings?> = appSettingsDao.getFlow()

    suspend fun getSettings(): AppSettings = withContext(Dispatchers.IO) {
        appSettingsDao.get() ?: AppSettings().also { appSettingsDao.insert(it) }
    }

    suspend fun ensureDefaultSettings() = withContext(Dispatchers.IO) {
        appSettingsDao.insert(AppSettings())     // IGNORE if already exists
    }

    suspend fun setEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettingsDao.setEnabled(enabled)
    }

    suspend fun setUnlockTimer(seconds: Int) = withContext(Dispatchers.IO) {
        appSettingsDao.setUnlockTimer(seconds)
    }

    suspend fun setLockDuration(minutes: Int) = withContext(Dispatchers.IO) {
        appSettingsDao.setLockDuration(minutes)
    }

    suspend fun setDifficulty(difficulty: Int) = withContext(Dispatchers.IO) {
        appSettingsDao.setDifficulty(difficulty)
    }

    suspend fun setDarkMode(darkMode: Int) = withContext(Dispatchers.IO) {
        appSettingsDao.setDarkMode(darkMode)
    }

    suspend fun setShowHints(show: Boolean) = withContext(Dispatchers.IO) {
        appSettingsDao.setShowHints(show)
    }

    // ── Protected Apps ────────────────────────────────────────────────────────
    val protectedAppsFlow: Flow<List<ProtectedApp>> = protectedAppDao.getAllFlow()

    suspend fun addProtectedApp(app: ProtectedApp) = withContext(Dispatchers.IO) {
        protectedAppDao.insert(app)
    }

    suspend fun removeProtectedApp(packageName: String) = withContext(Dispatchers.IO) {
        protectedAppDao.deleteByPackage(packageName)
    }

    suspend fun setAppEnabled(packageName: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        protectedAppDao.setEnabled(packageName, enabled)
    }

    suspend fun getEnabledProtectedPackages(): Set<String> = withContext(Dispatchers.IO) {
        protectedAppDao.getEnabledPackageNames().toSet()
    }

    suspend fun isProtected(packageName: String): Boolean = withContext(Dispatchers.IO) {
        protectedAppDao.isProtected(packageName)
    }

    // ── Riddles ───────────────────────────────────────────────────────────────
    val allRiddlesFlow: Flow<List<Riddle>> = riddleDao.getAllFlow()

    suspend fun seedRiddles(riddles: List<Riddle>) = withContext(Dispatchers.IO) {
        if (riddleDao.count() == 0) riddleDao.insertAll(riddles)
    }

    /**
     * Pick [count] riddles for a challenge session, selecting from different types
     * and preferring least-shown riddles. Marks them as shown afterward.
     */
    suspend fun pickRiddlesForChallenge(maxDifficulty: Int, count: Int = 2): List<Riddle> =
        withContext(Dispatchers.IO) {
            val pool = riddleDao.getByMaxDifficulty(maxDifficulty)

            // If all riddles have been shown, reset counts and start fresh
            if (pool.all { it.timesShown > 0 }) {
                riddleDao.resetAllShowCounts()
            }

            // Try to pick riddles from different types
            val types = RiddleType.values().toMutableList().shuffled()
            val picked = mutableListOf<Riddle>()
            for (type in types) {
                if (picked.size >= count) break
                val candidate = pool
                    .filter { it.type == type.name && it !in picked }
                    .minByOrNull { it.timesShown }
                candidate?.let { picked.add(it) }
            }
            // Fill remaining slots if not enough types
            if (picked.size < count) {
                val remainder = pool.filter { it !in picked }.sortedBy { it.timesShown }
                picked.addAll(remainder.take(count - picked.size))
            }

            // Mark selected riddles as shown
            picked.forEach { riddleDao.markShown(it.id) }
            picked.shuffled()
        }

    // ── Lock History ──────────────────────────────────────────────────────────
    val lockHistoryFlow: Flow<List<LockHistory>> = lockHistoryDao.getAllFlow()

    suspend fun insertLockEvent(event: LockHistory): Long = withContext(Dispatchers.IO) {
        lockHistoryDao.insert(event)
    }

    suspend fun updateLockEvent(event: LockHistory) = withContext(Dispatchers.IO) {
        lockHistoryDao.update(event)
    }

    suspend fun getLockEvent(id: Int): LockHistory? = withContext(Dispatchers.IO) {
        lockHistoryDao.getById(id)
    }

    suspend fun getActiveLockout(packageName: String): LockHistory? = withContext(Dispatchers.IO) {
        lockHistoryDao.getActiveLockout(packageName, System.currentTimeMillis())
    }

    // ── Statistics ────────────────────────────────────────────────────────────
    fun statsFromDateFlow(fromDate: String): Flow<List<Statistic>> =
        statisticDao.getStatsFromDateFlow(fromDate)

    suspend fun recordAttempt(packageName: String) = withContext(Dispatchers.IO) {
        val date = today()
        val existing = statisticDao.getForDate(packageName, date)
        if (existing == null) {
            statisticDao.insert(Statistic(packageName = packageName, date = date, unlockAttempts = 1))
        } else {
            statisticDao.incrementAttempts(packageName, date)
        }
    }

    suspend fun recordSuccess(packageName: String) = withContext(Dispatchers.IO) {
        val date = today()
        val existing = statisticDao.getForDate(packageName, date)
        if (existing == null) {
            statisticDao.insert(Statistic(packageName = packageName, date = date, successfulUnlocks = 1))
        } else {
            statisticDao.incrementSuccesses(packageName, date)
        }
    }

    suspend fun recordFailure(packageName: String) = withContext(Dispatchers.IO) {
        val date = today()
        val existing = statisticDao.getForDate(packageName, date)
        if (existing == null) {
            statisticDao.insert(Statistic(packageName = packageName, date = date, failedUnlocks = 1))
        } else {
            statisticDao.incrementFailures(packageName, date)
        }
    }

    suspend fun addUsageTime(packageName: String, ms: Long) = withContext(Dispatchers.IO) {
        val date = today()
        val rows = statisticDao.addUsageTime(packageName, date, ms)
        if (rows == 0) {
            statisticDao.insert(Statistic(packageName = packageName, date = date, totalUsageTimeMs = ms))
        }
    }

    suspend fun getWeeklyStats(): List<Statistic> = withContext(Dispatchers.IO) {
        statisticDao.getAllForDate(daysAgo(7))
    }
}
