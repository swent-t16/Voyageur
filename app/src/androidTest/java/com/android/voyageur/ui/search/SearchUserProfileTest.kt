import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.search.SearchUserProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull

class SearchUserProfileScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)

    // Mock FirebaseAuth to return our mocked firebaseUser
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)

    // Mock methods of FirebaseUser to return non-null values
    `when`(firebaseUser.uid).thenReturn("currentUserId")

    // Mock userRepository.listenToUser to immediately return a user
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user =
              User(
                  id = userId,
                  name = "Test User",
                  email = "test@example.com",
                  contacts = mutableListOf())
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

    // Mock friendRequestRepository methods if needed
    // For example, mock getFriendRequests or getNotificationCount if they are called

    // Create the UserViewModel with the mocked userRepository and firebaseAuth
    // Disable the AuthStateListener during tests to prevent unexpected calls
    userViewModel =
        UserViewModel(
            userRepository,
            firebaseAuth,
            friendRequestRepository = friendRequestRepository,
            addAuthStateListener = false)

    // Initialize 'user' with a valid User instance
    userViewModel._user.value =
        User(
            id = "currentUserId",
            name = "Current User",
            email = "current@example.com",
            contacts = mutableListOf())

    val selectedUser =
        User(
            id = "selectedUserId",
            name = "Selected User",
            email = "selected@example.com",
            interests = listOf("Reading", "Travel"))
    userViewModel._selectedUser.value = selectedUser

    composeTestRule.setContent {
      SearchUserProfileScreen(userViewModel = userViewModel, navigationActions = navigationActions)
    }
  }

  @Test
  fun testUserProfileContentDisplaysCorrectly() {
    composeTestRule.onNodeWithTag("userProfileContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userEmail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("interestsFlowRow").assertIsDisplayed()
    composeTestRule.onNodeWithText("Reading").assertIsDisplayed()
    composeTestRule.onNodeWithText("Travel").assertIsDisplayed()
  }

  @Test
  fun testLoadingIndicatorDisplaysWhenLoading() {
    userViewModel._isLoading.value = true
    userViewModel._selectedUser.value = null
    composeTestRule.onNodeWithTag("userProfileLoadingIndicator").assertIsDisplayed()
  }

  @Test
  fun testDefaultProfilePictureDisplaysWhenNoPicture() {
    val userWithoutPicture =
        User("selectedUserId", "Test User", "test@example.com", profilePicture = "")
    userViewModel._selectedUser.value = userWithoutPicture
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("defaultProfilePicture").assertIsDisplayed()
  }

  @Test
  fun testProfilePictureDisplaysWhenUserHasPicture() {
    val userWithPicture =
        User(
            "selectedUserId",
            "Test User",
            "test@example.com",
            profilePicture = "http://example.com/profile.jpg")
    userViewModel._selectedUser.value = userWithPicture
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("userProfilePicture").assertIsDisplayed()
  }

  @Test
  fun testDisplaysNoNameAndNoEmailWhenEmptyFields() {
    val userWithEmptyFields = User("selectedUserId", "", "", interests = emptyList())
    userViewModel._selectedUser.value = userWithEmptyFields
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
    composeTestRule.onNodeWithText("No name available").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userEmail").assertIsDisplayed()
    composeTestRule.onNodeWithText("No email available").assertIsDisplayed()
  }

  @Test
  fun testNoInterestsMessageDisplaysWhenUserHasNoInterests() {
    val userWithNoInterests =
        User("selectedUserId", "Test User", "test@example.com", interests = emptyList())
    userViewModel._selectedUser.value = userWithNoInterests
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("noInterests").assertIsDisplayed()
    composeTestRule.onNodeWithText("No interests added yet").assertIsDisplayed()
  }

  @Test
  fun testNavigateBackToSearchWhenNoUserDataAvailable() {
    userViewModel._selectedUser.value = null
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("userProfileScreen").assertDoesNotExist()
    verify(navigationActions).navigateTo(Route.SEARCH)
  }

  @Test
  fun testButtonDisplaysCancelWhenRequestPending() {
    // Set up a pending friend request
    val pendingRequest =
        FriendRequest(id = "requestId", from = "currentUserId", to = "selectedUserId")
    userViewModel._sentFriendRequests.value = listOf(pendingRequest)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("userProfileAddRemoveContactButton")
        .assertIsDisplayed()
        .assert(hasText("Cancel"))

    // Simulate button click
    composeTestRule.onNodeWithTag("userProfileAddRemoveContactButton").performClick()
  }
}
