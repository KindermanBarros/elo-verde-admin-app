package com.eloverde.admin

import com.google.firebase.messaging.FirebaseMessagingService

class AdminMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AdminDeviceRegistration.register(token)
    }
}
