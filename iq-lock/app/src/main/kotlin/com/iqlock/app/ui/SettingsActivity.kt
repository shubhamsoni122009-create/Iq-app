package com.iqlock.app.ui

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.iqlock.app.IQLockApplication
import com.iqlock.app.R
import com.iqlock.app.databinding.ActivitySettingsBinding
import com.iqlock.app.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * SettingsActivity.kt — User-configurable settings for IQ Lock.
 *
 * Controls:
 *  - Unlock timer (SeekBar: 30–300 seconds)
 *  - Lock duration (SeekBar: 1–60 minutes after failure)
 *  - Difficulty (RadioGroup: Easy / Medium / Hard)
 *  - Dark mode (RadioGroup: System / Light / Dark)
 *  - Show hints (Switch)
 *
 * All changes are persisted immediately to Room via SettingsViewModel.
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private var isObserving = false   // prevents feedback loops during init

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        observeSettings()
        setupTimerSeekBar()
        setupLockDurationSeekBar()
        setupDifficultyGroup()
        setupDarkModeGroup()
        setupHintsSwitch()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    // ── Observe ───────────────────────────────────────────────────────────────

    private fun observeSettings() {
        lifecycleScope.launch {
            viewModel.settings.collect { settings ->
                settings ?: return@collect
                isObserving = true

                // Timer seekbar (30–300 seconds)
                val timerProgress = ((settings.unlockTimerSeconds - 30) / 270.0 * 100).toInt()
                binding.seekbarTimer.progress = timerProgress.coerceIn(0, 100)
                binding.tvTimerValue.text = "${settings.unlockTimerSeconds}s"

                // Lock duration seekbar (1–60 minutes)
                val lockProgress = ((settings.lockDurationMinutes - 1) / 59.0 * 100).toInt()
                binding.seekbarLockDuration.progress = lockProgress.coerceIn(0, 100)
                binding.tvLockDurationValue.text = "${settings.lockDurationMinutes} min"

                // Difficulty radio
                when (settings.difficulty) {
                    1 -> binding.rbEasy.isChecked = true
                    2 -> binding.rbMedium.isChecked = true
                    3 -> binding.rbHard.isChecked = true
                }

                // Dark mode radio
                when (settings.darkMode) {
                    0 -> binding.rbSystem.isChecked = true
                    1 -> binding.rbLight.isChecked = true
                    2 -> binding.rbDark.isChecked = true
                }

                // Hints switch
                binding.switchHints.isChecked = settings.showHints

                isObserving = false
            }
        }
    }

    // ── Controls ──────────────────────────────────────────────────────────────

    private fun setupTimerSeekBar() {
        binding.seekbarTimer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                // Map 0–100 → 30–300 seconds
                val seconds = (30 + progress / 100.0 * 270).toInt()
                binding.tvTimerValue.text = "${seconds}s"
                viewModel.setUnlockTimer(seconds)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
    }

    private fun setupLockDurationSeekBar() {
        binding.seekbarLockDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                // Map 0–100 → 1–60 minutes
                val minutes = (1 + progress / 100.0 * 59).toInt()
                binding.tvLockDurationValue.text = "$minutes min"
                viewModel.setLockDuration(minutes)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
    }

    private fun setupDifficultyGroup() {
        binding.rgDifficulty.setOnCheckedChangeListener { _, checkedId ->
            if (isObserving) return@setOnCheckedChangeListener
            val diff = when (checkedId) {
                R.id.rbEasy   -> 1
                R.id.rbMedium -> 2
                R.id.rbHard   -> 3
                else           -> 2
            }
            viewModel.setDifficulty(diff)
        }
    }

    private fun setupDarkModeGroup() {
        binding.rgDarkMode.setOnCheckedChangeListener { _, checkedId ->
            if (isObserving) return@setOnCheckedChangeListener
            val mode = when (checkedId) {
                R.id.rbSystem -> 0
                R.id.rbLight  -> 1
                R.id.rbDark   -> 2
                else           -> 0
            }
            viewModel.setDarkMode(mode)
            (application as IQLockApplication).applyDarkMode(mode)
        }
    }

    private fun setupHintsSwitch() {
        binding.switchHints.setOnCheckedChangeListener { _, isChecked ->
            if (isObserving) return@setOnCheckedChangeListener
            viewModel.setShowHints(isChecked)
        }
    }
}
