package com.veera.call.services

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle

class CallService: ConnectionService() {
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        return CallConnection().apply {
            val address = request.address
            setInitializing()
            setActive()
        }
    }
}

class CallConnection : Connection() {
    override fun onDisconnect() {
        destroy()
    }
}