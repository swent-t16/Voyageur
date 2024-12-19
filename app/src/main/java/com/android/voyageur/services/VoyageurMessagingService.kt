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

/**
 * A Firebase Cloud Messaging service for handling incoming messages and managing FCM tokens. This
 * service processes various types of notifications, including friend requests and friend request
 * acceptance notifications.
 */
class VoyageurMessagingService : FirebaseMessagingService() {

  companion object {
    /** The type identifier for friend request notifications. */
    const val TYPE_FRIEND_REQUEST = "friend_request"

    /** The type identifier for friend request acceptance notifications. */
    const val TYPE_FRIEND_REQUEST_ACCEPTED = "friend_request_accepted"

    /** The shared preferences key for storing the FCM token. */
    const val PREFS_FCM_TOKEN = "fcm_token"

    /** The name of the shared preferences file. */
    const val PREFS_NAME = "voyageur_prefs"
    /** The type identifier for trip invite notifications. */
    const val TYPE_TRIP_INVITE = "trip_invite"
  }

  private val TAG = "VoyageurMessagingService"
  private val db = FirebaseFirestore.getInstance()

  /**
   * Initializes the notification channel for the app on service creation. Ensures that
   * notifications are properly displayed on devices running Android O and above.
   */
  override fun onCreate() {
    super.onCreate()
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    NotificationHelper.createNotificationChannel(this, notificationManager)
  }

  /**
   * Called when a new FCM message is received. Processes the message based on its type and displays
   * the appropriate notification.
   *
   * @param remoteMessage The received FCM message.
   */
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "From: ${remoteMessage.from}")

    val notificationProvider = AndroidNotificationProvider(this)

    remoteMessage.data.let { data ->
      when (data["type"]) {
        TYPE_FRIEND_REQUEST -> {
          val senderName = data["senderName"] ?: getString(R.string.unknown)
          notificationProvider.showNewFriendRequestNotification(senderName)
        }
        TYPE_FRIEND_REQUEST_ACCEPTED -> {
          val acceptorName = data["acceptorName"] ?: getString(R.string.unknown)
          notificationProvider.showFriendRequestAcceptedNotification(acceptorName)
        }
        TYPE_TRIP_INVITE -> { // Handle trip invite notifications
          val tripName = data["tripName"] ?: getString(R.string.unknown_trip)
          val senderName = data["senderName"] ?: getString(R.string.unknown)
          notificationProvider.showNewTripInviteNotification(tripName, senderName)
        }
        else -> Log.d(TAG, "Unknown message type: ${data["type"]}")
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

  /**
   * Called when a new FCM token is generated. Stores the token locally and updates it in Firestore
   * if the user is logged in.
   *
   * @param token The new FCM token.
   */
  override fun onNewToken(token: String) {
    Log.d(TAG, "Refreshed token: $token")

    // Store token in SharedPreferences
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(PREFS_FCM_TOKEN, token)
        .apply()

    // Update token in Firestore if user is logged in
    FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
      ProcessLifecycleOwner.get().lifecycleScope.launch { updateUserToken(userId, token) }
    }
  }

  /**
   * Updates the user's FCM token in Firestore.
   *
   * @param userId The ID of the user whose token is to be updated.
   * @param token The new FCM token.
   */
  private fun updateUserToken(userId: String, token: String) {
    db.collection("users")
        .document(userId)
        .update("fcmToken", token)
        .addOnSuccessListener { Log.d(TAG, "FCM token updated successfully") }
        .addOnFailureListener { e -> Log.e(TAG, "Error updating FCM token", e) }
  }
}
