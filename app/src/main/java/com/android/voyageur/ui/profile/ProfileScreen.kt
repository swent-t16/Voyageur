package com.android.voyageur.ui.profile

import UserProfileContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.notifications.FriendRequest
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
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel)
      }) { padding ->
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .testTag("profileScreenContent"),
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
                      signedInUserId = user!!.id,
                      onSignOut = { isSigningOut = true },
                      userViewModel = userViewModel,
                      onEdit = { navigationActions.navigateTo(Route.EDIT_PROFILE) })
                }
                else -> {
                  Text("No user data available", modifier = Modifier.testTag("noUserData"))
                }
              }
            }
      }
}

@Composable
fun ProfileContent(
    userData: User,
    signedInUserId: String,
    onSignOut: () -> Unit,
    userViewModel: UserViewModel,
    onEdit: () -> Unit
) {
  val friendRequests by userViewModel.friendRequests.collectAsState()
  val notificationUsers by userViewModel.notificationUsers.collectAsState()
  Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(top = 16.dp), // Space for profile content
      horizontalAlignment = Alignment.CenterHorizontally) {
        UserProfileContent(
            userData = userData,
            signedInUserId = signedInUserId,
            showEditAndSignOutButtons = true,
            onSignOut = onSignOut,
            onEdit = onEdit)
        Spacer(modifier = Modifier.height(20.dp))
        // Expandable Friend Requests Menu
        FriendReqMenu(friendRequests, notificationUsers, userViewModel)
      }
}

/**
 * A composable function that displays a menu for handling pending friend requests. The menu can be
 * toggled between a collapsed view and an expanded dialog view for reviewing and managing friend
 * requests.
 *
 * @param friendRequests A list of [FriendRequest] objects representing the pending friend requests.
 * @param notificationUsers A list of [User] objects of users who sent the friend requests.
 * @param userViewModel An instance of [UserViewModel] for managing user-related actions such as
 *   accepting or rejecting friend requests.
 */
@Composable
fun FriendReqMenu(
    friendRequests: List<FriendRequest>,
    notificationUsers: List<User>,
    userViewModel: UserViewModel,
) {
    // Parent Card containing the scrollable box
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth(0.80f)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("friendRequestCard")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${friendRequests.size} Pending Friend Requests",
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Scrollable List inside a fixed-height box
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)// Set the height to restrict the scrollable area
                    .padding(top = 8.dp)
                    .testTag("friendRequestBox")
            ) {
                if (friendRequests.isEmpty()) {
                    // Display message if no friend requests
                    Box(
                        modifier = Modifier.fillMaxSize().testTag("noRequestsBox"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You're all caught up! No pending requests.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Display Lazy Column with the friend requests
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .testTag("friendRequestLazyColumn")
                    ) {
                        items(friendRequests) { request ->
                            val fromUser = notificationUsers.find { it.id == request.from }
                            fromUser?.let {
                                FriendRequestItem(
                                    friendRequest = request,
                                    fromUser = fromUser,
                                    userViewModel = userViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
/**
 * A composable function that displays a single friend request item. The item shows the user's
 * profile picture, name, and buttons to accept or reject the friend request.
 *
 * @param friendRequest Represents a pending friend request.
 * @param fromUser The [User] object representing the sender of the friend request.
 * @param userViewModel The [UserViewModel] used to handle actions like accepting or rejecting the
 *   friend request.
 */
@Composable
fun FriendRequestItem(friendRequest: FriendRequest, fromUser: User, userViewModel: UserViewModel) {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
          .testTag("friendRequest"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween) {
        // Display the Profile Picture of the user from which you have a request
        Image(
            painter = rememberAsyncImagePainter(fromUser.profilePicture),
            contentDescription = "Profile Picture",
            modifier =
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("profilePicture"))

        Text(
            text = fromUser.name,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f) // Take remaining space
            )

        // Accept and Reject Icons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(
              modifier = Modifier.testTag("acceptButton"),
              onClick = {
                // Accept user's friend request and add to contacts
                userViewModel.addContact(fromUser.id, friendRequest.id)
              }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept",
                    tint = Color.Green)
              }

          IconButton(
              modifier = Modifier.testTag("denyButton"),
              onClick = {
                // Delete the user friend request
                userViewModel.deleteFriendRequest(friendRequest.id)
              }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reject",
                    tint = Color.Red)
              }
        }
      }
}
