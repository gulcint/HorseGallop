package com.adincountry

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushService : FirebaseMessagingService() {
  override fun onNewToken(token: String) {
    super.onNewToken(token)
    // TODO send token to backend
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    // TODO show notification with deep link
  }
}
