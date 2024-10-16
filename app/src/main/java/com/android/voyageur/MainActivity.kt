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
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      FirebaseApp.initializeApp(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    setContent {
      VoyageurTheme {
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              VoyageurApp()
            }
      }
    }
  }
}
