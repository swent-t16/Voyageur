package com.android.voyageur.ui.notifications

/** NotificationProvider is an interface for displaying notifications. */
interface NotificationProvider {

  /**
   * Displays a notification for a new friend request.
   *
   * @param senderName The name of the user who sent the friend request.
   */
  fun showNewFriendRequestNotification(senderName: String)

  /**
   * Displays a notification for an accepted friend request.
   *
   * @param acceptorName The name of the user who accepted the friend request.
   */
  fun showFriendRequestAcceptedNotification(acceptorName: String)
}
