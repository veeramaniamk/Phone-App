package com.veera.core.telephony.repository

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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

    // Audio State
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn

    // Audio Control Signals for Service
    private val _audioRouteRequest = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val audioRouteRequest: SharedFlow<Int> = _audioRouteRequest

    private val _muteRequest = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val muteRequest: SharedFlow<Boolean> = _muteRequest

    // Event for UI to refresh logs
    private val _callFinishedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val callFinishedEvent: SharedFlow<Unit> = _callFinishedEvent

    fun updateCall(call: Call?) {
        _currentCall.value = call
        _callState.value = call?.state ?: Call.STATE_DISCONNECTED
        
        if (call == null) {
            _callFinishedEvent.tryEmit(Unit)
            // Reset audio states
            _isMuted.value = false
            _isSpeakerOn.value = false
        }

        call?.details?.let { details ->
            _callerNumber.value = details.handle?.schemeSpecificPart ?: ""
        }
    }
    
    fun setCallerInfo(name: String, number: String) {
        _callerName.value = name
        _callerNumber.value = number
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            _callState.value = state
            if (state == Call.STATE_DISCONNECTED) {
                _callFinishedEvent.tryEmit(Unit)
            }
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

    fun toggleMute() {
        val newState = !_isMuted.value
        _isMuted.value = newState
        _muteRequest.tryEmit(newState)
    }

    fun toggleSpeaker() {
        val newState = !_isSpeakerOn.value
        _isSpeakerOn.value = newState
        val route = if (newState) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
        _audioRouteRequest.tryEmit(route)
    }
    
    fun updateAudioState(state: CallAudioState) {
        _isMuted.value = state.isMuted
        _isSpeakerOn.value = state.route == CallAudioState.ROUTE_SPEAKER
    }
}
