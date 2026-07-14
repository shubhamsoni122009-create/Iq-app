package com.iqlock.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.iqlock.app.R
import com.iqlock.app.adapter.AppListAdapter
import com.iqlock.app.adapter.AppListItem
import com.iqlock.app.databinding.ActivityMainBinding
import com.iqlock.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * MainActivity.kt — The IQ Lock home screen / dashboard.
 *
 * Features:
 *  - Master enable/disable toggle (MaterialSwitch)
 *  - Permission status cards (Accessibility, Usage Stats, Overlay)
 *  - Summary stats card (weekly attempts, success rate)
 *  - Quick-access list of protected apps
 *  - Navigation buttons to AppSelection, Statistics, Settings
 *
 * Permissions that require user action are detected on every onResume so the
 * banners update automatically after the user returns from system Settings.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupToggle()
        setupNavigationButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        checkAndShowPermissionBanners()
        viewModel.refreshWeeklySummary()
    }

    // ── Toggle ────────────────────────────────────────────────────────────────

    private fun setupToggle() {
        binding.switchMaster.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleLockEnabled()
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun setupNavigationButtons() {
        binding.cardSelectApps.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }
        binding.cardStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLockEnabled.collect { enabled ->
                binding.switchMaster.isChecked = enabled
                binding.tvMasterStatus.text = if (enabled)
                    getString(R.string.lock_enabled)
                else
                    getString(R.string.lock_disabled)
            }
        }

        lifecycleScope.launch {
            viewModel.weeklySummary.collect { summary ->
                binding.tvWeeklyAttempts.text = summary.totalAttempts.toString()
                binding.tvWeeklySuccesses.text = summary.totalSuccesses.toString()
                binding.tvWeeklyFailures.text = summary.totalFailures.toString()
                binding.tvSuccessRate.text = "${summary.successRate}%"
            }
        }

        lifecycleScope.launch {
            viewModel.protectedApps.collect { apps ->
                val count = apps.size
                binding.tvProtectedCount.text = resources.getQuantityString(
                    R.plurals.apps_protected, count, count
                )
            }
        }
    }

    // ── Permission Banners ────────────────────────────────────────────────────

    private fun checkAndShowPermissionBanners() {
        val accessOk = isAccessibilityEnabled()
        val usageOk = isUsageStatsGranted()
        val overlayOk = Settings.canDrawOverlays(this)

        // Accessibility banner
        binding.bannerAccessibility.visibility =
            if (!accessOk) View.VISIBLE else View.GONE
        binding.bannerAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // Usage Stats banner
        binding.bannerUsageStats.visibility =
            if (!usageOk) View.VISIBLE else View.GONE
        binding.bannerUsageStats.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        // Overlay permission banner
        binding.bannerOverlay.visibility =
            if (!overlayOk) View.VISIBLE else View.GONE
        binding.bannerOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        // Show a snackbar if all permissions are granted
        if (accessOk && usageOk && overlayOk) {
            binding.tvAllPermissionsOk.visibility = View.VISIBLE
        } else {
            binding.tvAllPermissionsOk.visibility = View.GONE
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val expected = "$packageName/.service.IQLockAccessibilityService"
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    private fun isUsageStatsGranted(): Boolean {
        return try {
            val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) { false }
    }
}
