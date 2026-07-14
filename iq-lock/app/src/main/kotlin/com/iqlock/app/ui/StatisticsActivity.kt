package com.iqlock.app.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.iqlock.app.adapter.StatisticsAdapter
import com.iqlock.app.databinding.ActivityStatisticsBinding
import com.iqlock.app.viewmodel.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * StatisticsActivity.kt — Displays usage and unlock statistics.
 *
 * Shows:
 *  - Tab selector: Today / Last 7 Days
 *  - Summary card: total attempts, successes, failures, success rate
 *  - Per-app breakdown in a RecyclerView of stat cards
 */
@AndroidEntryPoint
class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var adapter: StatisticsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = StatisticsAdapter()
        binding.rvStats.layoutManager = LinearLayoutManager(this)
        binding.rvStats.adapter = adapter

        setupTabs()
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.selectPeriod(
                    if (tab?.position == 0) StatisticsViewModel.Period.DAILY
                    else StatisticsViewModel.Period.WEEKLY
                )
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.summary.collect { summary ->
                binding.tvTotalAttempts.text = summary.totalAttempts.toString()
                binding.tvTotalSuccesses.text = summary.totalSuccesses.toString()
                binding.tvTotalFailures.text = summary.totalFailures.toString()
                binding.tvSuccessRate.text = "${summary.successRate}%"
                binding.tvUsageTime.text = summary.totalUsageFormatted
                binding.progressSummary.progress = summary.successRate
            }
        }

        lifecycleScope.launch {
            viewModel.currentStats.collect { stats ->
                adapter.submitList(stats)
                binding.tvEmptyState.visibility =
                    if (stats.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }
}
