package com.veera.core.telephony.service

import android.telecom.Call
import android.telecom.InCallService
import com.veera.core.telephony.repository.CallRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppCallService : InCallService() {

    @Inject
    lateinit var callRepository: CallRepository

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callRepository.updateCall(call)
        callRepository.registerCallback(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callRepository.unregisterCallback(call)
        callRepository.updateCall(null)
    }
}
