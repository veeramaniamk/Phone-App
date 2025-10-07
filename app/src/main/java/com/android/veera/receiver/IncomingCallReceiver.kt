package com.android.veera.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast
import android.util.Log
import com.android.veera.IncomingCallActivity

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Log.d("IncomingCallReceiver", "Incoming call from: $incomingNumber")
                    Toast.makeText(context, "Incoming call: $incomingNumber", Toast.LENGTH_SHORT).show()

                    val dialogIntent = Intent(context, IncomingCallActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("incoming_number", incomingNumber)
                    }
                    context?.startActivity(dialogIntent)
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
