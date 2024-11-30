package com.android.voyageur

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.ui.notifications.NotificationHelper
import org.junit.After
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityIntegrationTest {

  private val context = ApplicationProvider.getApplicationContext<Context>()

  @After
  fun tearDown() {
    val notificationManager =
        ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll() // Clear all notifications
  }

  @Test
  fun testNotificationOnNoInternet() {
    val intent =
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

    try {
      NotificationHelper.showNotification(
          context = context,
          notificationId = 1,
          title = "No Internet Connection",
          text = "Check Network Settings",
          iconResId = R.drawable.app_logo,
          intent = intent)
    } catch (e: Exception) {
      fail("Notification not triggered: ${e.message}")
    }
  }
}
