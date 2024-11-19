import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.search.SearchUserProfileScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class SearchUserProfileScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var friendRequestRepository: FriendRequestRepository
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)

    // Initialize 'user' with a valid User instance
    userViewModel._user.value =
        User(id = "123", name = "Test User", email = "test@example.com", contacts = mutableListOf())
    val selectedUser =
        User("123", "Test User", "test@example.com", interests = listOf("Reading", "Travel"))
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
    val userWithoutPicture = User("123", "Test User", "test@example.com", profilePicture = "")
    userViewModel._selectedUser.value = userWithoutPicture
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("defaultProfilePicture").assertIsDisplayed()
  }

  @Test
  fun testProfilePictureDisplaysWhenUserHasPicture() {
    val userWithPicture =
        User(
            "123",
            "Test User",
            "test@example.com",
            profilePicture = "http://example.com/profile.jpg")
    userViewModel._selectedUser.value = userWithPicture
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("userProfilePicture").assertIsDisplayed()
  }

  @Test
  fun testDisplaysNoNameAndNoEmailWhenEmptyFields() {
    val userWithEmptyFields = User("123", "", "", interests = emptyList())
    userViewModel._selectedUser.value = userWithEmptyFields
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
    composeTestRule.onNodeWithText("No name available").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userEmail").assertIsDisplayed()
    composeTestRule.onNodeWithText("No email available").assertIsDisplayed()
  }

  @Test
  fun testNoInterestsMessageDisplaysWhenUserHasNoInterests() {
    val userWithNoInterests = User("123", "Test User", "test@example.com", interests = emptyList())
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
}
