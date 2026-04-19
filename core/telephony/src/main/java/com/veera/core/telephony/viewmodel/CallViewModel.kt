package com.veera.core.telephony.viewmodel

import android.Manifest
import android.telecom.Call
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veera.core.telephony.repository.CallLogRepository
import com.veera.core.telephony.repository.CallRepository
import com.veera.core.util.DialerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val dialerManager: DialerManager,
    private val callLogRepository: CallLogRepository
) : ViewModel() {
    val currentCall = callRepository.currentCall
    val callState = callRepository.callState
    val callerName = callRepository.callerName
    val callerNumber = callRepository.callerNumber
    
    val isMuted = callRepository.isMuted
    val isSpeakerOn = callRepository.isSpeakerOn

    val callStatusString = callState.map { state ->
        when (state) {
            Call.STATE_CONNECTING -> "Dialing..."
            Call.STATE_DIALING -> "Ringing..."
            Call.STATE_RINGING -> "Incoming..."
            Call.STATE_ACTIVE -> "Connected"
            Call.STATE_DISCONNECTED -> "Ended"
            Call.STATE_DISCONNECTING -> "Disconnecting..."
            Call.STATE_HOLDING -> "On Hold"
            else -> "Connecting..."
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Connecting...")

    init {
        // Resolve name when number changes
        viewModelScope.launch {
            callerNumber.collect { number ->
                if (number.isNotEmpty()) {
                    val name = callLogRepository.getContactName(number)
                    if (name != null) {
                        callRepository.setCallerInfo(name, number)
                    } else {
                        callRepository.setCallerInfo("Unknown", number)
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.CALL_PHONE)
    fun makeCall(number: String) {
        dialerManager.makeCall(number)
    }

    fun answerCall() {
        callRepository.answerCall()
    }

    fun rejectCall() {
        callRepository.rejectCall()
    }

    fun disconnectCall() {
        callRepository.disconnectCall()
    }

    fun toggleMute() {
        callRepository.toggleMute()
    }

    fun toggleSpeaker() {
        callRepository.toggleSpeaker()
    }
}
