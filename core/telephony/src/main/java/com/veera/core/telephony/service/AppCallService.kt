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
import android.os.Build
import android.content.pm.ServiceInfo
import android.app.Notification

/**
 * Service that integrates with Android Telecom to handle ongoing call states.
 * Extending InCallService allows this app to act as the default dialer and receive
 * Call objects representing active phone calls.
 * 
 * This service must be run as a Foreground Service using the 'phoneCall' foreground service type
 * in Android 14+ to prevent the system from falling back to the default system dialer.
 */
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
    
    private fun startForegroundCompat(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                id, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(id, notification)
        }
    }

    /**
     * Subscribes to the CallRepository state flow to react to changes in the call state.
     * Based on the state (Ringing, Active, Disconnected, etc.), it triggers the
     * appropriate CallStyle foreground notification to keep the UI synced.
     */
    private fun observeCallInfo() {
        callRepository.callState
            .onEach { state ->
                val name = callRepository.callerName.value
                val number = callRepository.callerNumber.value
                val isSpeakerOn = callRepository.isSpeakerOn.value
                val photoUri = callRepository.callerPhotoUri.value
                
                when (state) {
                    Call.STATE_RINGING -> {
                        val notification = notificationManager.buildIncomingCallNotification(name, number, photoUri)
                        startForegroundCompat(notificationManager.INCOMING_NOTIFICATION_ID, notification)
                        notificationManager.updateIncomingNotification(notification)
                    }
                    Call.STATE_ACTIVE -> {
                        if (proximityWakeLock?.isHeld == false) {
                            proximityWakeLock?.acquire()
                        }
                        val connectTime = callRepository.currentCall.value?.details?.connectTimeMillis ?: System.currentTimeMillis()
                        val notification = notificationManager.buildOngoingCallNotification(name, number, isSpeakerOn, connectTime, photoUri, isDialing = false)
                        startForegroundCompat(notificationManager.ONGOING_NOTIFICATION_ID, notification)
                        notificationManager.updateOngoingNotification(notification)
                        notificationManager.cancelIncomingNotification()
                    }
                    Call.STATE_DIALING, Call.STATE_CONNECTING, Call.STATE_SELECT_PHONE_ACCOUNT -> {
                        if (proximityWakeLock?.isHeld == false) {
                            proximityWakeLock?.acquire()
                        }
                        val notification = notificationManager.buildOngoingCallNotification(name, number, isSpeakerOn, null, photoUri, isDialing = true)
                        startForegroundCompat(notificationManager.ONGOING_NOTIFICATION_ID, notification)
                        notificationManager.updateOngoingNotification(notification)
                        notificationManager.cancelIncomingNotification()
                    }
                    Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> {
                        if (proximityWakeLock?.isHeld == true) {
                            proximityWakeLock?.release()
                        }
                        stopForeground(true)
                        notificationManager.cancelAllNotifications()
                    }
                }
            }
            .launchIn(serviceScope)

        callRepository.isSpeakerOn
            .onEach { isSpeakerOn ->
                val state = callRepository.callState.value
                val name = callRepository.callerName.value
                val number = callRepository.callerNumber.value
                val photoUri = callRepository.callerPhotoUri.value
                
                if (state == Call.STATE_ACTIVE || state == Call.STATE_DIALING || state == Call.STATE_CONNECTING || state == Call.STATE_SELECT_PHONE_ACCOUNT) {
                    val isDialing = state != Call.STATE_ACTIVE
                    val connectTime = if (isDialing) null else callRepository.currentCall.value?.details?.connectTimeMillis ?: System.currentTimeMillis()
                    val notification = notificationManager.buildOngoingCallNotification(name, number, isSpeakerOn, connectTime, photoUri, isDialing)
                    startForegroundCompat(notificationManager.ONGOING_NOTIFICATION_ID, notification)
                    notificationManager.updateOngoingNotification(notification)
                } else if (state == Call.STATE_RINGING) {
                    val notification = notificationManager.buildIncomingCallNotification(name, number, photoUri)
                    startForegroundCompat(notificationManager.INCOMING_NOTIFICATION_ID, notification)
                    notificationManager.updateIncomingNotification(notification)
                }
            }
            .launchIn(serviceScope)
            
        callRepository.callerName
            .onEach { name ->
                val state = callRepository.callState.value
                val number = callRepository.callerNumber.value
                val isSpeakerOn = callRepository.isSpeakerOn.value
                val photoUri = callRepository.callerPhotoUri.value

                if (state == Call.STATE_ACTIVE || state == Call.STATE_DIALING || state == Call.STATE_CONNECTING || state == Call.STATE_SELECT_PHONE_ACCOUNT) {
                    val isDialing = state != Call.STATE_ACTIVE
                    val connectTime = if (isDialing) null else callRepository.currentCall.value?.details?.connectTimeMillis ?: System.currentTimeMillis()
                    val notification = notificationManager.buildOngoingCallNotification(name, number, isSpeakerOn, connectTime, photoUri, isDialing)
                    startForegroundCompat(notificationManager.ONGOING_NOTIFICATION_ID, notification)
                    notificationManager.updateOngoingNotification(notification)
                } else if (state == Call.STATE_RINGING) {
                    val notification = notificationManager.buildIncomingCallNotification(name, number, photoUri)
                    startForegroundCompat(notificationManager.INCOMING_NOTIFICATION_ID, notification)
                    notificationManager.updateIncomingNotification(notification)
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

    /**
     * Called by Telecom when a new call is added to the system (Incoming or Outgoing).
     * This is the entry point for starting the Foreground Service.
     */
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callRepository.updateCall(call)
        callRepository.registerCallback(call)
        
        if (call.state == Call.STATE_RINGING) {
            val number = call.details.handle?.schemeSpecificPart ?: ""
            val notification = notificationManager.buildIncomingCallNotification("Incoming Call", number, null)
            startForegroundCompat(notificationManager.INCOMING_NOTIFICATION_ID, notification)
            notificationManager.updateIncomingNotification(notification)
        } else if (call.state == Call.STATE_DIALING || call.state == Call.STATE_CONNECTING || call.state == Call.STATE_SELECT_PHONE_ACCOUNT) {
            val number = call.details.handle?.schemeSpecificPart ?: ""
            val notification = notificationManager.buildOngoingCallNotification("Outgoing Call", number, false, null, null, true)
            startForegroundCompat(notificationManager.ONGOING_NOTIFICATION_ID, notification)
            notificationManager.updateOngoingNotification(notification)
        }
    }

    /**
     * Called by Telecom when a call is disconnected and removed.
     * We must clean up our state, stop the foreground service, and cancel the notification.
     */
    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callRepository.unregisterCallback(call)
        callRepository.updateCall(null)
        stopForeground(true)
        notificationManager.cancelAllNotifications()
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
