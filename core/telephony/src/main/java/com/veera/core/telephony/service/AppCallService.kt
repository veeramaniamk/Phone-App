package com.veera.core.telephony.service

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.veera.core.telephony.notification.CallNotificationManager
import com.veera.core.telephony.repository.CallRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class AppCallService : InCallService() {

    @Inject
    lateinit var callRepository: CallRepository

    @Inject
    lateinit var notificationManager: CallNotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        observeAudioRequests()
        observeCallInfo()
    }

    private fun observeCallInfo() {
        callRepository.callerName
            .onEach { name ->
                if (callRepository.callState.value == Call.STATE_RINGING) {
                    notificationManager.showIncomingCallNotification(name, callRepository.callerNumber.value)
                }
            }
            .launchIn(serviceScope)
    }

    private fun observeAudioRequests() {
        callRepository.audioRouteRequest
            .onEach { route -> setAudioRoute(route) }
            .launchIn(serviceScope)

        callRepository.muteRequest
            .onEach { isMuted -> setMuted(isMuted) }
            .launchIn(serviceScope)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callRepository.updateCall(call)
        callRepository.registerCallback(call)
        
        if (call.state == Call.STATE_RINGING) {
            val number = call.details.handle?.schemeSpecificPart ?: ""
            notificationManager.showIncomingCallNotification("Incoming Call", number)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callRepository.unregisterCallback(call)
        callRepository.updateCall(null)
        notificationManager.cancelNotification()
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        callRepository.updateAudioState(audioState)
    }
}
