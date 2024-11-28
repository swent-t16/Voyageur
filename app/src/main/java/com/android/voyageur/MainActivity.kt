package com.android.voyageur

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import com.android.voyageur.resources.C
import com.android.voyageur.ui.theme.VoyageurTheme
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActivity : ComponentActivity() {
  private lateinit var placesClient: PlacesClient

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      FirebaseApp.initializeApp(this)
      Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
      placesClient = Places.createClient(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    setContent {
      VoyageurTheme {
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              val status by connectivityState()
              val isConnected = status === ConnectionState.Available
              Column {
                Row {
                  if (!isConnected) {
                    // display a red banner at the top of the screen
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
}
