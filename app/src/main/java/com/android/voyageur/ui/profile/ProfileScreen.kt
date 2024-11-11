package com.android.voyageur.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.android.voyageur.ui.profile.interests.InterestChip

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

@OptIn(ExperimentalLayoutApi::class)
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
            text = userData.name.takeIf { it.isNotEmpty() } ?: "No name available",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("userName"))
        Text(
            text = userData.email.takeIf { it.isNotEmpty() } ?: "No email available",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("userEmail"))

        Spacer(modifier = Modifier.height(16.dp))

        // Display interests
        Text(
            text = "Interests:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp))

        if (userData.interests.isNotEmpty()) {
          // Display interests using FlowRow for better layout
          FlowRow(
              modifier =
                  Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("interestsFlowRow"),
              horizontalArrangement = Arrangement.Center) {
                userData.interests.forEach { interest ->
                  InterestChip(interest = interest, modifier = Modifier.padding(4.dp))
                }
              }
        } else {
          // Display message when no interests are added
          Text(
              text = "No interests added yet",
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.testTag("noInterests"))
        }

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
