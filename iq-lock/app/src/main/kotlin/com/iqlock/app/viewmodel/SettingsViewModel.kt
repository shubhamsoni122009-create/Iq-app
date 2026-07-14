package com.iqlock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqlock.app.data.IQLockRepository
import com.iqlock.app.data.entity.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SettingsViewModel.kt — ViewModel for the SettingsActivity.
 *
 * Exposes the current settings as a StateFlow. Each preference has a
 * corresponding setter that persists to Room through the repository.
 *
 * Settings managed here:
 *  - Unlock timer (30–300 seconds)
 *  - Lock duration (1–60 minutes after failure)
 *  - Difficulty (1=Easy, 2=Medium, 3=Hard)
 *  - Dark mode (0=System, 1=Light, 2=Dark)
 *  - Show hints (boolean)
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: IQLockRepository
) : ViewModel() {

    /** Live settings — drives all UI controls on the settings screen. */
    val settings: StateFlow<AppSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Update unlock timer duration. Valid range: 30–300 seconds. */
    fun setUnlockTimer(seconds: Int) {
        viewModelScope.launch {
            repository.setUnlockTimer(seconds.coerceIn(30, 300))
        }
    }

    /** Update lockout duration after failed attempts. Valid range: 1–60 minutes. */
    fun setLockDuration(minutes: Int) {
        viewModelScope.launch {
            repository.setLockDuration(minutes.coerceIn(1, 60))
        }
    }

    /** Update riddle difficulty. 1=Easy (all riddles), 2=Medium, 3=Hard only. */
    fun setDifficulty(difficulty: Int) {
        viewModelScope.launch {
            repository.setDifficulty(difficulty.coerceIn(1, 3))
        }
    }

    /** Update dark mode preference. 0=System, 1=Light, 2=Dark. */
    fun setDarkMode(darkMode: Int) {
        viewModelScope.launch {
            repository.setDarkMode(darkMode.coerceIn(0, 2))
        }
    }

    /** Toggle whether a hint is shown after 40 seconds. */
    fun setShowHints(show: Boolean) {
        viewModelScope.launch {
            repository.setShowHints(show)
        }
    }
}
