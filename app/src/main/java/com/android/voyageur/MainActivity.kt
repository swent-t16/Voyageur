package com.android.voyageur

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.voyageur.resources.C
import com.android.voyageur.ui.theme.VoyageurTheme
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * MainActivity is the entry point of the application. It initializes Firebase, Places API, and sets
 * up the UI using Jetpack Compose. It also handles network connectivity changes and displays
 * notifications when the device is offline.
 */
class MainActivity : ComponentActivity() {
  private lateinit var placesClient: PlacesClient

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Create the notification channel
    createNotificationChannel()

    try {
      FirebaseApp.initializeApp(this)
      Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
      placesClient = Places.createClient(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // Check and request notification permission for Android 13 and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
          PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
      }
    }

    setContent {
      VoyageurTheme {
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              val status by connectivityState()
              val isConnected = status == ConnectionState.Available

              // Trigger notification when not connected
              LaunchedEffect(isConnected) {
                if (!isConnected) {
                  showNotification()
                }
              }

              Column {
                Row {
                  if (!isConnected) {
                    // Display a red banner at the top of the screen
                    Text(
                        stringResource(R.string.no_internet_connection),
                        modifier =
                            Modifier.fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.error),
                        color = MaterialTheme.colorScheme.onError,
                        textAlign = TextAlign.Center)
                  }
                }
                Row { VoyageurApp(placesClient) }
              }
            }
      }
    }
  }

  /**
   * Creates a notification channel for devices running Android O and above. This is required to
   * display notifications.
   */
  @SuppressLint("ObsoleteSdkInt")
  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channelId = "voyageur_notifications"
      val channelName = "Voyageur Notifications"
      val channelDescription = "Notifications from Voyageur App"
      val importance = NotificationManager.IMPORTANCE_DEFAULT

      val channel =
          NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
          }

      val notificationManager: NotificationManager =
          getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  /**
   * Displays a notification indicating no internet connection. The notification is only shown if
   * the app has the POST_NOTIFICATIONS permission.
   */
  private fun showNotification() {
    val channelId = "voyageur_notifications" // Same as the one used when creating the channel
    val notificationId = 1 // Unique ID for your notification

    // Create an explicit intent for an Activity in your app
    val intent =
        Intent(this, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    val pendingIntent =
        PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val builder =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("No Internet Connection")
            .setContentText("Please check your network settings.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss the notification when the user taps on it

    with(NotificationManagerCompat.from(this)) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED) {
        notify(notificationId, builder.build())
      }
    }
  }

  /**
   * Handles the result of the permission request for notifications (Android 13+). If the permission
   * is granted, notifications can be shown. If the permission is denied, the user is informed that
   * notifications won't work without permission.
   */
  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 100) {
      if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        // Permission granted
      } else {
        // Permission denied
      }
    }
  }
}
