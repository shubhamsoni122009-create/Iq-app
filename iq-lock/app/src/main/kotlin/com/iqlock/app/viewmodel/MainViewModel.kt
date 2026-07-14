package com.iqlock.app.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iqlock.app.data.IQLockRepository
import com.iqlock.app.data.entity.AppSettings
import com.iqlock.app.data.entity.Statistic
import com.iqlock.app.data.entity.ProtectedApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainViewModel.kt — ViewModel for the main dashboard screen.
 *
 * Exposes:
 *  - [settings] — live settings (enable/disable master switch, etc.)
 *  - [protectedApps] — list of apps currently protected
 *  - [weeklyStats] — summarized weekly statistics for the dashboard
 *  - [isLockEnabled] — derived bool for the toggle switch
 *
 * Actions:
 *  - [toggleLockEnabled] — flip the master switch
 *  - [removeProtectedApp] — unprotect an app from the dashboard
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: IQLockRepository
) : AndroidViewModel(application) {

    private val pm: PackageManager = application.packageManager

    /** Live settings row — observed by the main screen toggle and header card. */
    val settings: StateFlow<AppSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** List of actively protected apps — shown in the home screen's protected list. */
    val protectedApps: StateFlow<List<ProtectedApp>> = repository.protectedAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Convenience bool derived from settings — drives the master toggle. */
    val isLockEnabled: StateFlow<Boolean> = settings
        .map { it?.isEnabled ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** Summary statistics: attempts, successes, failures for the last 7 days. */
    private val _weeklySummary = MutableStateFlow(WeeklySummary())
    val weeklySummary: StateFlow<WeeklySummary> = _weeklySummary.asStateFlow()

    init {
        viewModelScope.launch { repository.ensureDefaultSettings() }
        refreshWeeklySummary()
    }

    /** Toggle the global IQ Lock master switch. */
    fun toggleLockEnabled() {
        viewModelScope.launch {
            val current = settings.value?.isEnabled ?: true
            repository.setEnabled(!current)
        }
    }

    /** Remove an app from the protected list (called via swipe-to-delete on home screen). */
    fun removeProtectedApp(packageName: String) {
        viewModelScope.launch { repository.removeProtectedApp(packageName) }
    }

    /** Toggle the per-app lock without fully removing it. */
    fun toggleAppEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch { repository.setAppEnabled(packageName, enabled) }
    }

    /** Add an app to the protection list. */
    fun addProtectedApp(packageName: String, appLabel: String) {
        viewModelScope.launch {
            repository.addProtectedApp(
                com.iqlock.app.data.entity.ProtectedApp(
                    packageName = packageName,
                    appLabel = appLabel
                )
            )
        }
    }

    /** Reload weekly stats (called from onResume). */
    fun refreshWeeklySummary() {
        viewModelScope.launch {
            val stats = repository.statsFromDateFlow(repository.daysAgo(7))
                .first()
            _weeklySummary.value = WeeklySummary.from(stats)
        }
    }

    /** Attempt count for a given app label (used in the protected list row subtitle). */
    fun getAppLabel(packageName: String): String = try {
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    } catch (_: Exception) { packageName }

    data class WeeklySummary(
        val totalAttempts: Int = 0,
        val totalSuccesses: Int = 0,
        val totalFailures: Int = 0,
        val successRate: Int = 0           // 0–100 percentage
    ) {
        companion object {
            fun from(stats: List<Statistic>): WeeklySummary {
                val attempts = stats.sumOf { it.unlockAttempts }
                val successes = stats.sumOf { it.successfulUnlocks }
                val failures = stats.sumOf { it.failedUnlocks }
                val rate = if (attempts > 0) (successes * 100 / attempts) else 0
                return WeeklySummary(attempts, successes, failures, rate)
            }
        }
    }
}
