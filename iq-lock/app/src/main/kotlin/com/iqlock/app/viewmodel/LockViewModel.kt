package com.iqlock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqlock.app.data.IQLockRepository
import com.iqlock.app.data.entity.LockHistory
import com.iqlock.app.data.entity.LockOutcome
import com.iqlock.app.data.entity.Riddle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * LockViewModel.kt — ViewModel for the LockScreenActivity.
 *
 * Manages:
 *  - Fetching the pair of riddles for this unlock session
 *  - The 75-second countdown timer (one per attempt)
 *  - Attempt tracking (max 2 attempts before full lockout)
 *  - Riddle navigation (riddle 1 → riddle 2 within each attempt)
 *  - Answer validation
 *  - Recording outcomes to the database
 *
 * State machine:
 *   LOADING → SHOWING_RIDDLE_1 → SHOWING_RIDDLE_2
 *     → SUCCESS (both correct)
 *     → WRONG_ANSWER (incorrect on current riddle, same attempt)
 *     → FAILED_ATTEMPT (timer expired or wrong on riddle 2, attempts remain)
 *     → LOCKED_OUT (all attempts exhausted)
 */
@HiltViewModel
class LockViewModel @Inject constructor(
    private val repository: IQLockRepository
) : ViewModel() {

    // ── Challenge State ───────────────────────────────────────────────────────
    sealed class ChallengeState {
        object Loading : ChallengeState()
        data class ShowingRiddle(val riddleIndex: Int) : ChallengeState()   // 0 or 1
        object Success : ChallengeState()
        data class WrongAnswer(val riddleIndex: Int) : ChallengeState()
        data class FailedAttempt(val attemptsRemaining: Int) : ChallengeState()
        data class LockedOut(val lockedUntilMs: Long) : ChallengeState()
        object Cancelled : ChallengeState()
    }

    private val _state = MutableStateFlow<ChallengeState>(ChallengeState.Loading)
    val state: StateFlow<ChallengeState> = _state.asStateFlow()

    private val _riddles = MutableStateFlow<List<Riddle>>(emptyList())
    val riddles: StateFlow<List<Riddle>> = _riddles.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(75)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _attemptsUsed = MutableStateFlow(0)
    val attemptsUsed: StateFlow<Int> = _attemptsUsed.asStateFlow()

    private val _currentRiddleIndex = MutableStateFlow(0)
    val currentRiddleIndex: StateFlow<Int> = _currentRiddleIndex.asStateFlow()

    // First riddle answer state (for two-riddle display)
    private val _riddleOneAnswered = MutableStateFlow(false)
    val riddleOneAnswered: StateFlow<Boolean> = _riddleOneAnswered.asStateFlow()

    // ── Internal session data ─────────────────────────────────────────────────
    private var packageName: String = ""
    private var appLabel: String = ""
    private var maxAttempts: Int = 2
    private var timerDurationSeconds: Int = 75
    private var lockDurationMinutes: Int = 5
    private var historyId: Int = 0
    private var timerJob: Job? = null

    // ── Session Initialization ────────────────────────────────────────────────

    /** Called by LockScreenActivity immediately after creation. */
    fun startSession(packageName: String, appLabel: String) {
        this.packageName = packageName
        this.appLabel = appLabel
        viewModelScope.launch {
            val settings = repository.getSettings()
            maxAttempts = settings.maxAttempts
            timerDurationSeconds = settings.unlockTimerSeconds
            lockDurationMinutes = settings.lockDurationMinutes

            // Record the attempt event
            repository.recordAttempt(packageName)
            val id = repository.insertLockEvent(
                LockHistory(
                    packageName = packageName,
                    appLabel = appLabel,
                    outcome = LockOutcome.PENDING.name
                )
            )
            historyId = id.toInt()

            // Load riddles for this session
            val picked = repository.pickRiddlesForChallenge(settings.difficulty)
            _riddles.value = picked
            _attemptsUsed.value = 0

            startAttempt()
        }
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private fun startAttempt() {
        timerJob?.cancel()
        _currentRiddleIndex.value = 0
        _riddleOneAnswered.value = false
        _timeRemainingSeconds.value = timerDurationSeconds
        _state.value = ChallengeState.ShowingRiddle(0)
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            repeat(timerDurationSeconds) {
                delay(1_000)
                val remaining = _timeRemainingSeconds.value - 1
                _timeRemainingSeconds.value = remaining
                if (remaining <= 0) {
                    handleTimeout()
                    return@launch
                }
            }
        }
    }

    private fun handleTimeout() {
        viewModelScope.launch { handleAttemptFailed() }
    }

    // ── Answer Checking ────────────────────────────────────────────────────────

    /**
     * Called when the user submits an answer.
     * @param userInput The text the user entered or the option they selected.
     */
    fun submitAnswer(userInput: String) {
        val index = _currentRiddleIndex.value
        val riddle = _riddles.value.getOrNull(index) ?: return

        if (riddle.isCorrect(userInput)) {
            when (index) {
                0 -> {
                    // First riddle answered correctly — move to second
                    _riddleOneAnswered.value = true
                    _currentRiddleIndex.value = 1
                    _state.value = ChallengeState.ShowingRiddle(1)
                }
                1 -> {
                    // Both riddles answered correctly — success!
                    timerJob?.cancel()
                    viewModelScope.launch { handleSuccess() }
                }
            }
        } else {
            // Wrong answer on this riddle
            _state.value = ChallengeState.WrongAnswer(index)
            // After brief delay the UI resets input; attempt is NOT consumed yet
            // The user must exhaust the timer or give up to consume an attempt
        }
    }

    /** Called when the user explicitly gives up on an attempt. */
    fun forfeitAttempt() {
        timerJob?.cancel()
        viewModelScope.launch { handleAttemptFailed() }
    }

    // ── Outcome Handlers ──────────────────────────────────────────────────────

    private suspend fun handleSuccess() {
        repository.recordSuccess(packageName)
        val event = repository.getLockEvent(historyId)?.copy(
            outcome = LockOutcome.SOLVED.name,
            solvedAt = System.currentTimeMillis(),
            timeTakenMs = ((timerDurationSeconds - _timeRemainingSeconds.value) * 1_000L)
        )
        event?.let { repository.updateLockEvent(it) }
        _state.value = ChallengeState.Success
    }

    private suspend fun handleAttemptFailed() {
        val used = _attemptsUsed.value + 1
        _attemptsUsed.value = used

        if (used >= maxAttempts) {
            // All attempts exhausted — lock the app
            repository.recordFailure(packageName)
            val lockedUntil = System.currentTimeMillis() + (lockDurationMinutes * 60_000L)
            val event = repository.getLockEvent(historyId)?.copy(
                outcome = LockOutcome.FAILED.name,
                lockedUntil = lockedUntil,
                timeTakenMs = (timerDurationSeconds * 1_000L)
            )
            event?.let { repository.updateLockEvent(it) }
            _state.value = ChallengeState.LockedOut(lockedUntil)
        } else {
            // Still have attempts remaining — offer retry with new riddles
            val remaining = maxAttempts - used
            _state.value = ChallengeState.FailedAttempt(remaining)
        }
    }

    /** Called by the UI to begin a new attempt after a FailedAttempt state. */
    fun retryWithNewRiddles() {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val newRiddles = repository.pickRiddlesForChallenge(settings.difficulty)
            _riddles.value = newRiddles
            startAttempt()
        }
    }

    /** Cancel the lock — called when user presses back or IQ Lock is globally disabled. */
    fun cancelSession() {
        timerJob?.cancel()
        viewModelScope.launch {
            val event = repository.getLockEvent(historyId)?.copy(
                outcome = LockOutcome.CANCELLED.name
            )
            event?.let { repository.updateLockEvent(it) }
        }
        _state.value = ChallengeState.Cancelled
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
