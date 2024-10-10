package com.android.voyageur.ui.authentication

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.MainActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest : TestCase() {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  // The IntentsTestRule is not reliable.

  @Before
  fun setUp() {
    Intents.init()
  }

  // Release Intents after each test
  @After
  fun tearDown() {
    Intents.release()
  }
}
