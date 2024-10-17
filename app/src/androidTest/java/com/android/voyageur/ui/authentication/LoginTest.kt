package com.android.voyageur.ui.authentication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class LoginTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    firebaseAuth = mock(FirebaseAuth::class.java)
    navigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent { SignInScreen(navigationActions) }
  }

  @Test
  fun signInScreen_hasRequiredComponents() {
    composeTestRule.onNodeWithTag("signInScreenScaffold").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signInScreenColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("googleLogo", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("googleSignInButtonText", useUnmergedTree = true).assertExists()
  }

  @Test
  fun signInScreen_displaysCorrectTitle() {
    composeTestRule.onNodeWithTag("loginTitle").assertTextEquals("Welcome")
  }

  @Test
  fun googleSignInButton_hasCorrectText() {
    composeTestRule
        .onNodeWithTag("googleSignInButtonText", useUnmergedTree = true)
        .assertExists()
        .assertTextEquals("Sign in with Google")
  }

  @Test
  fun googleSignInButton_isClickable() {
    composeTestRule.onNodeWithTag("loginButton").assertHasClickAction()
  }

  @Test
  fun logo_isDisplayedCorrectly() {
    composeTestRule.onNodeWithTag("appLogo").assertIsDisplayed()
  }

  @Test
  fun testGoogleSignInOptions_initialization() {
    val token = "fake_client_id"
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(token)
            .requestEmail()
            .build()

    assertEquals(token, gso.serverClientId)
  }
}
