package com.veera.core.telephony.viewmodel

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.veera.core.telephony.repository.CallRepository
import com.veera.core.util.DialerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val dialerManager: DialerManager
) : ViewModel() {
    val currentCall = callRepository.currentCall
    val callState = callRepository.callState
    val callerName = callRepository.callerName
    val callerNumber = callRepository.callerNumber

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
}
