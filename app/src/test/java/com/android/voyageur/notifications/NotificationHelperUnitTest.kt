package com.android.voyageur.ui.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // Use Android 28+ for NotificationChannel testing
class NotificationHelperTest {

  @Test
  fun `createNotificationChannel creates channel correctly`() {
    // Arrange
    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val shadowNotificationManager = Shadows.shadowOf(notificationManager)

    // Act
    NotificationHelper.createNotificationChannel(context)

    // Assert
    val channels = notificationManager.notificationChannels
    assertNotNull(channels)
    assertEquals(1, channels.size) // Expect exactly one channel
    val channel = channels[0]
    assertEquals("voyageur_notifications", channel.id)
    assertEquals(context.getString(R.string.channel_name), channel.name)
    assertEquals(context.getString(R.string.channel_description), channel.description)
  }

  @Test
  fun `showNotification displays notification with default priority correctly`() {
    // Arrange
    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationId = 1
    val title = "Test Title"
    val text = "Test Text"
    val iconResId = android.R.drawable.ic_dialog_info
    val intent = Intent(context, NotificationHelperTest::class.java)
    val notificationManagerCompat = NotificationManagerCompat.from(context)

    // Clear all existing notifications
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll() // Reset notifications

    // Act
    NotificationHelper.showNotification(
        context = context,
        notificationId = notificationId,
        title = title,
        text = text,
        iconResId = iconResId,
        priority = NotificationManager.IMPORTANCE_DEFAULT, // Default priority
        intent = intent,
        notificationManagerCompat = notificationManagerCompat)

    // Assert
    val shadowNotificationManager = Shadows.shadowOf(notificationManager)
    val postedNotifications = shadowNotificationManager.allNotifications
    assertNotNull(postedNotifications)
    assertEquals(1, postedNotifications.size)
    val notification = postedNotifications[0]
    assertEquals(title, notification.extras.getString("android.title"))
    assertEquals(text, notification.extras.getString("android.text"))
  }

  @Test
  fun `showNotification displays notification with high priority correctly`() {
    // Arrange
    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationId = 2
    val title = "High Priority Test Title"
    val text = "High Priority Test Text"
    val iconResId = android.R.drawable.ic_dialog_info
    val intent = Intent(context, NotificationHelperTest::class.java)
    val notificationManagerCompat = NotificationManagerCompat.from(context)

    // Clear all existing notifications
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll() // Reset notifications

    // Act
    NotificationHelper.showNotification(
        context = context,
        notificationId = notificationId,
        title = title,
        text = text,
        iconResId = iconResId,
        priority = NotificationManager.IMPORTANCE_HIGH, // High priority
        intent = intent,
        notificationManagerCompat = notificationManagerCompat)

    // Assert
    val shadowNotificationManager = Shadows.shadowOf(notificationManager)
    val postedNotifications = shadowNotificationManager.allNotifications
    assertNotNull(postedNotifications)
    assertEquals(1, postedNotifications.size)
    val notification = postedNotifications[0]
    assertEquals(title, notification.extras.getString("android.title"))
    assertEquals(text, notification.extras.getString("android.text"))
    assertEquals(NotificationManager.IMPORTANCE_HIGH, notification.priority)
  }
}
