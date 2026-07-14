package com.iqlock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqlock.app.data.IQLockRepository
import com.iqlock.app.data.entity.Statistic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * StatisticsViewModel.kt — ViewModel for the StatisticsActivity.
 *
 * Provides:
 *  - [dailyStats] — today's stats for each protected app
 *  - [weeklyStats] — last 7 days of aggregate stats
 *  - [selectedPeriod] — toggle between DAILY and WEEKLY view
 *  - [summary] — aggregated totals for the currently selected period
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: IQLockRepository
) : ViewModel() {

    enum class Period { DAILY, WEEKLY }

    private val _selectedPeriod = MutableStateFlow(Period.DAILY)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    /** Stats from the last 7 days — always loaded. */
    val weeklyStats: StateFlow<List<Statistic>> = repository
        .statsFromDateFlow(repository.daysAgo(7))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Today-only stats. */
    val dailyStats: StateFlow<List<Statistic>> = repository
        .statsFromDateFlow(repository.today())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Stats for the currently selected period. */
    val currentStats: StateFlow<List<Statistic>> = _selectedPeriod.flatMapLatest { period ->
        when (period) {
            Period.DAILY -> repository.statsFromDateFlow(repository.today())
            Period.WEEKLY -> repository.statsFromDateFlow(repository.daysAgo(7))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Aggregated totals for the current period — shown in the summary card. */
    val summary: StateFlow<Summary> = currentStats
        .map { Summary.from(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Summary())

    fun selectPeriod(period: Period) {
        _selectedPeriod.value = period
    }

    data class Summary(
        val totalAttempts: Int = 0,
        val totalSuccesses: Int = 0,
        val totalFailures: Int = 0,
        val totalUsageMs: Long = 0L,
        val successRate: Int = 0
    ) {
        val totalUsageFormatted: String
            get() {
                val mins = (totalUsageMs / 60_000).toInt()
                return if (mins < 60) "${mins}m" else "${mins / 60}h ${mins % 60}m"
            }

        companion object {
            fun from(stats: List<Statistic>) = Summary(
                totalAttempts = stats.sumOf { it.unlockAttempts },
                totalSuccesses = stats.sumOf { it.successfulUnlocks },
                totalFailures = stats.sumOf { it.failedUnlocks },
                totalUsageMs = stats.sumOf { it.totalUsageTimeMs },
                successRate = if (stats.sumOf { it.unlockAttempts } > 0)
                    stats.sumOf { it.successfulUnlocks } * 100 / stats.sumOf { it.unlockAttempts }
                else 0
            )
        }
    }
}
