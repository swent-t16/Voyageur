package com.android.voyageur.ui.notifications

import android.content.Context

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
  /**
   * Displays a notification for a new trip invite.
   *
   * @param tripName The name of the trip being invited.
   * @param senderName The name of the user who sent the invite.
   */
  fun showNewTripInviteNotification(tripName: String, senderName: String)

  val context: Context
}
