package com.veera.call

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.veera.call.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val packageName = telecomManager.defaultDialerPackage
        toast(packageName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                telecomManager.acceptRingingCall()
            }else {
                toast("Permission Required")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ uses RoleManager
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                (this as Activity).startActivityForResult(intent, 100)
            } else {
                toast("this role are not receiver")
            }
        } else {
            toast("Os version not enough")
        }

    }

    fun requestDefaultDialer(activity: Activity) {
        val telecomManager = activity.getSystemService(TELECOM_SERVICE) as TelecomManager
        val isDefault = activity.packageName == telecomManager.defaultDialerPackage

        if (!isDefault) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.packageName)
            }
            activity.startActivity(intent)
        }
    }


    fun promptSetDefaultDialer(context: Activity) {
        if (!isDefaultDialer(context)) {
            AlertDialog.Builder(context)
                .setTitle("Set as Default Phone App")
                .setMessage("This app needs to be set as your default Phone app to manage calls.")
                .setPositiveButton("Set Now") { _, _ ->
                    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                        putExtra(
                            TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                            context.packageName
                        )
                    }
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    fun isDefaultDialer(context: Context): Boolean {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        return context.packageName == telecomManager.defaultDialerPackage
    }

    fun toast(message:String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (packageName == telecomManager.defaultDialerPackage) {
            Toast.makeText(this, "App is now the default dialer", Toast.LENGTH_SHORT).show()
        }
    }

}