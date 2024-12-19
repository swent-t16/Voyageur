package com.android.voyageur.ui.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import com.android.voyageur.R

/**
 * AndroidNotificationProvider is responsible for displaying notifications related to friend
 * requests.
 *
 * @param context The context used to access resources and system services.
 */
open class AndroidNotificationProvider(override val context: Context) : NotificationProvider {

  /**
   * Displays a notification for a new friend request.
   *
   * @param senderName The name of the user who sent the friend request.
   */
  override fun showNewFriendRequestNotification(senderName: String) {
    val notificationId = System.currentTimeMillis().toInt()
    NotificationHelper.showNotification(
        context = context,
        notificationId = notificationId,
        title = context.getString(R.string.new_friend_request),
        text = context.getString(R.string.friend_request_message, senderName),
        iconResId = R.drawable.app_logo,
        priority = NotificationCompat.PRIORITY_HIGH)
  }

  /**
   * Displays a notification for an accepted friend request.
   *
   * @param acceptorName The name of the user who accepted the friend request.
   */
  override fun showFriendRequestAcceptedNotification(acceptorName: String) {
    val notificationId = System.currentTimeMillis().toInt()
    NotificationHelper.showNotification(
        context = context,
        notificationId = notificationId,
        title = context.getString(R.string.friend_request_accepted),
        text = context.getString(R.string.friend_request_accepted_message, acceptorName),
        iconResId = R.drawable.app_logo,
        priority = NotificationCompat.PRIORITY_HIGH)
  }
}
