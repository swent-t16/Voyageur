package com.android.voyageur.ui.profile

import UserProfileContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
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
  UserProfileContent(
      userData = userData, showEditAndSignOutButtons = true, onSignOut = onSignOut, onEdit = onEdit)
}
