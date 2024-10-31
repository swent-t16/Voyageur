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

@Composable
fun ProfileScreen(userViewModel: UserViewModel, navigationActions: NavigationActions) {
  // Observe user and loading state from UserViewModel
  val user by userViewModel.user.collectAsState()
  val isLoading by userViewModel.isLoading.collectAsState()

  var isSigningOut by remember { mutableStateOf(false) }

  // Navigate to AUTH if user is null and not loading
  if (user == null && !isLoading) {
    LaunchedEffect(Unit) { navigationActions.navigateTo(Route.AUTH) }
    return // Exit composable to prevent further execution
  }

  // Handle sign-out
  if (isSigningOut) {
    LaunchedEffect(Unit) {
      userViewModel.signOutUser()
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
              when {
                isSigningOut -> {
                  CircularProgressIndicator(modifier = Modifier.testTag("signingOutIndicator"))
                }
                isLoading -> {
                  CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
                }
                user != null -> {
                  ProfileContent(
                      userData = user!!,
                      onSignOut = { isSigningOut = true },
                      onEdit = { navigationActions.navigateTo(Route.EDIT_PROFILE) })
                }
                else -> {
                  Text("No user data available", modifier = Modifier.testTag("noUserData"))
                }
              }
            }
      })
}

@Composable
fun ProfileContent(userData: User, onSignOut: () -> Unit, onEdit: () -> Unit) {
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

        // Edit and Sign out buttons
        Row {
          Button(onClick = onEdit, modifier = Modifier.testTag("editButton")) {
            Text(text = "Edit")
          }
          Spacer(modifier = Modifier.width(16.dp))
          Button(onClick = onSignOut, modifier = Modifier.testTag("signOutButton")) {
            Text(text = "Sign Out")
          }
        }
      }
}
