package com.iqlock.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.iqlock.app.R
import com.iqlock.app.data.entity.Riddle
import com.iqlock.app.databinding.ActivityLockScreenBinding
import com.iqlock.app.viewmodel.LockViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * LockScreenActivity.kt — The challenge screen shown over a protected app.
 *
 * Launched by IQLockAccessibilityService when a protected app comes to the foreground.
 * Uses FLAG_SHOW_WHEN_LOCKED and FLAG_KEEP_SCREEN_ON to appear even on the lock screen.
 *
 * Flow:
 *  1. Service passes packageName + appLabel via Intent extras.
 *  2. ViewModel selects 2 riddles and starts the countdown.
 *  3. Riddle 1 displayed → user answers → Riddle 2 displayed → user answers.
 *  4. Both correct within 75s → SUCCESS → finish() (user enters the app).
 *  5. Timer expires or user forfeits → FAILED_ATTEMPT (retry if attempts remain).
 *  6. All attempts exhausted → LOCKED_OUT state → show countdown to unlock.
 */
@AndroidEntryPoint
class LockScreenActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_LABEL    = "extra_app_label"

        /** Broadcast sent to close the lock screen when the user exits the protected app. */
        const val ACTION_DISMISS_LOCK = "com.iqlock.app.DISMISS_LOCK"
    }

    private lateinit var binding: ActivityLockScreenBinding
    private val viewModel: LockViewModel by viewModels()

    private var hintTimer: CountDownTimer? = null
    private var lockoutTimer: CountDownTimer? = null

    private val dismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_DISMISS_LOCK) finish()
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen and keep screen on
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prevent back-press from bypassing the lock
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* blocked */ }
        })

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: run { finish(); return }
        val appLabel    = intent.getStringExtra(EXTRA_APP_LABEL) ?: packageName

        binding.tvLockedApp.text = getString(R.string.locked_app_label, appLabel)

        viewModel.startSession(packageName, appLabel)

        setupAnswerButtons()
        observeViewModel()
        registerDismissReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        hintTimer?.cancel()
        lockoutTimer?.cancel()
        try { unregisterReceiver(dismissReceiver) } catch (_: Exception) {}
    }

    // ── UI Setup ──────────────────────────────────────────────────────────────

    private fun setupAnswerButtons() {
        // The 4 option chips are bound dynamically in showRiddle()
        binding.btnForfeit.setOnClickListener {
            viewModel.forfeitAttempt()
        }
        binding.etOpenAnswer.setOnEditorActionListener { _, _, _ ->
            submitOpenAnswer()
            true
        }
        binding.btnSubmitAnswer.setOnClickListener { submitOpenAnswer() }
    }

    private fun submitOpenAnswer() {
        val text = binding.etOpenAnswer.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return
        binding.etOpenAnswer.text?.clear()
        viewModel.submitAnswer(text)
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state -> handleState(state) }
        }

        lifecycleScope.launch {
            viewModel.timeRemainingSeconds.collect { seconds ->
                binding.tvTimer.text = formatSeconds(seconds)
                // Change timer color to red when <= 15 seconds
                val color = if (seconds <= 15)
                    getColor(R.color.error_red)
                else
                    getColor(R.color.purple_200)
                binding.tvTimer.setTextColor(color)
            }
        }

        lifecycleScope.launch {
            viewModel.attemptsUsed.collect { used ->
                binding.tvAttemptNumber.text = getString(R.string.attempt_number, used + 1)
            }
        }

        lifecycleScope.launch {
            viewModel.riddleOneAnswered.collect { answered ->
                binding.tvRiddle1Status.visibility = if (answered) View.VISIBLE else View.GONE
            }
        }
    }

    private fun handleState(state: LockViewModel.ChallengeState) {
        when (state) {
            is LockViewModel.ChallengeState.Loading -> showLoadingPanel()

            is LockViewModel.ChallengeState.ShowingRiddle -> {
                val riddle = viewModel.riddles.value.getOrNull(state.riddleIndex)
                riddle?.let { showRiddle(it, state.riddleIndex) }
                binding.tvRiddleNumber.text =
                    getString(R.string.riddle_number, state.riddleIndex + 1, 2)
            }

            is LockViewModel.ChallengeState.WrongAnswer -> {
                Toast.makeText(this, R.string.wrong_answer, Toast.LENGTH_SHORT).show()
                binding.etOpenAnswer.text?.clear()
                // Clear chip selection
                binding.chipGroupOptions.clearCheck()
            }

            is LockViewModel.ChallengeState.FailedAttempt -> {
                showFailedPanel(state.attemptsRemaining)
            }

            is LockViewModel.ChallengeState.LockedOut -> {
                showLockedOutPanel(state.lockedUntilMs)
            }

            is LockViewModel.ChallengeState.Success -> {
                showSuccessPanel()
            }

            is LockViewModel.ChallengeState.Cancelled -> finish()
        }
    }

    // ── Panel Switchers ───────────────────────────────────────────────────────

    private fun showLoadingPanel() {
        binding.panelRiddle.visibility = View.GONE
        binding.panelFailed.visibility = View.GONE
        binding.panelLockedOut.visibility = View.GONE
        binding.panelSuccess.visibility = View.GONE
        binding.progressLoading.visibility = View.VISIBLE
    }

    private fun showRiddle(riddle: Riddle, index: Int) {
        binding.progressLoading.visibility = View.GONE
        binding.panelFailed.visibility = View.GONE
        binding.panelLockedOut.visibility = View.GONE
        binding.panelSuccess.visibility = View.GONE
        binding.panelRiddle.visibility = View.VISIBLE

        binding.tvQuestion.text = riddle.question

        // Show hint label (initially hidden — revealed by hint timer)
        binding.tvHint.visibility = View.GONE
        binding.tvHint.text = riddle.hint

        hintTimer?.cancel()
        if (riddle.hint.isNotBlank()) {
            hintTimer = object : CountDownTimer(40_000, 1_000) {
                override fun onTick(ms: Long) {}
                override fun onFinish() {
                    binding.tvHint.visibility = View.VISIBLE
                }
            }.start()
        }

        val options = riddle.optionList()
        if (options.isNotEmpty()) {
            // Multiple-choice mode
            binding.chipGroupOptions.visibility = View.VISIBLE
            binding.layoutOpenAnswer.visibility = View.GONE
            binding.chipGroupOptions.removeAllViews()
            options.forEach { opt ->
                val chip = layoutInflater.inflate(
                    R.layout.item_option_chip, binding.chipGroupOptions, false
                ) as Chip
                chip.text = opt
                chip.setOnClickListener { viewModel.submitAnswer(opt) }
                binding.chipGroupOptions.addView(chip)
            }
        } else {
            // Open-ended mode
            binding.chipGroupOptions.visibility = View.GONE
            binding.layoutOpenAnswer.visibility = View.VISIBLE
            binding.etOpenAnswer.hint = getString(R.string.type_your_answer)
        }
    }

    private fun showFailedPanel(attemptsRemaining: Int) {
        binding.panelRiddle.visibility = View.GONE
        binding.panelFailed.visibility = View.VISIBLE
        binding.panelLockedOut.visibility = View.GONE
        binding.panelSuccess.visibility = View.GONE

        binding.tvFailedMessage.text = resources.getQuantityString(
            R.plurals.attempts_remaining, attemptsRemaining, attemptsRemaining
        )

        binding.btnRetry.setOnClickListener {
            viewModel.retryWithNewRiddles()
        }
    }

    private fun showLockedOutPanel(lockedUntilMs: Long) {
        binding.panelRiddle.visibility = View.GONE
        binding.panelFailed.visibility = View.GONE
        binding.panelLockedOut.visibility = View.VISIBLE
        binding.panelSuccess.visibility = View.GONE

        lockoutTimer?.cancel()
        lockoutTimer = object : CountDownTimer(lockedUntilMs - System.currentTimeMillis(), 1_000) {
            override fun onTick(ms: Long) {
                val mins = TimeUnit.MILLISECONDS.toMinutes(ms)
                val secs = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
                binding.tvLockCountdown.text = String.format("%02d:%02d", mins, secs)
            }
            override fun onFinish() {
                // Timer expired — offer new challenge
                viewModel.retryWithNewRiddles()
            }
        }.start()
    }

    private fun showSuccessPanel() {
        binding.panelRiddle.visibility = View.GONE
        binding.panelFailed.visibility = View.GONE
        binding.panelLockedOut.visibility = View.GONE
        binding.panelSuccess.visibility = View.VISIBLE

        // Auto-close after a short delay so the user can see the success feedback
        binding.root.postDelayed({ finish() }, 1_500)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun formatSeconds(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%d:%02d", m, s)
    }

    private fun registerDismissReceiver() {
        registerReceiver(
            dismissReceiver,
            IntentFilter(ACTION_DISMISS_LOCK),
            Context.RECEIVER_NOT_EXPORTED
        )
    }
}
