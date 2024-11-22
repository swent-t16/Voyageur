package com.android.voyageur.ui.search

import UserProfileContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route

/**
 * Composable function for displaying a detailed profile of a selected user. Displays a loading
 * indicator if data is loading and navigates back to search if no user data is available.
 *
 * @param userViewModel The UserViewModel used for managing user data and interactions.
 * @param navigationActions The NavigationActions used for handling navigation actions within the
 *   app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserProfileScreen(userViewModel: UserViewModel, navigationActions: NavigationActions) {
  // Observing the selected user and loading state from the ViewModel
  val user by userViewModel.selectedUser.collectAsState()
  val isLoading by userViewModel.isLoading.collectAsState()

  // Navigate back to search if no user data is available and loading is complete
  if (user == null && !isLoading) {
    LaunchedEffect(Unit) { navigationActions.navigateTo(Route.SEARCH) }
    return
  }

  Scaffold(
      modifier = Modifier.testTag("userProfileScreen"),
      topBar = {
        TopAppBar(
            title = { Text("${user?.name ?: "User"}'s Profile") },
            navigationIcon = {
              IconButton(
                  onClick = {
                    navigationActions.goBack() // Navigate back
                  }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.testTag("userProfileBackButton"))
                  }
            })
      },
      content = { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize().padding(paddingValues).testTag("userProfileScreenContent"),
            contentAlignment = Alignment.Center) {
              when {
                isLoading -> {
                  CircularProgressIndicator(
                      modifier = Modifier.testTag("userProfileLoadingIndicator"))
                }
                user != null -> {
                  SearchUserProfileContent(userData = user!!, userViewModel = userViewModel)
                }
                else -> {
                  Text("No user data available", modifier = Modifier.testTag("userProfileNoData"))
                }
              }
            }
      })
}

/**
 * Composable function for displaying the details of a selected user. Displays profile picture,
 * name, email, interests, and an "Add Contact" button.
 *
 * @param userData The selected User object containing user details.
 * @param userViewModel The UserViewModel used for managing user data and interactions.
 */
@Composable
fun SearchUserProfileContent(userData: User, userViewModel: UserViewModel) {
  val currentUser by userViewModel.user.collectAsState()
  val sentFriendRequests by userViewModel.sentFriendRequests.collectAsState()

  // Fetch sent friend requests when the composable is first displayed
  LaunchedEffect(Unit) { userViewModel.getSentFriendRequests() }

  val isContactAdded = currentUser?.contacts?.contains(userData.id) ?: false
  val isRequestPending = sentFriendRequests.any { it.to == userData.id }
  val signedInUserId = currentUser?.id ?: ""

  UserProfileContent(
      userData = userData,
      signedInUserId = signedInUserId,
      isContactAdded = isContactAdded,
      isRequestPending = isRequestPending,
      onAddContact = { userViewModel.addContact(userData.id) },
      onRemoveContact = { userViewModel.removeContact(userData.id) })
}
