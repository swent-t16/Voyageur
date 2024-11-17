package com.android.voyageur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.android.voyageur.resources.C
import com.android.voyageur.ui.theme.VoyageurTheme
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
  private lateinit var placesClient: PlacesClient
  private lateinit var generativeModel: GenerativeModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      FirebaseApp.initializeApp(this)
      Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
      placesClient = Places.createClient(this)
      generativeModel =
          GenerativeModel(
              // Specify a Gemini model appropriate for your use case
              modelName = "gemini-1.5-flash-latest",
              // Access your API key as a Build Configuration variable (see "Set up your API
              // key" above)
              apiKey = BuildConfig.GEMINI_API_KEY)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    setContent {
      VoyageurTheme {
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              VoyageurApp(placesClient, generativeModel)
            }
      }
    }
  }
}
