package com.veera.call.services

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager

object DefaultDialerHelper {
    fun getDefaultDialerPackage(context: Context): String? {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        return telecomManager.defaultDialerPackage
    }
    fun requestDefaultDialer(context: Activity) {
        val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)
            } else {
                TODO("VERSION.SDK_INT < Q")
            }
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.startActivityForResult(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER),
                    100
                )
            }
        } else {
            // Fallback for Vivo devices
            openVivoDefaultAppsSettings(context)
        }
    }

    private fun openVivoDefaultAppsSettings(context: Context) {
        try {
            // Try Vivo's custom settings path
            val intent = Intent().apply {
                setClassName(
                    "com.android.settings",
                    "com.android.settings.Settings\$DefaultAppSettingsActivity"
                )
                putExtra(":settings:fragment_args_key", "default_dialer")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Standard Android fallback
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}