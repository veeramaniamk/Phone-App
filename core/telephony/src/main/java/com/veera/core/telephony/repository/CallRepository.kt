package com.veera.core.telephony.repository

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

import com.veera.core.telephony.recorder.CallRecorder

@Singleton
class CallRepository @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val callRecorder: CallRecorder
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _currentCall = MutableStateFlow<Call?>(null)
    val currentCall: StateFlow<Call?> = _currentCall

    private val _callState = MutableStateFlow<Int>(Call.STATE_DISCONNECTED)
    val callState: StateFlow<Int> = _callState

    private val _callerName = MutableStateFlow<String>("Unknown")
    val callerName: StateFlow<String> = _callerName

    private val _callerNumber = MutableStateFlow<String>("")
    val callerNumber: StateFlow<String> = _callerNumber

    private val _callerPhotoUri = MutableStateFlow<String?>(null)
    val callerPhotoUri: StateFlow<String?> = _callerPhotoUri

    // Audio State
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    private val _recordingStartTimeMillis = MutableStateFlow(0L)
    val recordingStartTimeMillis: StateFlow<Long> = _recordingStartTimeMillis
    
    private val _recordMessageEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val recordMessageEvent: SharedFlow<String> = _recordMessageEvent
    
    private val _supportedAudioRoutes = MutableStateFlow(CallAudioState.ROUTE_EARPIECE or CallAudioState.ROUTE_SPEAKER)
    val supportedAudioRoutes: StateFlow<Int> = _supportedAudioRoutes
    
    private val _currentAudioRoute = MutableStateFlow(CallAudioState.ROUTE_EARPIECE)
    val currentAudioRoute: StateFlow<Int> = _currentAudioRoute

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
            _currentAudioRoute.value = CallAudioState.ROUTE_EARPIECE
            if (_isRecording.value) {
                val result = callRecorder.stopRecording()
                _isRecording.value = false
                _recordingStartTimeMillis.value = 0L
                result.onSuccess { 
                    _recordMessageEvent.tryEmit("Saved: $it") 
                }.onFailure { 
                    _recordMessageEvent.tryEmit("Error saving recording") 
                }
            }
        }

        call?.details?.let { details ->
            val number = details.handle?.schemeSpecificPart ?: ""
            _callerNumber.value = number
            resolveCallerName(number)
        }
    }

    private fun resolveCallerName(number: String) {
        if (number.isEmpty()) {
            _callerName.value = "Unknown"
            return
        }
        repositoryScope.launch {
            val name = callLogRepository.getContactName(number)
            _callerName.value = name ?: "Unknown"
            val photoUri = callLogRepository.getContactPhotoUri(number)
            _callerPhotoUri.value = photoUri
        }
    }
    
    fun setCallerInfo(name: String, number: String, photoUri: String? = null) {
        _callerName.value = name
        _callerNumber.value = number
        _callerPhotoUri.value = photoUri
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            _callState.value = state
            if (state == Call.STATE_ACTIVE && _isRecording.value) {
                if (!callRecorder.isCurrentlyRecording()) {
                    if (callRecorder.startRecording(_callerNumber.value)) {
                        _recordingStartTimeMillis.value = System.currentTimeMillis()
                    } else {
                        _recordMessageEvent.tryEmit("Failed to start recording")
                        _isRecording.value = false
                    }
                }
            }
            if (state == Call.STATE_DISCONNECTED) {
                _callFinishedEvent.tryEmit(Unit)
                if (_isRecording.value) {
                    val result = callRecorder.stopRecording()
                    _isRecording.value = false
                    _recordingStartTimeMillis.value = 0L
                    result.onSuccess { 
                        _recordMessageEvent.tryEmit("Saved: $it") 
                    }.onFailure { 
                        _recordMessageEvent.tryEmit("Error saving recording") 
                    }
                }
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
    
    fun setAudioRoute(route: Int) {
        _audioRouteRequest.tryEmit(route)
    }
    
    fun toggleRecording() {
        val newState = !_isRecording.value
        _isRecording.value = newState
        if (newState && _callState.value == Call.STATE_ACTIVE) {
            if (callRecorder.startRecording(_callerNumber.value)) {
                _recordingStartTimeMillis.value = System.currentTimeMillis()
            } else {
                _recordMessageEvent.tryEmit("Failed to start recording")
                _isRecording.value = false
            }
        } else if (!newState) {
            val result = callRecorder.stopRecording()
            _recordingStartTimeMillis.value = 0L
            result.onSuccess { 
                _recordMessageEvent.tryEmit("Saved: $it") 
            }.onFailure { 
                _recordMessageEvent.tryEmit("Error saving recording") 
            }
        }
    }
    
    fun updateAudioState(state: CallAudioState) {
        _isMuted.value = state.isMuted
        _isSpeakerOn.value = state.route == CallAudioState.ROUTE_SPEAKER
        _supportedAudioRoutes.value = state.supportedRouteMask
        _currentAudioRoute.value = state.route
    }
}
