package com.android.voyageur

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityAdditionalTests {

  @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  fun testPlacesInitialization() {
    activityRule.scenario.onActivity { activity ->
      try {
        // Simulate places initialization
        val placesClient = activity.placesClient
      } catch (e: Exception) {
        assert(false) { "Places initialization failed: ${e.message}" }
      }
    }
  }

  @Test
  fun testFirebaseInitialization() {
    activityRule.scenario.onActivity { activity ->
      try {
        // Simulate Firebase initialization
        val firebaseApp = com.google.firebase.FirebaseApp.getInstance()
      } catch (e: Exception) {
        assert(false) { "Firebase initialization failed: ${e.message}" }
      }
    }
  }
}
