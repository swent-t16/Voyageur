package com.android.voyageur.ui.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.android.voyageur.MainActivity
import com.android.voyageur.R
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidNotificationProviderTest {

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var context: Context
  private lateinit var notificationProvider: AndroidNotificationProvider
  private lateinit var notificationManager: NotificationManager
  private lateinit var uiDevice: UiDevice
  private lateinit var activityScenario: ActivityScenario<MainActivity>

  private val NOTIFICATION_TIMEOUT = 5000L
  private val TEST_SENDER_NAME = "John Doe"
  private val TEST_ACCEPTOR_NAME = "Jane Smith"

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    notificationProvider = AndroidNotificationProvider(context)
    notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    // Clear existing notifications
    notificationManager.cancelAll()

    // Initialize notification channel
    NotificationHelper.createNotificationChannel(context)

    // Launch MainActivity
    val intent =
        Intent(context, MainActivity::class.java).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    activityScenario = ActivityScenario.launch(intent)

    // Ensure we're starting from the home screen
    uiDevice.pressHome()
  }

  @After
  fun tearDown() {
    notificationManager.cancelAll()
    activityScenario.close()
  }

  @Test
  fun testNewFriendRequestNotification() {
    // Wait for activity to be fully created
    SystemClock.sleep(1000)

    // Send the notification
    activityScenario.onActivity { activity ->
      notificationProvider.showNewFriendRequestNotification(TEST_SENDER_NAME)
    }

    // Wait for notification processing
    SystemClock.sleep(1000)

    // Open notification shade
    uiDevice.openNotification()

    // Wait for notification to appear
    val notificationTitle = context.getString(R.string.new_friend_request)
    val notificationExists =
        uiDevice.wait(Until.hasObject(By.text(notificationTitle)), NOTIFICATION_TIMEOUT)
    assertTrue("Notification should be visible", notificationExists)

    // Verify notification content
    val titleObject = uiDevice.findObject(By.text(notificationTitle))
    assertNotNull("Notification title should exist", titleObject)

    val expectedMessage = context.getString(R.string.friend_request_message, TEST_SENDER_NAME)
    val messageObject = uiDevice.findObject(By.textContains(TEST_SENDER_NAME))
    assertNotNull("Notification text should exist", messageObject)
    assertTrue(
        "Notification should contain sender name", messageObject.text.contains(TEST_SENDER_NAME))
  }

  @Test
  fun testFriendRequestAcceptedNotification() {
    // Wait for activity to be fully created
    SystemClock.sleep(1000)

    // Send the notification
    activityScenario.onActivity { activity ->
      notificationProvider.showFriendRequestAcceptedNotification(TEST_ACCEPTOR_NAME)
    }

    // Wait for notification processing
    SystemClock.sleep(1000)

    // Open notification shade
    uiDevice.openNotification()

    // Wait for notification to appear
    val notificationTitle = context.getString(R.string.friend_request_accepted)
    val notificationExists =
        uiDevice.wait(Until.hasObject(By.text(notificationTitle)), NOTIFICATION_TIMEOUT)
    assertTrue("Notification should be visible", notificationExists)

    // Verify notification content
    val titleObject = uiDevice.findObject(By.text(notificationTitle))
    assertNotNull("Notification title should exist", titleObject)

    val expectedMessage =
        context.getString(R.string.friend_request_accepted_message, TEST_ACCEPTOR_NAME)
    val messageObject = uiDevice.findObject(By.textContains(TEST_ACCEPTOR_NAME))
    assertNotNull("Notification text should exist", messageObject)
    assertTrue(
        "Notification should contain acceptor name",
        messageObject.text.contains(TEST_ACCEPTOR_NAME))
  }

  @Test
  fun testNotificationPriority() {
    // Wait for activity to be fully created
    SystemClock.sleep(1000)

    // Send the notification
    activityScenario.onActivity { activity ->
      notificationProvider.showNewFriendRequestNotification(TEST_SENDER_NAME)
    }

    // Wait for notification processing
    SystemClock.sleep(1000)

    // Get active notifications
    val notifications = notificationManager.activeNotifications
    assertTrue("Should have at least one notification", notifications.isNotEmpty())

    // Verify priority
    val notification = notifications[0]
    assertTrue(
        "Notification should have high priority",
        notification.notification.priority == NotificationCompat.PRIORITY_HIGH)
  }

  @Test
  fun testNotificationIcon() {
    // Wait for activity to be fully created
    SystemClock.sleep(1000)

    // Send the notification
    activityScenario.onActivity { activity ->
      notificationProvider.showNewFriendRequestNotification(TEST_SENDER_NAME)
    }

    // Wait for notification processing
    SystemClock.sleep(1000)

    // Get active notifications
    val notifications = notificationManager.activeNotifications
    assertTrue("Should have at least one notification", notifications.isNotEmpty())

    // Verify icon
    val notification = notifications[0]
    assertTrue(
        "Notification should have correct icon",
        notification.notification.smallIcon.resId == R.drawable.app_logo)
  }

  @Test
  fun testNewTripInviteNotification() {
    // Wait for activity to be fully created
    SystemClock.sleep(1000)

    // Send the notification
    val testTripName = "Beach Adventure"
    activityScenario.onActivity { activity ->
      notificationProvider.showNewTripInviteNotification(testTripName, TEST_SENDER_NAME)
    }

    // Wait for notification processing
    SystemClock.sleep(1000)

    // Open notification shade
    uiDevice.openNotification()

    // Wait for notification to appear
    val notificationTitle = context.getString(R.string.new_trip_invite)
    val notificationExists =
        uiDevice.wait(Until.hasObject(By.text(notificationTitle)), NOTIFICATION_TIMEOUT)
    assertTrue("Notification should be visible", notificationExists)

    // Verify notification content
    val titleObject = uiDevice.findObject(By.text(notificationTitle))
    assertNotNull("Notification title should exist", titleObject)

    val expectedMessage =
        context.getString(R.string.trip_invite_message, TEST_SENDER_NAME, testTripName)
    val messageObject = uiDevice.findObject(By.textContains(testTripName))
    assertNotNull("Notification text should exist", messageObject)
    assertTrue(
        "Notification should contain trip name and sender name",
        messageObject.text.contains(testTripName) && messageObject.text.contains(TEST_SENDER_NAME))
  }

  @Test
  fun testTripInviteNotificationIcon() {
    // Wait for activity to be fully created
    SystemClock.sleep(1000)

    // Send the notification
    val testTripName = "Forest Escape"
    activityScenario.onActivity { activity ->
      notificationProvider.showNewTripInviteNotification(testTripName, TEST_SENDER_NAME)
    }

    // Wait for notification processing
    SystemClock.sleep(1000)

    // Get active notifications
    val notifications = notificationManager.activeNotifications
    assertTrue("Should have at least one notification", notifications.isNotEmpty())

    // Verify icon
    val notification = notifications[0]
    assertTrue(
        "Notification should have correct icon",
        notification.notification.smallIcon.resId == R.drawable.app_logo)
  }
}
