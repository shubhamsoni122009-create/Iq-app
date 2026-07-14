package com.iqlock.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iqlock.app.data.entity.Statistic
import com.iqlock.app.databinding.ItemStatCardBinding

/**
 * StatisticsAdapter.kt — RecyclerView adapter for the statistics screen.
 *
 * Displays one card per Statistic row (one per protected app per date).
 * Each card shows:
 *  - App label / package name
 *  - Unlock attempts, successes, and failures
 *  - Total usage time (formatted as "Xh Ym")
 *  - A simple progress bar showing success rate
 */
class StatisticsAdapter : ListAdapter<Statistic, StatisticsAdapter.StatViewHolder>(DIFF_CALLBACK) {

    inner class StatViewHolder(private val binding: ItemStatCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: Statistic) {
            // Show app package name (caller can swap in a label via the data model)
            binding.tvAppName.text = stat.packageName.substringAfterLast('.')
                .replaceFirstChar { it.uppercase() }
            binding.tvDate.text = stat.date

            binding.tvAttempts.text = stat.unlockAttempts.toString()
            binding.tvSuccesses.text = stat.successfulUnlocks.toString()
            binding.tvFailures.text = stat.failedUnlocks.toString()

            // Format usage time
            val mins = (stat.totalUsageTimeMs / 60_000).toInt()
            binding.tvUsageTime.text = when {
                mins < 1 -> "<1m"
                mins < 60 -> "${mins}m"
                else -> "${mins / 60}h ${mins % 60}m"
            }

            // Success rate progress bar
            val rate = if (stat.unlockAttempts > 0)
                (stat.successfulUnlocks * 100 / stat.unlockAttempts)
            else 0
            binding.progressSuccessRate.progress = rate
            binding.tvSuccessRate.text = "$rate%"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val binding = ItemStatCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Statistic>() {
            override fun areItemsTheSame(old: Statistic, new: Statistic) = old.id == new.id
            override fun areContentsTheSame(old: Statistic, new: Statistic) = old == new
        }
    }
}
