package com.iqlock.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.iqlock.app.data.AppDatabase
import com.iqlock.app.data.IQLockRepository
import com.iqlock.app.data.entity.AppSettings
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * IQLockApplication.kt — Application class, entry point for Hilt dependency injection.
 *
 * Responsibilities:
 *  1. Initialize Hilt via @HiltAndroidApp
 *  2. Seed the Room database with 100 riddles on first launch
 *  3. Insert the default AppSettings row if none exists
 *  4. Apply the saved dark-mode preference at startup
 *
 * The [applicationScope] coroutine scope is tied to the process lifetime.
 * It survives Activity/Fragment recreation and is used for fire-and-forget
 * initialization work that must outlive any single screen.
 */
@HiltAndroidApp
class IQLockApplication : Application() {

    /** Process-lifetime scope — NOT cancelled when any individual screen dies. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { initializeDatabase() }
    }

    /**
     * Seeds the database with riddles and default settings on first launch.
     * Room's IGNORE conflict strategy ensures these inserts are no-ops on
     * subsequent launches.
     */
    private suspend fun initializeDatabase() {
        val db = AppDatabase.getInstance(this)

        // Insert default settings (IGNORE on conflict — id=1 only ever exists once)
        db.appSettingsDao().insert(AppSettings())

        // Seed riddles if this is the first launch
        if (db.riddleDao().count() == 0) {
            db.riddleDao().insertAll(RiddleData.all())
        }

        // Apply saved dark mode from settings
        val darkMode = db.appSettingsDao().get()?.darkMode ?: 0
        applyDarkMode(darkMode)
    }

    /** Apply the AppCompatDelegate night mode matching the saved preference. */
    fun applyDarkMode(mode: Int) {
        val delegate = when (mode) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        // Must run on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            AppCompatDelegate.setDefaultNightMode(delegate)
        }
    }
}
