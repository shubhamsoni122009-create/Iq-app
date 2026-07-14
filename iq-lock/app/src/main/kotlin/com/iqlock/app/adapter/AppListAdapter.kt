package com.iqlock.app.adapter

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iqlock.app.databinding.ItemAppBinding

/**
 * AppListAdapter.kt — RecyclerView adapter used in AppSelectionActivity.
 *
 * Shows a list of installed apps, each with:
 *  - App icon
 *  - App label
 *  - Package name subtitle
 *  - A checkbox indicating whether the app is currently protected
 *
 * Uses ListAdapter + DiffUtil for efficient, animated updates.
 */
class AppListAdapter(
    private val pm: PackageManager,
    private val onCheckedChange: (packageName: String, isChecked: Boolean) -> Unit
) : ListAdapter<AppListItem, AppListAdapter.AppViewHolder>(DIFF_CALLBACK) {

    inner class AppViewHolder(
        private val binding: ItemAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppListItem) {
            // Load app icon using the package manager
            try {
                val icon = pm.getApplicationIcon(item.packageName)
                binding.ivAppIcon.setImageDrawable(icon)
            } catch (_: Exception) {
                binding.ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            binding.tvAppLabel.text = item.appLabel
            binding.tvPackageName.text = item.packageName

            // Prevent checkbox listener from firing during bind
            binding.cbProtect.setOnCheckedChangeListener(null)
            binding.cbProtect.isChecked = item.isProtected

            binding.cbProtect.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(item.packageName, isChecked)
            }

            // Clicking the whole row toggles the checkbox
            binding.root.setOnClickListener {
                binding.cbProtect.toggle()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AppListItem>() {
            override fun areItemsTheSame(old: AppListItem, new: AppListItem) =
                old.packageName == new.packageName

            override fun areContentsTheSame(old: AppListItem, new: AppListItem) =
                old == new
        }
    }
}

/** Data model for a single row in the app selection list. */
data class AppListItem(
    val packageName: String,
    val appLabel: String,
    val isProtected: Boolean
)
