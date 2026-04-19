package com.veera.core.util

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    fun isDefaultDialer(): Boolean {
        return context.packageName == telecomManager.defaultDialerPackage
    }

    @RequiresPermission(Manifest.permission.CALL_PHONE)
    fun makeCall(number: String) {
        try {
            val uri = Uri.fromParts("tel", number, null)
            val extras = Bundle()
            // Optional: specify which phone account to use
            telecomManager.placeCall(uri, extras)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createRequestDefaultDialerIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            } else {
                null
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            }
        }
    }
}
