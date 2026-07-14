package com.iqlock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqlock.app.data.IQLockRepository
import com.iqlock.app.data.ThreeStageRiddleBank
import com.iqlock.app.data.ThreeStageRiddleBank.StageRiddle
import com.iqlock.app.data.entity.LockHistory
import com.iqlock.app.data.entity.LockOutcome
import com.iqlock.app.engine.MasterChallenge
import com.iqlock.app.engine.RelationshipEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ThreeStageViewModel.kt — Manages the complete 3-stage IQ unlock session.
 *
 * State machine:
 *  IDLE → STAGE_1 → STAGE_2 → STAGE_3 → SOLVED | LOCKED_OUT
 *
 * The ViewModel is kept across configuration changes (rotation, etc.),
 * so the timer and stage state survive screen rotations.
 *
 * Timer:
 *  - Each stage has its own independent countdown (default 90 seconds).
 *  - If the timer expires the user FAILS the current stage.
 *  - Max 2 wrong answers per stage before lockout.
 *
 * Lockout:
 *  - After exhausting attempts OR the timer across all stages, the app locks for
 *    [LOCKOUT_DURATION_MS] milliseconds.
 */
@HiltViewModel
class ThreeStageViewModel @Inject constructor(
    private val repository: IQLockRepository
) : ViewModel() {

    companion object {
        const val STAGE_TIMER_SECONDS = 90
        const val MAX_WRONG_PER_STAGE = 2
        const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L   // 5 minutes
    }

    // ── Sealed State ─────────────────────────────────────────────────────────

    sealed class SessionState {
        object Idle : SessionState()
        data class Stage1(val riddle: StageRiddle, val timerSeconds: Int, val wrongCount: Int) : SessionState()
        data class Stage2(val riddle: StageRiddle, val timerSeconds: Int, val wrongCount: Int) : SessionState()
        data class Stage3(val challenge: MasterChallenge, val timerSeconds: Int, val wrongCount: Int) : SessionState()
        object Solved : SessionState()
        data class LockedOut(val unlocksAt: Long) : SessionState()
        data class WrongAnswer(val stage: Int, val wrongCount: Int, val attemptsLeft: Int) : SessionState()
    }

    private val _state = MutableStateFlow<SessionState>(SessionState.Idle)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    // ── Session Data ─────────────────────────────────────────────────────────

    private var riddle1: StageRiddle? = null
    private var riddle2: StageRiddle? = null
    private var userAnswer1: String = ""
    private var userAnswer2: String = ""
    private var masterChallenge: MasterChallenge? = null

    private var historyId: Long = -1L
    var targetPackage: String = ""
    var targetLabel: String = ""

    /** Correct answer committed after Stage 1 — exposed so the Activity can display the recap. */
    var lastAnswer1: String = "—"
        private set

    /** Correct answer committed after Stage 2 — exposed so the Activity can display the recap. */
    var lastAnswer2: String = "—"
        private set

    /** Explanation text from the RelationshipEngine — shown on the Solved panel. */
    var lastExplanation: String = ""
        private set

    // ── Timer ─────────────────────────────────────────────────────────────────

    private var timerJob: Job? = null
    private var remainingSeconds = STAGE_TIMER_SECONDS
    private var currentStageWrong = 0

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Begin a new unlock session for [packageName].
     * Picks fresh riddles and starts Stage 1.
     */
    fun startSession(packageName: String, appLabel: String) {
        targetPackage = packageName
        targetLabel = appLabel

        viewModelScope.launch {
            // Check if still locked out from a previous failed session
            val activeLockout = repository.getActiveLockout(packageName)
            if (activeLockout != null && activeLockout.lockedUntil > System.currentTimeMillis()) {
                _state.value = SessionState.LockedOut(activeLockout.lockedUntil)
                return@launch
            }

            // Record unlock attempt
            repository.recordAttempt(packageName)

            // Log history row
            historyId = repository.insertLockEvent(
                LockHistory(
                    packageName = packageName,
                    appLabel = appLabel,
                    outcome = LockOutcome.PENDING.name
                )
            )

            // Pick one riddle from each bank
            riddle1 = ThreeStageRiddleBank.pickFromA(1).firstOrNull()
            riddle2 = ThreeStageRiddleBank.pickFromB(1).firstOrNull()

            if (riddle1 == null || riddle2 == null) {
                // Safety: shouldn't happen with a 25-riddle bank
                riddle1 = ThreeStageRiddleBank.BANK_A.random()
                riddle2 = ThreeStageRiddleBank.BANK_B.random()
            }

            startStage1()
        }
    }

    // ── Stage Transitions ─────────────────────────────────────────────────────

    private fun startStage1() {
        currentStageWrong = 0
        startTimer {
            // Timer expired on Stage 1 → lockout
            triggerLockout("Stage 1 timer expired")
        }
        _state.value = SessionState.Stage1(riddle1!!, remainingSeconds, currentStageWrong)
    }

    private fun startStage2() {
        currentStageWrong = 0
        startTimer {
            triggerLockout("Stage 2 timer expired")
        }
        _state.value = SessionState.Stage2(riddle2!!, remainingSeconds, currentStageWrong)
    }

    private fun startStage3() {
        currentStageWrong = 0
        val challenge = RelationshipEngine.generate(
            answer1 = userAnswer1,
            answer2 = userAnswer2,
            riddle1Question = riddle1!!.question,
            riddle2Question = riddle2!!.question
        )
        masterChallenge = challenge
        lastExplanation = challenge.explanation
        startTimer {
            triggerLockout("Stage 3 timer expired")
        }
        _state.value = SessionState.Stage3(challenge, remainingSeconds, currentStageWrong)
    }

    // ── Answer Submission ─────────────────────────────────────────────────────

    /**
     * Submit the user's typed answer for Stage 1 or Stage 2 (open-ended).
     */
    fun submitAnswer(input: String) {
        when (val s = _state.value) {
            is SessionState.Stage1 -> checkStage1(input)
            is SessionState.Stage2 -> checkStage2(input)
            else -> {} // ignore
        }
    }

    /**
     * Submit a multiple-choice selection for Stage 3.
     *
     * @param selectedIndex 0-based index of the user's chosen option.
     */
    fun submitStage3Choice(selectedIndex: Int) {
        val challenge = masterChallenge ?: return
        if (selectedIndex == challenge.correctIndex) {
            onSessionSolved()
        } else {
            onWrongAnswer(stage = 3)
        }
    }

    private fun checkStage1(input: String) {
        val r = riddle1 ?: return
        if (input.trim().equals(r.answer.trim(), ignoreCase = true)) {
            userAnswer1 = r.answer
            lastAnswer1 = r.answer
            timerJob?.cancel()
            startStage2()
        } else {
            onWrongAnswer(stage = 1)
        }
    }

    private fun checkStage2(input: String) {
        val r = riddle2 ?: return
        if (input.trim().equals(r.answer.trim(), ignoreCase = true)) {
            userAnswer2 = r.answer
            lastAnswer2 = r.answer
            timerJob?.cancel()
            startStage3()
        } else {
            onWrongAnswer(stage = 2)
        }
    }

    private fun onWrongAnswer(stage: Int) {
        currentStageWrong++
        val attemptsLeft = MAX_WRONG_PER_STAGE - currentStageWrong

        _state.value = SessionState.WrongAnswer(
            stage = stage,
            wrongCount = currentStageWrong,
            attemptsLeft = attemptsLeft
        )

        if (attemptsLeft <= 0) {
            triggerLockout("Exhausted attempts on Stage $stage")
        } else {
            // Resume same stage (timer already running)
            viewModelScope.launch {
                delay(1_500)     // Show "wrong" feedback briefly
                resumeCurrentStage(stage)
            }
        }
    }

    private fun resumeCurrentStage(stage: Int) {
        when (stage) {
            1 -> _state.value = SessionState.Stage1(riddle1!!, remainingSeconds, currentStageWrong)
            2 -> _state.value = SessionState.Stage2(riddle2!!, remainingSeconds, currentStageWrong)
            3 -> _state.value = SessionState.Stage3(masterChallenge!!, remainingSeconds, currentStageWrong)
        }
    }

    // ── Lockout / Solved ──────────────────────────────────────────────────────

    private fun onSessionSolved() {
        timerJob?.cancel()
        viewModelScope.launch {
            repository.recordSuccess(targetPackage)
            val event = repository.getLockEvent(historyId.toInt())
            event?.let {
                repository.updateLockEvent(
                    it.copy(
                        outcome = LockOutcome.SOLVED.name,
                        solvedAt = System.currentTimeMillis(),
                        timeTakenMs = System.currentTimeMillis() - it.attemptedAt
                    )
                )
            }
        }
        _state.value = SessionState.Solved
    }

    private fun triggerLockout(reason: String) {
        timerJob?.cancel()
        val unlocksAt = System.currentTimeMillis() + LOCKOUT_DURATION_MS
        viewModelScope.launch {
            repository.recordFailure(targetPackage)
            val event = repository.getLockEvent(historyId.toInt())
            event?.let {
                repository.updateLockEvent(
                    it.copy(
                        outcome = LockOutcome.FAILED.name,
                        lockedUntil = unlocksAt
                    )
                )
            }
            // Insert an active lockout record so the accessibility service can detect it
            repository.insertLockEvent(
                LockHistory(
                    packageName = targetPackage,
                    appLabel = targetLabel,
                    outcome = LockOutcome.FAILED.name,
                    lockedUntil = unlocksAt
                )
            )
        }
        _state.value = SessionState.LockedOut(unlocksAt)
    }

    fun cancelSession() {
        timerJob?.cancel()
        viewModelScope.launch {
            val event = repository.getLockEvent(historyId.toInt())
            event?.let {
                repository.updateLockEvent(it.copy(outcome = LockOutcome.CANCELLED.name))
            }
        }
        _state.value = SessionState.Idle
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private fun startTimer(onExpired: () -> Unit) {
        timerJob?.cancel()
        remainingSeconds = STAGE_TIMER_SECONDS
        timerJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1_000)
                remainingSeconds--
                // Refresh the displayed timer in the current state
                refreshTimerInState()
            }
            onExpired()
        }
    }

    private fun refreshTimerInState() {
        when (val s = _state.value) {
            is SessionState.Stage1 ->
                _state.value = s.copy(timerSeconds = remainingSeconds)
            is SessionState.Stage2 ->
                _state.value = s.copy(timerSeconds = remainingSeconds)
            is SessionState.Stage3 ->
                _state.value = s.copy(timerSeconds = remainingSeconds)
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
