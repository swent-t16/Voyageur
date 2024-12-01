package com.android.voyageur

import android.Manifest
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
import com.android.voyageur.resources.C
import com.android.voyageur.ui.notifications.NotificationHelper
import com.android.voyageur.ui.theme.VoyageurTheme
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActivity : ComponentActivity() {
  lateinit var placesClient: PlacesClient

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize the notification channel
    NotificationHelper.createNotificationChannel(this)

    try {
      FirebaseApp.initializeApp(this)
      Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
      placesClient = Places.createClient(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // Check and request notification permission for Android 13 and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
          PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
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
                  val intent =
                      Intent(this@MainActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                      }
                  NotificationHelper.showNotification(
                      context = this@MainActivity,
                      notificationId = 1,
                      title = getString(R.string.no_internet_connection),
                      text = getString(R.string.check_network_settings),
                      iconResId = R.drawable.app_logo,
                      intent = intent)
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
                                .background(color = MaterialTheme.colorScheme.error)
                                .semantics { testTag = "NoInternetBanner" },
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

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 100) {
      if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        // Permission granted; you can show notifications now
      } else {
        // Permission denied; handle accordingly
      }
    }
  }
}
