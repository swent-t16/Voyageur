package com.android.voyageur.ui.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class SearchUserProfileScreenTest {

    private lateinit var navigationActions: NavigationActions
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        navigationActions = mock(NavigationActions::class.java)
        userRepository = mock(UserRepository::class.java)
        userViewModel = UserViewModel(userRepository)

        val selectedUser = User("123", "Test User", "test@example.com", interests = listOf("Reading", "Travel"))
        userViewModel._selectedUser.value = selectedUser

        composeTestRule.setContent {
            SearchUserProfileScreen(userViewModel = userViewModel, navigationActions = navigationActions)
        }
    }

    @Test
    fun testUserProfileScreenDisplaysCorrectly() {
        composeTestRule.onNodeWithTag("userProfileScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("userProfileScreenContent").assertIsDisplayed()
        composeTestRule.onNodeWithTag("userProfileName").assertIsDisplayed()
        composeTestRule.onNodeWithTag("userProfileEmail").assertIsDisplayed()
    }

    @Test
    fun testUserProfileDisplaysLoadingIndicatorWhenLoading() {
        userViewModel._isLoading.value = true
        userViewModel._selectedUser.value = null
        composeTestRule.onNodeWithTag("userProfileLoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun testUserProfileDisplaysCorrectDetails() {
        composeTestRule.onNodeWithTag("userProfileName").assertIsDisplayed()
        composeTestRule.onNodeWithTag("userProfileEmail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("userProfileInterestsFlow").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Travel").assertIsDisplayed()
    }

    @Test
    fun testDisplayDefaultProfilePictureWhenNoPicture() {
        val userWithoutPicture = User("123", "Test User", "test@example.com", profilePicture = "")
        userViewModel._selectedUser.value = userWithoutPicture
        userViewModel._isLoading.value = false

        composeTestRule.onNodeWithTag("userProfileDefaultPicture").assertIsDisplayed()
    }

    @Test
    fun testDisplayProfilePictureWhenUserHasPicture() {
        val userWithPicture = User("123", "Test User", "test@example.com", profilePicture = "http://example.com/profile.jpg")
        userViewModel._selectedUser.value = userWithPicture
        userViewModel._isLoading.value = false

        composeTestRule.onNodeWithTag("userProfilePicture").assertIsDisplayed()
    }

    @Test
    fun testHandleEmptyNameAndEmailGracefully() {
        val userWithEmptyFields = User("123", "", "", interests = emptyList())
        userViewModel._selectedUser.value = userWithEmptyFields
        userViewModel._isLoading.value = false

        composeTestRule.onNodeWithTag("userProfileName").assertIsDisplayed()
        composeTestRule.onNodeWithTag("userProfileEmail").assertIsDisplayed()
    }

    @Test
    fun testNoInterestsMessageDisplayedWhenUserHasNoInterests() {
        val userWithNoInterests = User("123", "Test User", "test@example.com", interests = emptyList())
        userViewModel._selectedUser.value = userWithNoInterests
        userViewModel._isLoading.value = false

        composeTestRule.onNodeWithTag("userProfileNoInterests").assertIsDisplayed()
    }
}
