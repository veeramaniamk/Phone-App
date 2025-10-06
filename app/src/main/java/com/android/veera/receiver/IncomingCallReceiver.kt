package com.android.veera.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast
import android.util.Log

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Log.d("IncomingCallReceiver", "Incoming call from: $incomingNumber")
                    Toast.makeText(context, "Incoming call: $incomingNumber", Toast.LENGTH_SHORT).show()

                    // Optionally start your activity to show UI
                    val callIntent = Intent(context, Class.forName("com.example.phoneapp.MainActivity"))
                    callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context?.startActivity(callIntent)
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.d("IncomingCallReceiver", "Call answered or outgoing")
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.d("IncomingCallReceiver", "Call ended or no call")
                }
            }
        }
    }
}
