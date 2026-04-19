package com.veera.core.telephony.service

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
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

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        observeAudioRequests()
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
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callRepository.unregisterCallback(call)
        callRepository.updateCall(null)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        callRepository.updateAudioState(audioState)
    }
}
