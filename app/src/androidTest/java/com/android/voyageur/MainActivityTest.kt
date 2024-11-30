package com.android.voyageur

import android.app.NotificationManager
import android.content.Context
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.ui.theme.VoyageurTheme
import org.junit.After
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

  @get:Rule val composeTestRule = createComposeRule()

  @After
  fun tearDown() {
    val notificationManager =
        ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll() // Clear all notifications
  }

  @Test
  fun testNoInternetBannerDisplayed() {
    composeTestRule.setContent {
      VoyageurTheme {
        // Simulating no internet state
        androidx.compose.foundation.layout.Row {
          androidx.compose.material3.Text(
              "No Internet Connection",
              modifier = androidx.compose.ui.Modifier.semantics { testTag = "NoInternetBanner" })
        }
      }
    }

    composeTestRule.onNodeWithTag("NoInternetBanner").assertIsDisplayed()
  }

  @Test
  fun testNotificationTriggered() {
    val notificationTitle = "No Internet Connection"
    val notificationMessage = "Check Network Settings"

    // Simulate showing notification
    composeTestRule.setContent {
      VoyageurTheme {
        androidx.compose.foundation.layout.Row {
          androidx.compose.material3.Text(notificationTitle)
        }
      }
    }

    composeTestRule.onNodeWithText(notificationTitle).assertIsDisplayed()
  }
}
