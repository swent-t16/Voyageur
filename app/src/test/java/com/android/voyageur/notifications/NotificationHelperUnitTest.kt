package com.android.voyageur.ui.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
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
    assertEquals("Voyageur Notifications", channel.name)
    assertEquals("Notifications from Voyageur App", channel.description)
  }

  @Test
  fun `showNotification displays notification correctly`() {
    // Arrange
    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationId = 1
    val title = "Test Title"
    val text = "Test Text"
    val iconResId = android.R.drawable.ic_dialog_info
    val intent = Intent(context, NotificationHelperTest::class.java)
    val notificationManagerCompat = NotificationManagerCompat.from(context)

    // Clear all existing notifications using NotificationManager
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll() // Reset notifications

    // Act
    NotificationHelper.showNotification(
        context, notificationId, title, text, iconResId, intent, notificationManagerCompat)

    // Assert
    val shadowNotificationManager = Shadows.shadowOf(notificationManager)
    val postedNotifications = shadowNotificationManager.allNotifications
    assertNotNull(postedNotifications)
    assertEquals(1, postedNotifications.size)
    val notification = postedNotifications[0]
    assertEquals(title, notification.extras.getString("android.title"))
    assertEquals(text, notification.extras.getString("android.text"))
  }
}
