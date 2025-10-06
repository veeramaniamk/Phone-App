package com.android.veera

import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.veera.dialer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQ_CALL_PERMISSIONS = 100
    private lateinit var handle: PhoneAccountHandle

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val roleManager = getSystemService(android.app.role.RoleManager::class.java)

        if (roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_DIALER)) {
            if (!roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_DIALER)
                startActivityForResult(intent, 100)
            }
        }
    }

}
