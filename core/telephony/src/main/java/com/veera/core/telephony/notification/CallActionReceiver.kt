package com.veera.core.telephony.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.veera.core.telephony.repository.CallRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class CallActionReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CallActionReceiverEntryPoint {
        fun callRepository(): CallRepository
    }

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CallActionReceiverEntryPoint::class.java
        )
        val callRepository = entryPoint.callRepository()

        when (intent.action) {
            "ACTION_ANSWER" -> {
                callRepository.answerCall()
                // Don't cancel notification here, let the service update it to ACTIVE state notification
            }
            "ACTION_DECLINE", "ACTION_DISCONNECT" -> {
                callRepository.disconnectCall()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(1001) // NOTIFICATION_ID
            }
            "ACTION_TOGGLE_SPEAKER" -> {
                callRepository.toggleSpeaker()
            }
        }
    }
}
