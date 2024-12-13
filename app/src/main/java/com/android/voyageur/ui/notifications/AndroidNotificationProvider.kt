package com.android.voyageur.ui.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import com.android.voyageur.R

open class AndroidNotificationProvider(private val context: Context) : NotificationProvider {
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