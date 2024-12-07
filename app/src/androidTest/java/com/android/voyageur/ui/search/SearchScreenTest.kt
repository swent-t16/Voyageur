package com.android.voyageur.ui.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.NavigationState
import com.android.voyageur.ui.navigation.Route
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.test.runTest
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
  private lateinit var tripsRepository: TripRepository
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var navigationState: NavigationState
  private lateinit var friendRequestRepository: FriendRequestRepository
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    placesRepository = mock(PlacesRepository::class.java)
    tripsRepository = mock(TripRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    placesViewModel = PlacesViewModel(placesRepository)
    tripsViewModel = TripsViewModel(tripsRepository)
    navigationState = NavigationState()
    `when`(navigationActions.currentRoute()).thenReturn(Route.SEARCH)
    `when`(navigationActions.getNavigationState()).thenReturn(navigationState)
    composeTestRule.setContent {
      SearchScreen(userViewModel, placesViewModel, tripsViewModel, navigationActions, false)
    }
  }

  @Test
  fun testInitialState() {
    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchScreenContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tabRow").assertIsDisplayed()
  }

  @Test
  fun testTabFilterFunctionality() {
    composeTestRule.onNodeWithTag("tabRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton_USERS").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton_PLACES").assertIsDisplayed()
    composeTestRule.onNodeWithTag("discoverTab").assertIsDisplayed()
  }

  @Test
  fun testSearchUsersFunctionality() {
    val searchQuery = "test"
    val mockUserList = listOf(User(id = "1", name = "Test User", email = "test@example.com"))
    `when`(userRepository.searchUsers(eq(searchQuery), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(mockUserList)
    }
    composeTestRule.onNodeWithTag("searchTextField").performTextClearance()
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(searchQuery)
    composeTestRule.onNodeWithTag("searchTextField").performClick()
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

  @Test
  fun testSearchPlacesFunctionality() {
    val searchQuery = "test"
    val mockPlaceList =
        listOf(Place.builder().setName("Test Place 1").setId("1").setRating(4.0).build())
    `when`(placesRepository.searchPlaces(eq(searchQuery), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<Place>) -> Unit
      onSuccess(mockPlaceList)
    }
    composeTestRule.onNodeWithTag("filterButton_PLACES").performClick()
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(searchQuery)
    composeTestRule.onNodeWithTag("searchTextField").performClick()
  }

  @Test
  fun testNoResultsFoundPlaces() {
    val searchQuery = "test"
    `when`(placesRepository.searchPlaces(eq(searchQuery), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<Place>) -> Unit
      onSuccess(emptyList())
    }
    composeTestRule.onNodeWithTag("filterButton_PLACES").performClick()
    composeTestRule.onNodeWithTag("searchTextField").performTextInput(searchQuery)
  }

  @Test
  fun testToggleToMapViewButton() {
    composeTestRule.onNodeWithTag("filterButton_PLACES").performClick()
    composeTestRule.onNodeWithTag("toggleMapViewButton").assertIsDisplayed().performClick()

    composeTestRule.onNodeWithTag("toggleMapViewButton").assertIsDisplayed().performClick()
  }

  @Test
  fun testDiscoverTab() = runTest {
    composeTestRule.awaitIdle()
    `when`(tripsRepository.getFeed(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<Trip>) -> Unit
      onSuccess(listOf(Trip(id = "1"))) // Provide a valid Trip for testing
    }
    composeTestRule.onNodeWithTag("discoverTab").performClick()
    composeTestRule.awaitIdle()
    composeTestRule.onNodeWithTag("tripCard_1").assertIsDisplayed()
  }

  @Test
  fun testDiscoverNoFeed() {
    `when`(tripsRepository.getFeed(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<Trip>) -> Unit
      onSuccess(emptyList())
    }

    composeTestRule.onNodeWithTag("discoverTab").performClick()
    composeTestRule.onNodeWithTag("noTripsFound").assertIsDisplayed()
  }
}
