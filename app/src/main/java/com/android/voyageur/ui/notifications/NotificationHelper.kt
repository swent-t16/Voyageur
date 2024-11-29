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
import com.android.voyageur.R

object NotificationHelper {
  private const val CHANNEL_ID = "voyageur_notifications"

  /** Creates a notification channel for Android O and above. */
  fun createNotificationChannel(
      context: Context,
      notificationManager: NotificationManager? = null
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel =
          NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_name), importance)
              .apply { description = context.getString(R.string.channel_description) }
      val mgr =
          notificationManager
              ?: context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      mgr?.createNotificationChannel(channel)
          ?: run {
            // Log or handle the error gracefully
            android.util.Log.e(
                "NotificationHelper",
                "NotificationManager is null. Cannot create notification channel.")
          }
    }
  }

  /**
   * Displays a notification with the provided details and configurable priority.
   *
   * @param context The context from which the notification is sent.
   * @param notificationId Unique ID for the notification.
   * @param title Title of the notification.
   * @param text Content text of the notification.
   * @param iconResId Resource ID of the notification icon.
   * @param priority Priority level for the notification (e.g., PRIORITY_DEFAULT, PRIORITY_HIGH).
   * @param intent Optional intent to be triggered when the notification is clicked.
   * @param notificationManagerCompat Optional NotificationManagerCompat for testing.
   */
  fun showNotification(
      context: Context,
      notificationId: Int,
      title: String,
      text: String,
      iconResId: Int,
      priority: Int = NotificationCompat.PRIORITY_DEFAULT,
      intent: Intent? = null,
      notificationManagerCompat: NotificationManagerCompat? = null
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
            .setPriority(priority)
            .setAutoCancel(true)

    pendingIntent?.let { builder.setContentIntent(it) }

    val mgrCompat = notificationManagerCompat ?: NotificationManagerCompat.from(context)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED) {
      mgrCompat.notify(notificationId, builder.build())
    }
  }

  /**
   * Displays a notification for no internet connection. This method is specialized to avoid
   * redundancy in multiple places.
   */
  fun showNoInternetNotification(context: Context, iconResId: Int, intent: Intent? = null) {
    showNotification(
        context = context,
        notificationId = 1,
        title = context.getString(R.string.notification_no_internet_title),
        text = context.getString(R.string.notification_no_internet_text),
        iconResId = iconResId,
        priority = NotificationCompat.PRIORITY_HIGH, // Critical notification
        intent = intent)
  }
}
