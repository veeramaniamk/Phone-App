package com.veera.dialer.services

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.veera.dialer.CallActivity

class MyInCallService : InCallService() {
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        // Start UI for call
        val intent = Intent(this, CallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
    }
}
