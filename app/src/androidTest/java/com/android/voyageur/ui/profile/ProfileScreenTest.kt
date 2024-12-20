package com.android.voyageur.ui.profile

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.notifications.TripInvite
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock

class ProfileScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var tripInviteRepository: TripInviteRepository
  private lateinit var tripsRepository: TripRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock dependencies
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)
    tripsRepository = mock(TripRepository::class.java)
    tripInviteRepository = mock(TripInviteRepository::class.java)

    // Mock FirebaseAuth to return our mocked firebaseUser
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)

    // Mock methods of FirebaseUser to return non-null values
    `when`(firebaseUser.uid).thenReturn("123")
    `when`(firebaseUser.displayName).thenReturn("Test User")
    `when`(firebaseUser.email).thenReturn("test@example.com")
    `when`(firebaseUser.photoUrl).thenReturn(null) // Or a valid URI if needed

    // Mock userRepository.getUserById to call onSuccess with a User
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user = User(userId, "Test User", "test@example.com", interests = emptyList())
          onSuccess(user)
          null
        }
        .`when`(userRepository)
        .getUserById(anyString(), anyOrNull(), anyOrNull())

    // Mock userRepository.listenToUser to call onSuccess with a User
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user = User(userId, "Test User", "test@example.com", interests = emptyList())
          onSuccess(user)
          null
        }
        .`when`(userRepository)
        .listenToUser(anyString(), anyOrNull(), anyOrNull())

    // Mock userRepository.fetchUsersByIds to return an empty list
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .`when`(userRepository)
        .fetchUsersByIds(anyOrNull(), anyOrNull(), anyOrNull())

    // Mock friendRequestRepository.getFriendRequests to return an empty list
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(emptyList()) // Return empty list or desired data
          null
        }
        .`when`(friendRequestRepository)
        .getFriendRequests(anyString(), anyOrNull(), anyOrNull())

    // Mock friendRequestRepository.getNotificationCount to return zero
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(Long) -> Unit>(1)
          onSuccess(0L) // Return zero notifications
          null
        }
        .`when`(friendRequestRepository)
        .getNotificationCount(anyString(), anyOrNull(), anyOrNull())

    // Create the UserViewModel with the mocked userRepository and firebaseAuth
    userViewModel =
        UserViewModel(
            userRepository,
            firebaseAuth,
            friendRequestRepository,
            addAuthStateListener = false // Prevent adding the AuthStateListener during tests
            )
    userViewModel.shouldFetch = false

    tripsViewModel = TripsViewModel(tripsRepository, tripInviteRepository, false, firebaseAuth)

    // Mocking initial navigation state
    `when`(navigationActions.currentRoute()).thenReturn(Route.PROFILE)

    // Set the content for Compose rule
    composeTestRule.setContent {
      ProfileScreen(
          userViewModel = userViewModel,
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions)
      FriendReqMenu(
          friendRequests = friendRequests.value,
          notificationUsers = notificationUsers.value,
          userViewModel = userViewModel)
    }
  }

  val friendRequests = mutableStateOf(emptyList<FriendRequest>())
  val notificationUsers = mutableStateOf(emptyList<User>())

  @Test
  fun displayUserProfileWhenUserIsLoggedIn() {
    // Arrange: Mock a logged-in user
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()

    // Assert: Check that the user profile information is displayed
    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userEmail").assertIsDisplayed()
  }

  @Test
  fun logoutButtonIsDisplayedAndCallsLogoutAction() {
    // Arrange: Mock a logged-in user
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the logout button is displayed
    composeTestRule.onNodeWithTag("signOutButton").assertIsDisplayed()

    // Act: Perform click on logout button
    composeTestRule.onNodeWithTag("signOutButton").performClick()
  }

  @Test
  fun displayDefaultProfilePictureWhenNoProfilePicture() {
    // Arrange: Mock a user without a profile picture
    val user =
        User("123", "Jane Doe", "jane@example.com", profilePicture = "", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the default profile picture is displayed
    composeTestRule.onNodeWithTag("defaultProfilePicture").assertIsDisplayed()
  }

  @Test
  fun displayProfilePictureWhenUserHasProfilePicture() {
    val user =
        User(
            "123",
            "Jane Doe",
            "jane@example.com",
            profilePicture = "http://example.com/profile.jpg")
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("userProfilePicture").assertIsDisplayed()
  }

  @Test
  fun handleEmptyNameAndEmailGracefully() {
    // Arrange: Mock a user with empty name and email
    val user = User("123", "", "", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the placeholders for name and email are displayed
    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userEmail").assertIsDisplayed()
  }

  @Test
  fun signOutTriggersSignOutActionAndNavigatesToAuth() {
    // Arrange: Mock the user to simulate a logged-in state
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false
    // Perform the sign-out action
    composeTestRule.onNodeWithTag("signOutButton").performClick()

    // Simulate the sign-out and check if the navigation happens
    composeTestRule.runOnUiThread {
      // Mock the effect of signing out, including navigating to the Auth screen
      userViewModel._user.value = null
      userViewModel._isLoading.value = false
      verify(navigationActions).navigateTo(Route.AUTH)
    }

    // Assert: Verify that the navigation was triggered
    verify(navigationActions).navigateTo(Route.AUTH)
  }

  // New test to verify interests are displayed when the user has interests
  @Test
  fun displayInterestsWhenUserHasInterests() {
    // Arrange: Mock a user with interests
    val interests = listOf("Hiking", "Travel", "Photography")
    val user = User("123", "Jane Doe", "jane@example.com", interests = interests)
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the interests are displayed
    composeTestRule.onNodeWithTag("interestsFlowRow").assertIsDisplayed()
    interests.forEach { interest -> composeTestRule.onNodeWithText(interest).assertIsDisplayed() }
  }

  // New test to verify the "No interests added yet" message is displayed when the user has no
  // interests
  @Test
  fun displayNoInterestsMessageWhenUserHasNoInterests() {
    // Arrange: Mock a user with no interests
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the "No interests added yet" message is displayed
    composeTestRule.onNodeWithTag("noInterests").assertIsDisplayed()
  }

  @Test
  fun displaysNoRequestsTextWhenFriendRequestsAreEmpty() {
    // Arrange: Set empty friendRequests
    friendRequests.value = emptyList()

    // Assert: Check for empty state elements
    composeTestRule.onNodeWithTag("noRequestsBox").assertIsDisplayed()
    composeTestRule.onNodeWithText("You're all caught up! No pending requests.").assertIsDisplayed()
  }

  @Test
  fun displaysFriendRequestWhenListIsNotEmpty() {
    // Arrange: Set friendRequests with some data
    val mockFriendRequests = listOf(FriendRequest(from = "user1", to = "user2"))
    friendRequests.value = mockFriendRequests
    notificationUsers.value =
        listOf(User(id = "user1", name = "User One", profilePicture = "http://example.com/pic.jpg"))
    composeTestRule.waitForIdle()

    // Assert: Check that the LazyColumn is displayed
    composeTestRule.onNodeWithTag("friendRequestLazyColumn").assertIsDisplayed()

    // Assert: Check that the specific friend request item has count one
    composeTestRule.onAllNodesWithTag("friendRequest", useUnmergedTree = true).assertCountEquals(1)

    // Assert: Check the profile picture and name are displayed
    composeTestRule.onNodeWithTag("profilePicture", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("User One", useUnmergedTree = true).assertIsDisplayed()

    // Assert: Check the accept and deny buttons are displayed
    composeTestRule.onNodeWithTag("acceptButton", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("denyButton", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun notificationBadgeIsNotDisplayed() {
    userViewModel._notificationCount.value = 0
    composeTestRule.waitForIdle()
    assert(userViewModel._notificationCount.value == 0L)
    // Verify the notification badge is not displayed on the profile tab
    composeTestRule.onNodeWithTag("notificationBadge").assertIsNotDisplayed()
  }

  @Test
  fun notificationBadgeIsDisplayedWhenNotificationsExist() {
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    // Arrange: Set notification count to a non-zero value
    userViewModel._notificationCount.value = 5
    composeTestRule.waitForIdle()
    assert(userViewModel._notificationCount.value == 5L)
    // Assert: Verify the notification badge is displayed on the profile tab
    composeTestRule.onNodeWithTag("notificationBadge", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("5", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun declineTripInviteCallsRepository() {
    // Set up logged in user first
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Create test invite
    val invite = TripInvite(id = "invite1", from = "user1", to = "user2", tripId = "trip1")
    tripsViewModel.set_tripInvites(listOf(invite))

    // Let the UI update
    composeTestRule.waitForIdle()

    // Verify trip invite card is displayed
    composeTestRule.onNodeWithTag("tripInviteCard").assertIsDisplayed()

    // Verify deny button is displayed and click it
    composeTestRule.onNodeWithTag("denyButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("denyButton").performClick()

    // Verify repository method was called
    verify(tripInviteRepository).deleteTripInvite(eq("invite1"), anyOrNull(), anyOrNull())
  }

  @Test
  fun tripInvitesShowCorrectCount() {
    // Set up logged in user first
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Create multiple invites
    val invites =
        listOf(
            TripInvite(id = "1", from = "user1", to = "user2", tripId = "trip1"),
            TripInvite(id = "2", from = "user3", to = "user2", tripId = "trip2"))
    tripsViewModel.set_tripInvites(invites)

    // Let the UI update
    composeTestRule.waitForIdle()

    // Verify count is displayed correctly
    composeTestRule.onNodeWithText("Pending Trip Invites (2)").assertIsDisplayed()
  }

  @Test
  fun acceptingTripInviteUpdatesInvitesList() {
    // Set up logged in user first
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Create test invite
    val invite = TripInvite(id = "invite1", from = "user1", to = "user2", tripId = "trip1")
    tripsViewModel.set_tripInvites(listOf(invite))

    // Let the UI update
    composeTestRule.waitForIdle()

    // Verify trip invite card is displayed
    composeTestRule.onNodeWithTag("tripInviteCard").assertIsDisplayed()

    // Verify accept button is displayed and click it
    composeTestRule.onNodeWithTag("acceptButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("acceptButton").performClick()
  }

  @Test
  fun tripInviteMenuShowsEmptyState() {
    // Set up logged in user first
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Set empty invites
    tripsViewModel.set_tripInvites(emptyList())

    // Let the UI update
    composeTestRule.waitForIdle()

    // Verify empty state is shown
    composeTestRule.onNodeWithTag("tripInviteCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noInvitesBox").assertIsDisplayed()
  }

  @Test
  fun displaysTripInviteFromInfo() {
    // Set up logged in user
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Mock trip and sender details
    val trip = Trip(id = "trip1", name = "Adventure Trip")
    val sender = User(id = "sender123", name = "Alice")

    // Mock tripInviteRepository and userRepository behavior
    doAnswer { invocation ->
          val tripId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(Trip?) -> Unit>(1)
          if (tripId == "trip1") {
            onSuccess(trip)
          } else {
            onSuccess(null)
          }
          null
        }
        .`when`(tripsRepository)
        .getTripById(eq("trip1"), anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User?) -> Unit>(1)
          if (userId == "sender123") {
            onSuccess(sender)
          } else {
            onSuccess(null)
          }
          null
        }
        .`when`(userRepository)
        .getUserById(eq("sender123"), anyOrNull(), anyOrNull())

    // Create test invite
    val invite = TripInvite(id = "invite1", from = "sender123", to = "user2", tripId = "trip1")
    tripsViewModel.set_tripInvites(listOf(invite))

    // Let the UI update
    composeTestRule.waitForIdle()

    // Verify the sender's name and trip name are displayed
    composeTestRule.onNodeWithText("Adventure Trip").assertIsDisplayed()
    composeTestRule.onNodeWithText("Invited by: Alice").assertIsDisplayed()
  }
}
