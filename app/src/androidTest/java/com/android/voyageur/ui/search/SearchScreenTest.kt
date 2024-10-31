package com.android.voyageur.ui.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
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
  private lateinit var placesViewModel: PlacesViewModel
  private lateinit var placesRepository: PlacesRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    placesRepository = mock(PlacesRepository::class.java)
    userViewModel = UserViewModel(userRepository)
    placesViewModel = PlacesViewModel(placesRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Route.SEARCH)
    composeTestRule.setContent { SearchScreen(userViewModel, placesViewModel, navigationActions) }
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
  }

  @Test
  fun testAddToContactsButton() {
    val userId = "1"
    val mockUser = User(id = userId, name = "Test User", email = "test@example.com")

    // Set up the repository to return the mock user in the search results
    `when`(userRepository.searchUsers(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(listOf(mockUser))
    }

    // Set the current user without contacts, so the button is enabled
    `when`(userRepository.getUserById(eq(userId), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (User) -> Unit
      onSuccess(User(id = "currentUser", contacts = emptyList()))
    }

    // Perform a search to display the user
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Test User")
    composeTestRule.onNodeWithTag("userItem_1").assertIsDisplayed()

    // Check the "Add to contacts" button and click it
    composeTestRule.onNodeWithText("Add to contacts").assertIsDisplayed().performClick()

    // Verify that the addContact method was called with the correct userId
  }
}
