package com.android.voyageur.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
  private const val CHANNEL_ID = "voyageur_notifications"
  private const val CHANNEL_NAME = "Voyageur Notifications"
  private const val CHANNEL_DESCRIPTION = "Notifications from Voyageur App"

  /** Creates a notification channel for Android O and above. */
  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel =
          NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
          }
      val notificationManager: NotificationManager =
          context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  /**
   * Displays a notification with the provided details.
   *
   * @param context The context from which the notification is sent.
   * @param notificationId Unique ID for the notification.
   * @param title Title of the notification.
   * @param text Content text of the notification.
   * @param iconResId Resource ID of the notification icon.
   * @param intent Optional intent to be triggered when the notification is clicked.
   */
  fun showNotification(
      context: Context,
      notificationId: Int,
      title: String,
      text: String,
      iconResId: Int,
      intent: Intent? = null
  ) {
    val pendingIntent =
        intent?.let {
          PendingIntent.getActivity(
              context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

    val builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconResId)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

    pendingIntent?.let { builder.setContentIntent(it) }

    with(NotificationManagerCompat.from(context)) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
          context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
              PackageManager.PERMISSION_GRANTED) {
        notify(notificationId, builder.build())
      }
    }
  }
}
