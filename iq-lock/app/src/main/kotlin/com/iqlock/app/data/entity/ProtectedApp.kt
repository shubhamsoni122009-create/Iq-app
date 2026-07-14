package com.iqlock.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ProtectedApp.kt — Room entity storing apps selected for IQ Lock protection.
 *
 * Each row represents one app the user has chosen to protect.
 * The accessibility service reads this table on every foreground-change event.
 *
 * Fields:
 *  - packageName: unique identifier, e.g. "com.instagram.android"
 *  - appLabel: display name cached at time of selection, e.g. "Instagram"
 *  - isEnabled: if false, the lock is paused for this app without removing it
 *  - addedAt: epoch millis when the user added this app to protection
 *  - iconBase64: optional cached base-64 icon (empty if loading at runtime)
 */
@Entity(tableName = "protected_apps", primaryKeys = ["package_name"])
data class ProtectedApp(
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "app_label")
    val appLabel: String,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "icon_base64")
    val iconBase64: String = ""
)
