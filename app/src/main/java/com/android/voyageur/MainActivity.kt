package com.android.voyageur

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Main activity of the Voyageur app. Initializes Firebase, notifications, and Google Places API.
 * Displays the main UI content and handles connectivity state.
 */
class MainActivity : ComponentActivity() {
  lateinit var placesClient: PlacesClient
  private val TAG = "MainActivity"
  private val NOTIFICATION_PERMISSION_CODE = 100

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initializeFirebase()
    initializeNotifications()
    initializePlaces()

    setContent {
      VoyageurTheme {
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              val status by connectivityState()
              val isConnected = status == ConnectionState.Available

              Column {
                Row {
                  if (!isConnected) {
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

  /** Initializes Firebase services. */
  private fun initializeFirebase() {
    getFCMToken()
    try {
      FirebaseApp.initializeApp(this)
    } catch (e: Exception) {
      Log.e(TAG, "Error initializing Firebase", e)
    }
  }

  /** Initializes notification channels and requests notification permissions if needed. */
  private fun initializeNotifications() {
    NotificationHelper.createNotificationChannel(this)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
          PackageManager.PERMISSION_GRANTED) {
        requestPermissions(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
      }
    }
  }

  /** Initializes Google Places API. */
  private fun initializePlaces() {
    try {
      Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
      placesClient = Places.createClient(this)
    } catch (e: Exception) {
      Log.e(TAG, "Error initializing Places", e)
    }
  }

  /** Retrieves the FCM token and updates it in Firestore. */
  private fun getFCMToken() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
      if (!task.isSuccessful) {
        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
        return@addOnCompleteListener
      }

      val token = task.result
      // Add debug log to verify token
      Log.d(TAG, "FCM Token: $token")

      // Save token to SharedPreferences
      getSharedPreferences("voyageur_prefs", MODE_PRIVATE)
          .edit()
          .putString("fcm_token", token)
          .apply()

      // Get current user and update token immediately
      FirebaseAuth.getInstance().currentUser?.let { user -> updateUserToken(user.uid, token) }
    }
  }

  /**
   * Updates the FCM token for the specified user in Firestore.
   *
   * @param userId The ID of the user.
   * @param token The FCM token to update.
   */
  private fun updateUserToken(userId: String, token: String) {
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .update("fcmToken", token)
        .addOnSuccessListener { Log.d(TAG, "FCM token updated in Firestore") }
        .addOnFailureListener { e -> Log.e(TAG, "Error updating FCM token in Firestore", e) }
  }

  /**
   * Handles the result of permission requests.
   *
   * @param requestCode The request code passed in requestPermissions.
   * @param permissions The requested permissions.
   * @param grantResults The grant results for the corresponding permissions.
   */
  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == NOTIFICATION_PERMISSION_CODE) {
      if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        // Permission was granted
        Log.d(TAG, "Notification permission granted")
      } else {
        // Permission was denied
        Log.d(TAG, "Notification permission denied")
      }
    }
  }
}
