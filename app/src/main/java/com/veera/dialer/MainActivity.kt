package com.veera.dialer

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.veera.android.dialer.databinding.ActivityMainBinding
import com.veera.dialer.services.MyConnectionService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQ_CALL_PERMISSIONS = 100
    private lateinit var handle: PhoneAccountHandle

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

        // Ask default dialer role
        requestDefaultDialer(this)

        // Request runtime permissions
        requestPermissionsIfNeeded()

        registerPhoneAccount(this)

        val tm = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        Toast.makeText(this, "Default dialer: ${tm.defaultDialerPackage}", Toast.LENGTH_LONG).show()

        // Example button to make a call
        binding.callB.setOnClickListener {
            placeCall(this, binding.phoneNumberET.text.toString())
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.MANAGE_OWN_CALLS
        )
        ActivityCompat.requestPermissions(this, permissions, REQ_CALL_PERMISSIONS)
    }

    fun placeCall(context: Context, number: String) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val uri = Uri.fromParts("tel", number, null)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "No CALL_PHONE permission", Toast.LENGTH_SHORT).show()
            return
        }

        val bundle = Bundle().apply {
            putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
        }
        telecomManager.placeCall(uri, bundle)
    }

    fun registerPhoneAccount(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val componentName = ComponentName(context, MyConnectionService::class.java)

        // store in the class field
        handle = PhoneAccountHandle(componentName, "MyPhoneAccountId")

        val phoneAccount = PhoneAccount.builder(handle, "My Phone App")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
    }

    fun requestDefaultDialer(activity: Activity) {
        val telecomManager = activity.getSystemService(TELECOM_SERVICE) as TelecomManager
        val isDefault = activity.packageName == telecomManager.defaultDialerPackage

        if (!isDefault) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    activity.packageName
                )
            }
            activity.startActivity(intent)
        }
    }
}
