package com.android.voyageur.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(userViewModel: UserViewModel, navigationActions: NavigationActions) {
  // Observing user and loading state from the UserViewModel
  val user by userViewModel.user.collectAsState()
  val isLoading by userViewModel.isLoading.collectAsState()

  // Firebase current user
  val currentUser = FirebaseAuth.getInstance().currentUser

  var isSigningOut by remember { mutableStateOf(false) }

  // Handle sign-out with navigation in a LaunchedEffect
  if (isSigningOut) {
    LaunchedEffect(isSigningOut) {
      userViewModel.signOutUser()
      delay(300)
      navigationActions.navigateTo(Route.AUTH)
    }
  } else {
    if (currentUser != null) {
      if (user == null && !isLoading) {
        // Load user data if not already loaded
        userViewModel.loadUser(currentUser.uid, currentUser).also {
          currentUser.displayName?.let { name ->
            userViewModel.updateUser(
                userViewModel.user.value?.apply { this.name = name }
                    ?: User(id = currentUser.uid, name = name))
          }
          currentUser.photoUrl?.let { photoUrl ->
            userViewModel.updateUser(
                userViewModel.user.value?.apply { this.profilePicture = photoUrl.toString() }
                    ?: User(id = currentUser.uid, profilePicture = photoUrl.toString()))
          }
        }
      }
    } else {
      // Navigate to AUTH if no current user is logged in
      navigationActions.navigateTo(Route.AUTH)
    }
  }

  // Main Scaffold layout for ProfileScreen with Bottom Navigation
  Scaffold(
      modifier = Modifier.testTag("profileScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
        )
      },
      content = { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize().padding(paddingValues).testTag("profileScreenContent"),
            contentAlignment = Alignment.Center) {
              if (isSigningOut) {
                CircularProgressIndicator(modifier = Modifier.testTag("signingOutIndicator"))
              } else if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
              } else {
                user?.let { userData ->
                  ProfileContent(userData = userData, onSignOut = { isSigningOut = true })
                }
                    ?: run {
                      Text("No user data available", modifier = Modifier.testTag("noUserData"))
                    }
              }
            }
      })
}

@Composable
fun ProfileContent(userData: User, onSignOut: () -> Unit) {
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("profileContent"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Display the profile picture if available
        if (userData.profilePicture.isNotEmpty()) {
          Image(
              painter = rememberAsyncImagePainter(model = userData.profilePicture),
              contentDescription = "Profile Picture",
              modifier = Modifier.size(128.dp).clip(CircleShape).testTag("profilePicture"))
        } else {
          Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "Default Profile Picture",
              modifier = Modifier.size(128.dp).testTag("defaultProfilePicture"))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display user name and email
        Text(
            text = "Name: ${userData.name.takeIf { it.isNotEmpty() } ?: "No name available"}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("userName"))
        Text(
            text = "Email: ${userData.email.takeIf { it.isNotEmpty() } ?: "No email available"}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("userEmail"))

        Spacer(modifier = Modifier.height(16.dp))

        // Sign out button
        Button(onClick = onSignOut, modifier = Modifier.testTag("signOutButton")) {
          Text(text = "Sign Out")
        }
      }
}
