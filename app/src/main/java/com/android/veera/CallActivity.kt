package com.android.veera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.android.veera.functions.CallManager
import com.android.veera.services.MyInCallService
import com.veera.dialer.databinding.ActivityCallBinding

class CallActivity : AppCompatActivity() {

    lateinit var binding: ActivityCallBinding
    lateinit var activity: FragmentActivity
    lateinit var context: Context

    @RequiresApi( Build.VERSION_CODES.P, Build.VERSION_CODES.O )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        context = this
        activity = this

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.MODIFY_AUDIO_SETTINGS), 101)
        }

        binding.btnSetDefaultDialer.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CallManager.setAsDefaultDialer(this@CallActivity)
            }
        }

        // Answer Call
        binding.btnAnswer.setOnClickListener {
            CallManager.answerCall(context)
        }

        // End Call
        binding.btnEnd.setOnClickListener {
            CallManager.endCall(context)
        }

        // Toggle Speaker
        binding.toggleSpeaker.setOnCheckedChangeListener { _, isChecked ->
            // Before requesting focus
            val service = MyInCallService.instance
            if (service != null) {
                service.toggleSpeaker(isChecked)
            } else {
                Log.e("CallActivity", "MyInCallService instance is null")
                Toast.makeText(context, "Call service not available", Toast.LENGTH_SHORT).show()
            }
        }

    }



}


