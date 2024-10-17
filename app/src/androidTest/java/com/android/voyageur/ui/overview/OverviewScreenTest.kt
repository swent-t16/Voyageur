package com.android.voyageur.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.navigation.Screen
import junit.framework.TestCase.assertEquals
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

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripViewModel = TripsViewModel(tripRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    composeTestRule.setContent { OverviewScreen(tripViewModel, navigationActions) }
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
    composeTestRule.onNodeWithTag("topBarTitle").assertIsDisplayed()
    composeTestRule.onNodeWithText("Your trips").assertIsDisplayed()
  }

  @Test
  fun overviewDisplaysCard() {
    // Test contains only one element to test it correctly displays a Card
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                creator = "Andreea",
                participants = listOf("Alex", "Mihai", "Ioana", "Andrei", "Maria", "Matei"),
                name = "Paris Trip",
            ),
        )
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed()
    // Checks that "and .. more" text is displayed
    composeTestRule.onNodeWithTag("additionalParticipantsText").assertIsDisplayed()
  }

  @Test
  fun cardDisplaysAvatar() {
    // Test contains only one element to test it correctly displays a Card
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                creator = "Andreea",
                participants = listOf("Alex"),
                name = "Paris Trip",
            ))
    `when`(tripRepository.getTrips(eq("Andreea"), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(0)(mockTrips)
    }
    tripViewModel.getTrips()
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantAvatar").assertIsDisplayed()
  }

  @Test
  fun noParticipantsDisplaysNoAvatars() {
    // Test contains only one element to test it correctly displays a Card
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                creator = "Andreea",
                participants = emptyList(),
                name = "Paris Trip",
            ))
    `when`(tripRepository.getTrips(eq("Andreea"), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(0)(mockTrips)
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

    // Simulate getting the mock trip from the repository
    `when`(tripRepository.getTrips(eq("Andreea"), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }

    tripViewModel.getTrips()

    // Simulate clicking the trip card
    composeTestRule.onNodeWithTag("cardItem").performClick()

    // Verify the trip is selected and navigation to the BY_DAY screen is called
    assert(tripViewModel.selectedTrip.value == mockTrip)
    verify(navigationActions).navigateTo(screen = Screen.BY_DAY)
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
    assertEquals("No participants.", result0)
    assertEquals("1 Participant:", result1)
    assertEquals("2 Participants:", result2)
  }
}
