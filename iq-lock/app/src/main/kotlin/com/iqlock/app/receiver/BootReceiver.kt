package com.iqlock.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

/**
 * BootReceiver.kt — BroadcastReceiver that triggers on device boot.
 *
 * IQ Lock relies on an AccessibilityService which is automatically restarted by
 * the Android system after reboot as long as the permission was granted before
 * the device was powered off. This receiver exists to:
 *  1. Detect that the device has booted
 *  2. Check whether the accessibility service is still enabled
 *  3. If it's no longer enabled (e.g., cleared by a factory reset or OEM), it
 *     logs the state so the next app launch can prompt the user to re-enable it.
 *
 * NOTE: Android does not allow programmatically enabling accessibility services.
 * The user must always enable it manually via Settings → Accessibility.
 * This receiver cannot force-start the service; it only checks the state.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Check if the accessibility service is still registered
            val isEnabled = isAccessibilityServiceEnabled(context)

            // The accessibility service auto-restarts on boot if the permission was granted.
            // If it's not enabled, we store the fact so MainActivity can prompt the user.
            val prefs = context.getSharedPreferences("iqlock_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("last_boot_time", System.currentTimeMillis())
                .putBoolean("accessibility_was_enabled_at_boot", isEnabled)
                .apply()
        }
    }

    /**
     * Returns true if IQLockAccessibilityService is listed as an enabled
     * accessibility service in the system settings.
     */
    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponent =
            "${context.packageName}/.service.IQLockAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val component = colonSplitter.next()
            if (component.equals(expectedComponent, ignoreCase = true)) return true
        }
        return false
    }
}
