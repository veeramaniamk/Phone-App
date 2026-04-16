package com.android.veera.service

import android.telecom.Call
import android.telecom.InCallService

class CallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        // This is where you would normally show your in-call UI
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
    }
}
