package com.android.voyageur.ui.trip

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class TopTabsTest {
  private val sampleTrip = Trip(name = "Sample Trip")

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var navHostController: NavHostController

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    tripsViewModel = TripsViewModel(tripRepository)
  }

  @Test
  fun displayTextWhenNoTripSelected() {
    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions) }

    // Verify that the "No trip selected" text is displayed
    composeTestRule.onNodeWithText("No trip selected. Should not happen").assertIsDisplayed()
  }

  @Test
  fun topAppBar_DisplaysTripName() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions) }

    // Verify that the TopAppBar displays the trip name
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sample Trip").assertIsDisplayed()
  }

  @Test
  fun tabRow_DisplaysTabsCorrectly() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions) }

    // Verify that each tab is displayed with the correct title
    composeTestRule.onNodeWithTag("tabRow").assertIsDisplayed()
    composeTestRule.onNodeWithText("Schedule").assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
    composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
  }

  @Test
  fun tabRow_SwitchesContentOnTabClick() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions) }

    // Initially, the first tab (Schedule) should be selected
    composeTestRule.onNodeWithText("Schedule").assertIsSelected()

    // Verify that ByDayScreen content is shown initially
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()

    // Switch to the "Activities" tab and verify
    composeTestRule.onNodeWithText("Activities").performClick()
    composeTestRule.onNodeWithText("Activities").assertIsSelected()
    composeTestRule.onNodeWithTag("activitiesScreen").assertIsDisplayed()

    // Switch to the "Settings" tab and verify
    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule.onNodeWithText("Settings").assertIsSelected()
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
  }

  @Test
  fun testCurrentTabIndexForTrip_updatesProperly() {
    tripsViewModel.selectTrip(sampleTrip)

    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithText("Schedule").assertExists()
    composeTestRule.onNodeWithText("Activities").assertExists()
    composeTestRule.onNodeWithText("Settings").assertExists()

    composeTestRule.onNodeWithText("Activities").performClick()
    assert(navigationActions.currentTabIndexForTrip == 1)

    composeTestRule.onNodeWithText("Settings").performClick()
    assert(navigationActions.currentTabIndexForTrip == 2)
    composeTestRule.onNodeWithText("Schedule").performClick()
    assert(navigationActions.currentTabIndexForTrip == 0)
  }
}
