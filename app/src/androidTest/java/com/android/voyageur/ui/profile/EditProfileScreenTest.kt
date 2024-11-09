package com.android.voyageur.ui.profile

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.android.voyageur.MainActivity
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
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

class EditProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser

  @Before
  fun setUp() {
    // Mock dependencies
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)

    // Mock FirebaseAuth to return our mocked firebaseUser
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)

    // Mock methods of FirebaseUser to return non-null values
    `when`(firebaseUser.uid).thenReturn("123")
    `when`(firebaseUser.displayName).thenReturn("Test User")
    `when`(firebaseUser.email).thenReturn("test@example.com")
    `when`(firebaseUser.photoUrl).thenReturn(null)

    // Mock userRepository.getUserById to call onSuccess with a User
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user =
              User(
                  id = userId,
                  name = "Test User",
                  email = "test@example.com",
                  interests = listOf("Travel", "Photography"))
          onSuccess(user)
          null
        }
        .`when`(userRepository)
        .getUserById(anyString(), anyOrNull(), anyOrNull())

    // Create the UserViewModel with the mocked userRepository and firebaseAuth
    userViewModel = UserViewModel(userRepository, firebaseAuth)

    // Mocking initial navigation state
    `when`(navigationActions.currentRoute()).thenReturn(Route.EDIT_PROFILE)

    // Set the content for Compose rule
    composeTestRule.activity.setContent {
      EditProfileScreen(userViewModel = userViewModel, navigationActions = navigationActions)
    }
  }

  @Test
  fun displayLoadingIndicatorWhenIsLoading() {
    // Arrange: Set isLoading to true
    userViewModel._isLoading.value = true
    userViewModel._user.value = null

    // Assert: Check that the loading indicator is displayed
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
  }

  @Test
  fun displayUserDataWhenUserIsAvailable() {
    // Arrange: Mock a logged-in user
    val user = User("123", "Jane Doe", "jane@example.com", "http://example.com/profile.jpg")
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the user data fields are displayed with correct values
    composeTestRule.onNodeWithTag("nameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()

    // Verify that the name and email fields contain the correct text
    composeTestRule.onNodeWithTag("nameField").assert(hasText("Jane Doe"))
    composeTestRule.onNodeWithTag("emailField").assert(hasText("jane@example.com"))
  }

  @Test
  fun editProfilePictureLaunchesImagePicker() {
    // Since we cannot test ActivityResultLauncher directly, we can test that the button is
    // displayed
    // Arrange: Mock a logged-in user
    val user = User("123", "Jane Doe", "jane@example.com")
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the edit image button is displayed
    composeTestRule.onNodeWithTag("editImageButton").assertIsDisplayed()
  }

  @Test
  fun handleEmptyUserDataGracefully() {
    // Arrange: Mock a user with empty data
    val user = User("123", "", "", "")
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the fields are displayed and can be edited
    composeTestRule.onNodeWithTag("nameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
  }

  @Test
  fun displayDefaultProfilePictureWhenNoProfilePicture() {
    // Arrange: Mock a user without a profile picture
    val user = User("123", "Jane Doe", "jane@example.com", "")
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the default profile picture is displayed
    composeTestRule.onNodeWithTag("defaultProfilePicture").assertIsDisplayed()
  }

  @Test
  fun displayProfilePictureWhenUserHasProfilePicture() {
    // Arrange: Mock a user with a profile picture
    val user = User("123", "Jane Doe", "jane@example.com", "http://example.com/profile.jpg")
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Check that the profile picture is displayed
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
  }

  @Test
  fun emailFieldIsReadOnly() {
    // Arrange: Mock a logged-in user
    val user = User("123", "Jane Doe", "jane@example.com")
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    // Assert: Email field is displayed
    composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()

    // Check if the email field is read-only by verifying its properties
    composeTestRule.onNodeWithTag("emailField").assert(hasText("jane@example.com"))
    composeTestRule.onNodeWithTag("emailField").assertIsNotEnabled()
  }

  @Test
  fun noUserDataDisplaysMessage() {
    // Arrange: Set user to null and isLoading to false
    userViewModel._user.value = null
    userViewModel._isLoading.value = false

    // Assert: Check that the "No user data available" text is displayed
    composeTestRule.onNodeWithTag("noUserData").assertIsDisplayed()
    composeTestRule.onNodeWithText("No user data available").assertIsDisplayed()
  }
  // New test: Add Interest
  @Test
  fun addInterestSuccessfully() {
    // Arrange
    userViewModel._user.value =
        User("123", "Jane Doe", "jane@example.com", interests = mutableListOf())
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Act: Enter a new interest and click the Add button
    val newInterest = "Hiking"
    composeTestRule.onNodeWithTag("newInterestField").performTextInput(newInterest)
    composeTestRule.onNodeWithTag("addInterestButton").performClick()

    // Assert: Check that the new interest chip is displayed
    composeTestRule.onNodeWithText(newInterest).assertIsDisplayed()
  }

  // New test: Remove Interest with Confirmation Dialog
  @Test
  fun removeInterestWithConfirmation() {
    // Arrange
    val interestToRemove = "Photography"
    userViewModel._user.value =
        User(
            "123",
            "Jane Doe",
            "jane@example.com",
            interests = mutableListOf("Travel", interestToRemove))
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Act: Click on the remove icon of the interest chip
    composeTestRule.onNodeWithTag("removeInterestButton_$interestToRemove").performClick()

    // Assert: Confirmation dialog is displayed
    composeTestRule.onNodeWithText("Remove Interest").assertIsDisplayed()
    composeTestRule
        .onNodeWithText(
            "Are you sure you want to remove \"$interestToRemove\" from your interests?")
        .assertIsDisplayed()

    // Act: Confirm the deletion
    composeTestRule.onNodeWithText("Remove").performClick()

    // Assert: The interest chip is no longer displayed
    composeTestRule.onNodeWithText(interestToRemove).assertDoesNotExist()
  }
  // New test: Cancel Removing Interest
  @Test
  fun cancelRemoveInterest() {
    // Arrange
    val interestToRemove = "Travel"
    userViewModel._user.value =
        User(
            "123",
            "Jane Doe",
            "jane@example.com",
            interests = mutableListOf(interestToRemove, "Photography"))
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Act: Click on the remove icon of the interest chip
    composeTestRule.onNodeWithTag("removeInterestButton_$interestToRemove").performClick()

    // Assert: Confirmation dialog is displayed
    composeTestRule.onNodeWithText("Remove Interest").assertIsDisplayed()

    // Act: Cancel the deletion
    composeTestRule.onNodeWithText("Cancel").performClick()

    // Assert: The interest chip is still displayed
    composeTestRule.onNodeWithText(interestToRemove).assertIsDisplayed()
  }
  // New test: Do not add whitespace-only interest
  @Test
  fun doNotAddWhitespaceOnlyInterest() {
    // Arrange
    userViewModel._user.value =
        User("123", "Jane Doe", "jane@example.com", interests = mutableListOf())
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Act: Enter an interest that is only whitespaces and click the Add button
    val whitespaceInterest = "   "
    composeTestRule.onNodeWithTag("newInterestField").performTextInput(whitespaceInterest)
    composeTestRule.onNodeWithTag("addInterestButton").performClick()

    // Assert: Check that "No interests added yet" text is still displayed
    composeTestRule.onNodeWithTag("noInterests").assertIsDisplayed()
    // Also check that the interest is not added
    composeTestRule.onAllNodes(hasText(whitespaceInterest.trim())).assertCountEquals(0)
  }
  // Test: Interests are displayed
  @Test
  fun interestsAreDisplayed() {
    // Arrange
    val interests = listOf("Travel", "Photography")
    userViewModel._user.value =
        User("123", "Jane Doe", "jane@example.com", interests = interests.toMutableList())
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Assert: Check that the interest chips are displayed
    for (interest in interests) {
      composeTestRule.onNodeWithText(interest).assertIsDisplayed()
    }
  }
  // Test: No interests text not displayed when interests exist
  @Test
  fun noInterestsTextNotDisplayedWhenInterestsExist() {
    // Arrange
    userViewModel._user.value =
        User("123", "Jane Doe", "jane@example.com", interests = mutableListOf("Travel"))
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Assert: Check that "No interests added yet" text is not displayed
    composeTestRule.onNodeWithTag("noInterests").assertDoesNotExist()
  }
  // Test: Do not add empty interest
  @Test
  fun doNotAddEmptyInterest() {
    // Arrange
    userViewModel._user.value =
        User("123", "Jane Doe", "jane@example.com", interests = mutableListOf())
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Act: Click the Add button without entering any text
    composeTestRule.onNodeWithTag("addInterestButton").performClick()

    // Assert: Check that "No interests added yet" text is still displayed
    composeTestRule.onNodeWithTag("noInterests").assertIsDisplayed()
  }
  // Test: Add interest using IME action
  @Test
  fun addInterestWithImeAction() {
    // Arrange
    userViewModel._user.value =
        User("123", "Jane Doe", "jane@example.com", interests = mutableListOf())
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Act: Enter a new interest and press the "Done" IME action
    val newInterest = "Cooking"
    composeTestRule.onNodeWithTag("newInterestField").performTextInput(newInterest)
    composeTestRule.onNodeWithTag("newInterestField").performImeAction()

    // Assert: Check that the new interest chip is displayed
    composeTestRule.onNodeWithText(newInterest).assertIsDisplayed()
  }
  // Test: Do not add empty interest with IME action
  @Test
  fun doNotAddEmptyInterestWithImeAction() {
    // Arrange
    userViewModel._user.value =
        User("123", "Jane Doe", "jane@example.com", interests = mutableListOf())
    userViewModel._isLoading.value = false

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Act: Press the "Done" IME action without entering any text
    composeTestRule.onNodeWithTag("newInterestField").performImeAction()

    // Assert: Check that "No interests added yet" text is still displayed
    composeTestRule.onNodeWithTag("noInterests").assertIsDisplayed()
  }
}
