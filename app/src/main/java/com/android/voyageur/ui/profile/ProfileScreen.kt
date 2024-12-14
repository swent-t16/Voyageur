package com.android.voyageur.ui.profile

import UserProfileContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.TripInvite
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route

/**
 * Composable function that represents the profile screen. Displays user information, handles
 * navigation, and manages sign-out logic.
 *
 * @param userViewModel The [UserViewModel] instance used to observe user state and actions.
 * @param navigationActions The [NavigationActions] instance for navigating between screens.
 */
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions
) {
  val user by userViewModel.user.collectAsState()
  val isLoading by userViewModel.isLoading.collectAsState()

  LaunchedEffect(Unit) { tripsViewModel.fetchTripInvites() }

  var isSigningOut by remember { mutableStateOf(false) }

  if (user == null && !isLoading) {
    LaunchedEffect(Unit) { navigationActions.navigateTo(Route.AUTH) }
    return
  }

  if (isSigningOut) {
    LaunchedEffect(Unit) {
      userViewModel.signOutUser()
      navigationActions.navigateTo(Route.AUTH)
    }
  }

  Scaffold(
      modifier = Modifier.testTag("profileScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel,
            tripsViewModel)
      }) { padding ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .testTag("profileScreenContent"),
            contentAlignment = Alignment.Center) {
              when {
                isSigningOut ->
                    CircularProgressIndicator(modifier = Modifier.testTag("signingOutIndicator"))
                isLoading ->
                    CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
                user != null ->
                    ProfileContent(
                        userData = user!!,
                        signedInUserId = user!!.id,
                        onSignOut = { isSigningOut = true },
                        userViewModel = userViewModel,
                        tripsViewModel = tripsViewModel,
                        onEdit = { navigationActions.navigateTo(Route.EDIT_PROFILE) })
                else ->
                    Text(
                        stringResource(R.string.no_user_data),
                        modifier = Modifier.testTag("noUserData"))
              }
            }
      }
}

/**
 * Composable function that displays detailed user profile information and interaction options.
 * Includes profile editing and friend request management.
 *
 * @param userData The [User] object representing the signed-in user's data.
 * @param signedInUserId The ID of the currently signed-in user.
 * @param onSignOut A lambda function triggered when the user chooses to sign out.
 * @param userViewModel The [UserViewModel] instance for managing user-related actions.
 * @param onEdit A lambda function triggered to navigate to the edit profile screen.
 */
@Composable
fun ProfileContent(
    userData: User,
    signedInUserId: String,
    onSignOut: () -> Unit,
    userViewModel: UserViewModel,
    tripsViewModel: TripsViewModel,
    onEdit: () -> Unit
) {
  val friendRequests by userViewModel.friendRequests.collectAsState()
  val notificationUsers by userViewModel.notificationUsers.collectAsState()
  val tripInvites by tripsViewModel.tripInvites.collectAsState()

  Column(
      modifier = Modifier.fillMaxSize().padding(top = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        UserProfileContent(
            userData = userData,
            signedInUserId = signedInUserId,
            showEditAndSignOutButtons = true,
            onSignOut = onSignOut,
            onEdit = onEdit)
        Spacer(modifier = Modifier.height(20.dp))

        // Friend Requests Menu
        FriendReqMenu(friendRequests, notificationUsers, userViewModel)

        Spacer(modifier = Modifier.height(20.dp))

        // Trip Invite Menu
        TripInviteMenu(tripInvites = tripInvites, tripsViewModel = tripsViewModel)
      }
}

@Composable
fun TripInviteItem(tripInvite: TripInvite, tripsViewModel: TripsViewModel) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("tripInvite"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "From: ${tripInvite.from}", modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(
              modifier = Modifier.testTag("acceptButton"),
              onClick = { tripsViewModel.acceptTripInvite(tripInvite) }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.accept),
                    tint = Color.Green)
              }

          IconButton(
              modifier = Modifier.testTag("denyButton"),
              onClick = { tripsViewModel.declineTripInvite(tripInvite.id) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.reject),
                    tint = Color.Red)
              }
        }
      }
}

@Composable
fun TripInviteMenu(tripInvites: List<TripInvite>, tripsViewModel: TripsViewModel) {
  Card(
      shape = RoundedCornerShape(12.dp),
      modifier =
          Modifier.fillMaxWidth(0.80f)
              .padding(horizontal = 10.dp, vertical = 8.dp)
              .testTag("tripInviteCard")) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
              text = stringResource(R.string.pending_trip_invites, tripInvites.size),
              color = MaterialTheme.colorScheme.onSurface,
          )

          Card(
              shape = RoundedCornerShape(8.dp),
              modifier =
                  Modifier.fillMaxWidth()
                      .heightIn(max = 180.dp)
                      .padding(top = 8.dp)
                      .testTag("tripInviteBox")) {
                if (tripInvites.isEmpty()) {
                  Box(
                      modifier = Modifier.fillMaxSize().testTag("noInvitesBox"),
                      contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.no_pending_invites),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                } else {
                  LazyColumn(
                      modifier =
                          Modifier.fillMaxSize().padding(8.dp).testTag("tripInviteLazyColumn")) {
                        items(tripInvites) { invite ->
                          TripInviteItem(tripInvite = invite, tripsViewModel = tripsViewModel)
                        }
                      }
                }
              }
        }
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
      modifier =
          Modifier.fillMaxWidth(0.80f)
              .padding(horizontal = 10.dp, vertical = 8.dp)
              .testTag("friendRequestCard")) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
              text = stringResource(R.string.pending_friend_requests, friendRequests.size),
              color = MaterialTheme.colorScheme.onSurface,
          )

          // Scrollable List inside a fixed-height box
          Card(
              shape = RoundedCornerShape(8.dp),
              modifier =
                  Modifier.fillMaxWidth()
                      .heightIn(max = 180.dp) // Set the height to restrict the scrollable area
                      .padding(top = 8.dp)
                      .testTag("friendRequestBox")) {
                if (friendRequests.isEmpty()) {
                  // Display message if no friend requests
                  Box(
                      modifier = Modifier.fillMaxSize().testTag("noRequestsBox"),
                      contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.no_pending_requests),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                } else {
                  // Display Lazy Column with the friend requests
                  LazyColumn(
                      modifier =
                          Modifier.fillMaxSize().padding(8.dp).testTag("friendRequestLazyColumn")) {
                        items(friendRequests) { request ->
                          val fromUser = notificationUsers.find { it.id == request.from }
                          fromUser?.let {
                            FriendRequestItem(
                                friendRequest = request,
                                fromUser = fromUser,
                                userViewModel = userViewModel)
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
      modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("friendRequest"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween) {
        // Display the Profile Picture of the user from which you have a request
        Image(
            painter = rememberAsyncImagePainter(fromUser.profilePicture),
            contentDescription = stringResource(R.string.profile_picture),
            modifier =
                Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).testTag("profilePicture"))

        Text(
            text = fromUser.name,
            modifier = Modifier.padding(start = 8.dp).weight(1f) // Take remaining space
            )

        // Accept and Reject Icons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(
              modifier = Modifier.testTag("acceptButton"),
              onClick = {
                // Accept user's friend request and add to contacts
                userViewModel.acceptFriendRequest(friendRequest)
              }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.accept),
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
                    contentDescription = stringResource(R.string.reject),
                    tint = Color.Red)
              }
        }
      }
}
