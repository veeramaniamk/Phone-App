package com.android.veera.functions

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity

object CallManager {

    @RequiresApi(Build.VERSION_CODES.O)
    fun answerCall(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        try {
            telecomManager.acceptRingingCall()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setAsDefaultDialer(activity: FragmentActivity) {
        val roleManager = activity.getSystemService(android.app.role.RoleManager::class.java)

        if (roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_DIALER)) {
            if (!roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_DIALER)
                activity.startActivityForResult(intent, 100)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun endCall(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        try {
            telecomManager.endCall()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun toggleSpeaker(context: Context, enable: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Put device into "in-call" mode
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // Request audio focus for VOICE_CALL stream
        audioManager.requestAudioFocus(
            { },  // no-op listener
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )

        // Now toggle speaker
        audioManager.isSpeakerphoneOn = enable
    }


}
