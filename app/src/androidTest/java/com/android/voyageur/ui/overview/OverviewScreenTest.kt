package com.android.voyageur.ui.overview

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.NavigationState
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class OverviewScreenTest {
  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripViewModel: TripsViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    friendRequestRepository =
        mock(com.android.voyageur.model.notifications.FriendRequestRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    tripViewModel = TripsViewModel(tripRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    val mockUsers =
        listOf(
            com.android.voyageur.model.user.User(
                id = "1",
                name = "John Doe",
                email = "john@example.com",
            ),
            com.android.voyageur.model.user.User(
                id = "2",
                name = "Jane Doe",
                email = "jane@example.com",
            ))

    `when`(userRepository.fetchUsersByIds(any(), any(), any())).then {
      val onSuccess = it.getArgument<(List<com.android.voyageur.model.user.User>) -> Unit>(1)
      onSuccess(mockUsers) // Simulate a successful callback
    }
    `when`(navigationActions.getNavigationState()).thenReturn(NavigationState())
    composeTestRule.setContent { OverviewScreen(tripViewModel, navigationActions, userViewModel) }
  }

  @Test
  fun displayTextWhenEmpty() {
    `when`(tripRepository.getTrips(eq(""), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf())
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("emptyTripPrompt").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun overviewDisplaysCard() {
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                creator = "Andreea",
                participants = listOf("Alex", "Mihai", "Ioana", "Andrei", "Maria", "Matei"),
                name = "Paris Trip"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardRow", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("additionalParticipantsText", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun noParticipantsDisplaysNoAvatars() {
    val mockTrips =
        listOf(
            Trip(id = "23", creator = "Andreea", participants = emptyList(), name = "Paris Trip"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantAvatar").assertDoesNotExist()
  }

  @Test
  fun clickingTripCardNavigatesToTripDetails() {
    val mockTrip =
        Trip(
            id = "1",
            creator = "Andreea",
            participants = listOf("Alex", "Mihai"),
            name = "Paris Trip")
    val mockTrips = listOf(mockTrip)

    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }

    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("cardItem").performClick()

    verify(navigationActions).navigateTo(screen = Screen.TOP_TABS)
  }

  @Test
  fun clickingTripCardUpdatesNavigationState() {
    val mockTrip =
        Trip(
            id = "1",
            creator = "Andreea",
            participants = listOf("Alex", "Mihai"),
            name = "Paris Trip")
    val mockTrips = listOf(mockTrip)

    // Simulate getting the mock trip from the repository
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }

    tripViewModel.getTrips()

    // Simulate clicking the trip card
    composeTestRule.onNodeWithTag("cardItem").performClick()

    // Verify the trip is selected
    assert(tripViewModel.selectedTrip.value == mockTrip)

    // Verify the navigation state is updated
    val navigationState = navigationActions.getNavigationState()
    assert(navigationState.currentTabIndexForTrip == 0)
    assert(navigationState.isDailyViewSelected)
  }

  @Test
  fun createTripButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTripButton").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Floating action button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTripButton").performClick()
    verify(navigationActions).navigateTo(screen = Screen.ADD_TRIP)
  }

  @Test
  fun helperStringReturnsCorrectStrings() {
    val result0 = generateParticipantString(0)
    val result1 = generateParticipantString(1)
    val result2 = generateParticipantString(2)
    assert(result0 == "No participants.")
    assert(result1 == "1 Participant:")
    assert(result2 == "2 Participants:")
  }

  @Test
  fun tripWithImageDisplaysImage() {
    val mockTrip =
        Trip(
            id = "1",
            creator = "Andreea",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "https://example.com/image.jpg",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("tripImage", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("defaultTripImage").assertDoesNotExist()
  }

  @Test
  fun tripWithoutImageDisplaysDefaultImage() {
    val mockTrip =
        Trip(
            id = "1",
            creator = "Andreea",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("defaultTripImage", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("tripImage").assertDoesNotExist()
  }

  @Test
  fun deleteTripMethodIsCalled() {
    val mockTrip =
        Trip(
            id = "1",
            creator = "Andreea",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("expandIcon_${mockTrip.name}").performClick()
    composeTestRule.onNodeWithText("Delete").performClick()
    composeTestRule.onNodeWithText("Remove").performClick()

    verify(tripRepository).deleteTripById(eq(mockTrip.id), any(), any())
  }

  @Test
  fun searchField_filtersTrips() {
    val mockTrip =
        Trip(
            id = "1",
            creator = "Andreea",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "https://example.com/image.jpg",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    // Enter search query
    composeTestRule.onNodeWithTag("searchField").performTextInput("1")

    // Check if only matching trip is shown
    composeTestRule.onNodeWithText("1").assertExists()
  }

  @Test
  fun noResultsFound_isDisplayed_whenSearchHasNoMatches() {
    // Given - Set up trips with specific names
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                name = "Paris Trip",
                startDate = Timestamp.now(),
                endDate = Timestamp.now()),
            Trip(
                id = "2",
                name = "London Adventure",
                startDate = Timestamp.now(),
                endDate = Timestamp.now()))

    // When - Set up trips in repository
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    // Perform search with no matches
    composeTestRule.onNodeWithTag("searchField").performTextInput("Berlin")

    // Then - Verify NoResultsFound is displayed
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()

    // Verify trip cards are not shown
    composeTestRule.onAllNodesWithTag("cardItem").assertCountEquals(0)
  }

  @Test
  fun noResultsFound_isNotDisplayed_whenSearchHasMatches() {
    // Given
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                name = "Paris Trip",
                startDate = Timestamp.now(),
                endDate = Timestamp.now()))

    // When
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    // Perform search with matches
    composeTestRule.onNodeWithTag("searchField").performTextInput("Paris")

    // Then
    composeTestRule.onNodeWithTag("noResults").assertDoesNotExist()
    composeTestRule.onAllNodesWithTag("cardItem").assertCountEquals(1)
  }

  @Test
  fun noResultsFound_isNotDisplayed_whenSearchFieldIsEmpty() {
    // Given
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                name = "Paris Trip",
                startDate = Timestamp.now(),
                endDate = Timestamp.now()))

    // When
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    // Then - verify NoResultsFound is not displayed with empty search
    composeTestRule.onNodeWithTag("noResults").assertDoesNotExist()
    composeTestRule.onAllNodesWithTag("cardItem").assertCountEquals(1)
  }
}
