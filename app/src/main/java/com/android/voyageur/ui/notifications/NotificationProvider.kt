package com.android.voyageur.ui.notifications

interface NotificationProvider {
  fun showNewFriendRequestNotification(senderName: String)

  fun showFriendRequestAcceptedNotification(acceptorName: String)
}
