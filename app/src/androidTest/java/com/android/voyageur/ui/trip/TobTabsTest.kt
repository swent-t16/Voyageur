package com.android.voyageur.ui.trip

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class TopTabsTest {
  val sampleTrip = Trip(name = "Sample Trip")

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripsViewModel = TripsViewModel(tripRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Route.TOP_TABS)
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
}
