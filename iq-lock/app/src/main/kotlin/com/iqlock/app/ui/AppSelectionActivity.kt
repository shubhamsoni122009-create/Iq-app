package com.iqlock.app.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.iqlock.app.adapter.AppListAdapter
import com.iqlock.app.adapter.AppListItem
import com.iqlock.app.databinding.ActivityAppSelectionBinding
import com.iqlock.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AppSelectionActivity.kt — Lets the user choose which apps to protect with IQ Lock.
 *
 * Loads all installed user-facing apps, marks those already in the protected list,
 * and lets the user toggle protection via a checkbox or row click.
 * Changes are saved immediately to the Room database via [MainViewModel].
 */
@AndroidEntryPoint
class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: AppListAdapter

    private var allItems: List<AppListItem> = emptyList()
    private var protectedPackages: Set<String> = emptySet()
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AppListAdapter(packageManager) { pkg, checked ->
            onAppToggled(pkg, checked)
        }

        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter

        setupSearch()
        observeProtectedApps()
        loadInstalledApps()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // ── Load Installed Apps ────────────────────────────────────────────────────

    private fun loadInstalledApps() {
        binding.progressApps.visibility = View.VISIBLE
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { isUserApp(it) && it.packageName != packageName }
                    .map { appInfo ->
                        AppListItem(
                            packageName = appInfo.packageName,
                            appLabel = packageManager.getApplicationLabel(appInfo).toString(),
                            isProtected = appInfo.packageName in protectedPackages
                        )
                    }
                    .sortedBy { it.appLabel.lowercase() }
            }
            allItems = items
            binding.progressApps.visibility = View.GONE
            applyCurrentFilter()
        }
    }

    private fun isUserApp(appInfo: ApplicationInfo): Boolean =
        (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

    // ── Observe Protected Apps ─────────────────────────────────────────────────

    private fun observeProtectedApps() {
        lifecycleScope.launch {
            viewModel.protectedApps.collect { protected ->
                protectedPackages = protected.map { it.packageName }.toSet()
                // Refresh the list with updated protection state
                allItems = allItems.map { it.copy(isProtected = it.packageName in protectedPackages) }
                applyCurrentFilter()
            }
        }
    }

    // ── Toggle ─────────────────────────────────────────────────────────────────

    private fun onAppToggled(packageName: String, isChecked: Boolean) {
        if (isChecked) {
            // Resolve the human-readable label
            val label = allItems.firstOrNull { it.packageName == packageName }?.appLabel
                ?: packageName
            viewModel.addProtectedApp(packageName, label)
        } else {
            viewModel.removeProtectedApp(packageName)
        }
    }

    // ── Search ─────────────────────────────────────────────────────────────────

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyCurrentFilter()
                return true
            }
        })
    }

    private fun applyCurrentFilter() {
        val filtered = if (currentQuery.isBlank()) allItems
        else allItems.filter {
            it.appLabel.contains(currentQuery, ignoreCase = true) ||
            it.packageName.contains(currentQuery, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }
}
