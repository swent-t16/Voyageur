package com.android.voyageur.ui.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class SearchScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Route.SEARCH)
    composeTestRule.setContent { SearchScreen(userViewModel, navigationActions) }
  }

  @Test
  fun testInitialState() {
    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchScreenContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterIcon").assertIsDisplayed()
  }

  @Test
  fun testFilterIconButtonDisplaysFilterButtons() {
    composeTestRule.onNodeWithTag("filterIcon").performClick()
    composeTestRule.onNodeWithTag("filterButton_Users").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton_Locations").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton_All").assertIsDisplayed()
  }

  @Test
  fun testSearchFunctionality() {
    val searchQuery = "test"
    val mockUserList = listOf(User(id = "1", name = "Test User", email = "test@example.com"))
    `when`(userRepository.searchUsers(eq(searchQuery), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(mockUserList)
    }
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(searchQuery)
    composeTestRule.onNodeWithTag("userItem_1").assertIsDisplayed()
  }

  @Test
  fun testFilterFunctionality() {
    val searchQuery = "test"
    val mockUserList = listOf(User(id = "1", name = "Test User", email = "test@example.com"))
    `when`(userRepository.searchUsers(eq(searchQuery), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(mockUserList)
    }
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(searchQuery)

    composeTestRule.onNodeWithTag("filterIcon").performClick()
    composeTestRule.onNodeWithTag("filterRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton_Users").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton_Locations").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton_All").assertIsDisplayed()

    // For now we are only testing the filter functionality for users as the locations search is not
    // implemented yet
    // when filtering for locations, a message is displayed saying that the feature is not
    // implemented yet
    composeTestRule.onNodeWithTag("filterButton_Users").performClick()
    composeTestRule.onNodeWithTag("userItem_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("placesPending").assertDoesNotExist()

    composeTestRule.onNodeWithTag("filterButton_Locations").performClick()
    composeTestRule.onNodeWithTag("userItem_1").assertDoesNotExist()
    composeTestRule.onNodeWithTag("placesPending").assertIsDisplayed()

    composeTestRule.onNodeWithTag("filterButton_All").performClick()
    composeTestRule.onNodeWithTag("userItem_1").assertIsDisplayed()
    // when filtering for all, the message for locations is not displayed
    composeTestRule.onNodeWithTag("placesPending").assertDoesNotExist()
  }

  @Test
  fun testNoResultsFound() {
    val searchQuery = "test"
    `when`(userRepository.searchUsers(eq(searchQuery), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(emptyList())
    }
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(searchQuery)
    composeTestRule.onNodeWithTag("noResults").assertIsDisplayed()
  }
}
