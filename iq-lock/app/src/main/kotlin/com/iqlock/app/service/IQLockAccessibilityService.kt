package com.iqlock.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.iqlock.app.data.AppDatabase
import com.iqlock.app.ui.ThreeStageLockActivity
import kotlinx.coroutines.*

/**
 * IQLockAccessibilityService.kt — The core engine of IQ Lock.
 *
 * This service is declared in AndroidManifest.xml and enabled by the user via
 * Settings → Accessibility → IQ Lock. Once enabled, the Android system invokes
 * [onAccessibilityEvent] every time the foreground app changes.
 *
 * Detection logic:
 *  1. Listen for AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED events.
 *  2. Extract the package name from the event.
 *  3. Skip our own package, launchers, and system UI.
 *  4. Query the Room database for the list of enabled-protected packages.
 *  5. If the package is protected:
 *     a. Check if it is currently in a lockout period (failed max attempts).
 *     b. If locked out, re-show the LockScreenActivity with the lockout state.
 *     c. Otherwise, launch LockScreenActivity as a foreground overlay.
 *
 * Performance considerations:
 *  - The protected-package set is cached and refreshed every 5 seconds to avoid
 *    hitting the database on every event (which can fire dozens of times per second).
 *  - All database access runs on Dispatchers.IO.
 *  - The coroutine scope is tied to the service lifecycle.
 */
class IQLockAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Cached protected package set ─────────────────────────────────────────
    private var cachedProtectedPackages: Set<String> = emptySet()
    private var cacheTimestampMs: Long = 0L
    private val CACHE_TTL_MS = 5_000L          // refresh cache every 5 seconds

    // Packages to always ignore (system UI, recents, launcher, our own app)
    private val IGNORED_PACKAGES = setOf(
        "com.android.systemui",
        "com.android.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.miui.home",
        "com.samsung.android.app.launcher",
        "com.oneplus.launcher",
        "com.iqlock.app"   // Never lock our own UI
    )

    // Track the last app we showed a lock screen for (avoid repeated launches)
    private var lastLockedPackage: String = ""
    private var lastLockShowTimeMs: Long = 0L
    private val LOCK_COOLDOWN_MS = 2_000L      // don't re-trigger for same app within 2 seconds

    // ── AccessibilityService callbacks ────────────────────────────────────────

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Pre-load the protected packages immediately
        refreshProtectedPackages()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName in IGNORED_PACKAGES) return
        if (packageName.startsWith("com.android.") && packageName != "com.android.chrome") return

        // Check if global lock is enabled
        serviceScope.launch {
            handleForegroundChange(packageName)
        }
    }

    override fun onInterrupt() {
        // Required override — no-op for IQ Lock
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    // ── Core Logic ─────────────────────────────────────────────────────────────

    private suspend fun handleForegroundChange(packageName: String) {
        val db = AppDatabase.getInstance(applicationContext)

        // Check the global master switch first (fast path)
        val settings = db.appSettingsDao().get() ?: return
        if (!settings.isEnabled) return

        // Refresh cached protected packages if TTL expired
        val now = System.currentTimeMillis()
        if (now - cacheTimestampMs > CACHE_TTL_MS) {
            refreshProtectedPackages()
        }

        // Is this package protected?
        if (packageName !in cachedProtectedPackages) return

        // Cooldown: avoid spamming the lock screen for the same app rapidly
        if (packageName == lastLockedPackage && now - lastLockShowTimeMs < LOCK_COOLDOWN_MS) return

        // Check for an active lockout
        val activeLockout = db.lockHistoryDao().getActiveLockout(packageName, now)

        // Update tracking
        lastLockedPackage = packageName
        lastLockShowTimeMs = now

        // Retrieve app label for display
        val appLabel = try {
            applicationContext.packageManager
                .getApplicationLabel(
                    applicationContext.packageManager.getApplicationInfo(packageName, 0)
                ).toString()
        } catch (_: Exception) { packageName }

        // Launch the lock screen overlay
        showLockScreen(packageName, appLabel)
    }

    private fun showLockScreen(packageName: String, appLabel: String) {
        val intent = Intent(applicationContext, ThreeStageLockActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(ThreeStageLockActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(ThreeStageLockActivity.EXTRA_APP_LABEL, appLabel)
        }
        applicationContext.startActivity(intent)
    }

    private fun refreshProtectedPackages() {
        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            cachedProtectedPackages = db.protectedAppDao().getEnabledPackageNames().toSet()
            cacheTimestampMs = System.currentTimeMillis()
        }
    }
}
