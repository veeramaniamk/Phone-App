package com.android.veera.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class MyInCallService : InCallService() {

    private var currentCall: Call? = null

    companion object {
        // Static reference (only valid while a call is active!)
        var instance: MyInCallService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        currentCall = call
        // Start UI for call
        call.registerCallback(object : Call.Callback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onStateChanged(call: Call, state: Int) {
                if (state == Call.STATE_ACTIVE) {
                    // Call is answered, now enable audio
                    setupAudio()
                }
            }
        })
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        resetAudio()
        currentCall = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAudio() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            // Set communication mode
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

            // Request audio focus
            val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            if (audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Default to earpiece
                audioManager.isSpeakerphoneOn = false
                // Set volume to half of max
                audioManager.setStreamVolume(
                    AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) / 2,
                    0
                )
                Log.d("MyInCallService", "Audio setup completed")
            } else {
                Log.e("MyInCallService", "Failed to gain audio focus")
            }
        } catch (e: Exception) {
            Log.e("MyInCallService", "Error setting up audio: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleSpeaker(enable: Boolean) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            // Check default dialer
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (telecomManager.defaultDialerPackage != packageName) {
                Log.e("MyInCallService", "App is not the default dialer")
                Toast.makeText(this, "Please set this app as the default dialer", Toast.LENGTH_LONG).show()
                return
            }

            // Check for Bluetooth or wired headset
            if (audioManager.isBluetoothScoOn || audioManager.isWiredHeadsetOn) {
                Log.w("MyInCallService", "Bluetooth or wired headset active")
                Toast.makeText(this, "Cannot use speakerphone: Headset active", Toast.LENGTH_SHORT).show()
                return
            }

            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.abandonAudioFocus(null) // Clear any existing focus

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setOnAudioFocusChangeListener { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                audioManager.isSpeakerphoneOn = enable
                                Log.d("MyInCallService", "Speakerphone set to: $enable, actual state: ${audioManager.isSpeakerphoneOn}")
                            }
                            AudioManager.AUDIOFOCUS_LOSS -> Log.e("MyInCallService", "Audio focus lost")
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> Log.w("MyInCallService", "Audio focus lost transiently")
                            else -> Log.w("MyInCallService", "Focus change: $focusChange")
                        }
                    }
                    .build()
                val result = audioManager.requestAudioFocus(audioFocusRequest)
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    audioManager.isSpeakerphoneOn = enable
                    Log.d("MyInCallService", "Speakerphone set to: $enable, actual state: ${audioManager.isSpeakerphoneOn}")
                } else {
                    Log.e("MyInCallService", "Failed to gain audio focus: $result")
                    Toast.makeText(this, "Failed to switch audio mode", Toast.LENGTH_SHORT).show()
                }
            } else {
                @Suppress("DEPRECATION")
                val result = audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    audioManager.isSpeakerphoneOn = enable
                    Log.d("MyInCallService", "Speakerphone set to: $enable (pre-Oreo)")
                } else {
                    Log.e("MyInCallService", "Failed to gain audio focus (pre-Oreo): $result")
                    Toast.makeText(this, "Failed to switch audio mode", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MyInCallService", "Error toggling speaker: ${e.message}")
            Toast.makeText(this, "Error switching audio: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetAudio() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            audioManager.abandonAudioFocus(null) // Abandon audio focus
            Log.d("MyInCallService", "Audio reset completed")
        } catch (e: Exception) {
            Log.e("MyInCallService", "Error resetting audio: ${e.message}")
        }
    }

}
