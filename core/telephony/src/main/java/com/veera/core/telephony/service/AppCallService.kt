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
import android.content.Context
import android.os.PowerManager

@AndroidEntryPoint
class AppCallService : InCallService() {

    @Inject
    lateinit var callRepository: CallRepository

    @Inject
    lateinit var notificationManager: CallNotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var proximityWakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            proximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "AppCallService:ProximityWakeLock"
            )
        }
        
        observeAudioRequests()
        observeCallInfo()
    }

    private fun observeCallInfo() {
        callRepository.callState
            .onEach { state ->
                val name = callRepository.callerName.value
                val number = callRepository.callerNumber.value
                val isSpeakerOn = callRepository.isSpeakerOn.value
                val photoUri = callRepository.callerPhotoUri.value
                
                when (state) {
                    Call.STATE_RINGING -> {
                        notificationManager.showIncomingCallNotification(name, number, photoUri)
                    }
                    Call.STATE_ACTIVE -> {
                        if (proximityWakeLock?.isHeld == false) {
                            proximityWakeLock?.acquire()
                        }
                        val connectTime = callRepository.currentCall.value?.details?.connectTimeMillis ?: System.currentTimeMillis()
                        notificationManager.showOngoingCallNotification(name, number, isSpeakerOn, connectTime, photoUri)
                    }
                    Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> {
                        if (proximityWakeLock?.isHeld == true) {
                            proximityWakeLock?.release()
                        }
                        notificationManager.cancelNotification()
                    }
                }
            }
            .launchIn(serviceScope)

        callRepository.isSpeakerOn
            .onEach { isSpeakerOn ->
                if (callRepository.callState.value == Call.STATE_ACTIVE) {
                    val connectTime = callRepository.currentCall.value?.details?.connectTimeMillis ?: System.currentTimeMillis()
                    notificationManager.showOngoingCallNotification(
                        callRepository.callerName.value,
                        callRepository.callerNumber.value,
                        isSpeakerOn,
                        connectTime,
                        callRepository.callerPhotoUri.value
                    )
                }
            }
            .launchIn(serviceScope)
            
        callRepository.callerName
            .onEach { name ->
                if (callRepository.callState.value == Call.STATE_ACTIVE) {
                    val connectTime = callRepository.currentCall.value?.details?.connectTimeMillis ?: System.currentTimeMillis()
                    notificationManager.showOngoingCallNotification(
                        name,
                        callRepository.callerNumber.value,
                        callRepository.isSpeakerOn.value,
                        connectTime,
                        callRepository.callerPhotoUri.value
                    )
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
            notificationManager.showIncomingCallNotification("Incoming Call", number, null)
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

    override fun onDestroy() {
        super.onDestroy()
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock?.release()
        }
    }
}
