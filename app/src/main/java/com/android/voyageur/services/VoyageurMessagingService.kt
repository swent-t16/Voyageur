package com.android.voyageur.services

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.voyageur.R
import com.android.voyageur.ui.notifications.AndroidNotificationProvider
import com.android.voyageur.ui.notifications.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch

class VoyageurMessagingService : FirebaseMessagingService() {
  private val TAG = "VoyageurMessagingService"
  private val db = FirebaseFirestore.getInstance()

  override fun onCreate() {
    super.onCreate()
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    NotificationHelper.createNotificationChannel(this, notificationManager)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "From: ${remoteMessage.from}")

    val notificationProvider = AndroidNotificationProvider(this)

    remoteMessage.data.let { data ->
      when (data["type"]) {
        "friend_request" -> {
          val senderName = data["senderName"] ?: getString(R.string.unknown)
          notificationProvider.showNewFriendRequestNotification(senderName)
        }
        "friend_request_accepted" -> {
          val acceptorName = data["acceptorName"] ?: getString(R.string.unknown)
          notificationProvider.showFriendRequestAcceptedNotification(acceptorName)
        }
      }
    }

    remoteMessage.notification?.let { notification ->
      Log.d(TAG, "Message Notification Body: ${notification.body}")
      notification.body?.let { body ->
        NotificationHelper.showNotification(
            context = this,
            notificationId = System.currentTimeMillis().toInt(),
            title = notification.title ?: getString(R.string.app_name),
            text = body,
            iconResId = R.drawable.app_logo)
      }
    }
  }

  override fun onNewToken(token: String) {
    Log.d(TAG, "Refreshed token: $token")

    // Store token in SharedPreferences
    getSharedPreferences("voyageur_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("fcm_token", token)
        .apply()

    // Update token in Firestore if user is logged in
    FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
      ProcessLifecycleOwner.get().lifecycleScope.launch { updateUserToken(userId, token) }
    }
  }

  private fun updateUserToken(userId: String, token: String) {
    db.collection("users")
        .document(userId)
        .update("fcmToken", token)
        .addOnSuccessListener { Log.d(TAG, "FCM token updated successfully") }
        .addOnFailureListener { e -> Log.e(TAG, "Error updating FCM token", e) }
  }
}
