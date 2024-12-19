package com.android.voyageur.ui.authentication

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.R
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.TopLevelDestinations
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Creates and remembers a Firebase authentication launcher for Google Sign-In. This composable
 * function handles the authentication flow with Firebase using Google Sign-In.
 *
 * @param onAuthComplete Callback function invoked when authentication is successful
 * @param onAuthError Callback function invoked when authentication fails
 * @return ManagedActivityResultLauncher for handling the Google Sign-In intent
 */
@Composable
private fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
  val scope = rememberCoroutineScope()

  return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
      val account = task.getResult(ApiException::class.java)!!
      val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
      scope.launch {
        val authResult = Firebase.auth.signInWithCredential(credential).await()
        onAuthComplete(authResult)
      }
    } catch (e: ApiException) {
      onAuthError(e)
    }
  }
}

/**
 * Authentication wrapper composable that handles the authentication state. This component checks if
 * the user is authenticated and navigates accordingly.
 *
 * @param navigationActions Navigation handler for the app
 */
@Composable
fun AuthenticationWrapper(navigationActions: NavigationActions) {
  val user by remember { mutableStateOf(Firebase.auth.currentUser) }

  LaunchedEffect(user) {
    if (user != null) {
      navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
    }
  }

  if (user == null) {
    SignInScreen(navigationActions)
  }
}

/**
 * The main sign-in screen composable that displays the login interface. Features a scrollable
 * layout with app logo, welcome text, and Google Sign-In button.
 *
 * @param navigationActions Navigation handler for the app
 */
@Composable
fun SignInScreen(navigationActions: NavigationActions) {
  val context = LocalContext.current
  var user by remember { mutableStateOf(Firebase.auth.currentUser) }
  val success = stringResource(R.string.login_successful)
  val failure = stringResource(R.string.login_failed)
  val scrollState = rememberScrollState()

  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            user = result.user
            Toast.makeText(context, success, Toast.LENGTH_LONG).show()
            navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
          },
          onAuthError = {
            user = null
            Toast.makeText(context, failure, Toast.LENGTH_LONG).show()
          })
  val token = stringResource(R.string.default_web_client_id)

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("signInScreenScaffold"),
  ) { padding ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState) // Makes the column scrollable
                .testTag("signInScreenColumn"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      val logoWidth = 250.dp

      // App Logo
      Image(
          painter = painterResource(id = R.drawable.app_logo),
          contentDescription = stringResource(R.string.app_logo_content_description),
          contentScale = ContentScale.Crop,
          modifier =
              Modifier.width(logoWidth)
                  .height(logoWidth)
                  .clip(RoundedCornerShape(8.dp))
                  .testTag("appLogo"))

      Spacer(modifier = Modifier.height(16.dp))

      // Welcome Text
      Text(
          modifier = Modifier.testTag("loginTitle"),
          text = stringResource(R.string.welcome_text),
          style = MaterialTheme.typography.headlineLarge.copy(fontSize = 57.sp, lineHeight = 64.sp),
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center)

      Spacer(modifier = Modifier.height(48.dp))

      // Google Sign-In Button
      GoogleSignInButton(
          onSignInClick = {
            if (Firebase.auth.uid.orEmpty().isEmpty()) {
              val gso =
                  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                      .requestIdToken(token)
                      .requestEmail()
                      .build()
              val googleSignInClient = GoogleSignIn.getClient(context, gso)
              launcher.launch(googleSignInClient.signInIntent)
            }
          },
          buttonWidth = logoWidth)

      // Bottom spacing for better scrolling experience
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

/**
 * A custom Google Sign-In button composable that maintains Google's design guidelines. This button
 * includes the Google logo and sign-in text in a styled button.
 *
 * @param onSignInClick Callback function invoked when the button is clicked
 * @param buttonWidth Width of the button in Dp
 */
@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit, buttonWidth: Dp) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.White),
      shape = RoundedCornerShape(50),
      border = BorderStroke(1.dp, Color.LightGray),
      modifier = Modifier.padding(8.dp).height(48.dp).width(buttonWidth).testTag("loginButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().testTag("googleSignInButtonRow")) {
              // Google Logo
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = stringResource(R.string.google_logo_content_description),
                  modifier = Modifier.size(30.dp).padding(end = 8.dp).testTag("googleLogo"))

              // Sign-in Text
              Text(
                  text = stringResource(R.string.sign_in_with_google),
                  color = Color.Gray,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.testTag("googleSignInButtonText"))
            }
      }
}
