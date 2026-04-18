package com.veera.core.telephony.repository

import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor() {
    private val _currentCall = MutableStateFlow<Call?>(null)
    val currentCall: StateFlow<Call?> = _currentCall

    private val _callState = MutableStateFlow<Int>(Call.STATE_DISCONNECTED)
    val callState: StateFlow<Int> = _callState

    fun updateCall(call: Call?) {
        _currentCall.value = call
        _callState.value = call?.state ?: Call.STATE_DISCONNECTED
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            _callState.value = state
        }
    }

    fun registerCallback(call: Call) {
        call.registerCallback(callCallback)
    }

    fun unregisterCallback(call: Call) {
        call.unregisterCallback(callCallback)
    }
}
