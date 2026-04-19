package com.veera.core.telephony.repository

import android.telecom.Call
import android.telecom.VideoProfile
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

    private val _callerName = MutableStateFlow<String>("Unknown")
    val callerName: StateFlow<String> = _callerName

    private val _callerNumber = MutableStateFlow<String>("")
    val callerNumber: StateFlow<String> = _callerNumber

    fun updateCall(call: Call?) {
        _currentCall.value = call
        _callState.value = call?.state ?: Call.STATE_DISCONNECTED
        
        call?.details?.let { details ->
            _callerNumber.value = details.handle?.schemeSpecificPart ?: ""
            // Name resolution could happen here or in ViewModel
        }
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

    fun answerCall() {
        _currentCall.value?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun rejectCall() {
        _currentCall.value?.disconnect()
    }

    fun disconnectCall() {
        _currentCall.value?.disconnect()
    }
}
