package com.android.voyageur.ui.trip.settings

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.LIST_TRIP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class SettingsScreenTest {
  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripsViewModel = TripsViewModel(tripRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Route.SETTINGS)
    val sampleTrip = Trip(name = "Sample Trip")
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { SettingsScreen(tripsViewModel, navigationActions) }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
  }

  @Test
  fun displaysTopBarCorrectly() {

    // Check that the top bar with the title is displayed
    composeTestRule.onNodeWithText("Settings:").assertIsDisplayed()

    // Check that the Home icon button is displayed and clickable
    composeTestRule.onNodeWithTag("backToOverviewButton").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithContentDescription("Home").assertIsDisplayed().assertHasClickAction()
  }

  @Test
  fun navigatesToOverviewOnHomeIconClick() {

    // Simulate a click on the Home icon button
    composeTestRule.onNodeWithContentDescription("Home").performClick()

    // Verify that the navigation to the Overview screen was triggered
    verify(navigationActions).navigateTo(Screen.OVERVIEW)
  }

  @Test
  fun displaysCorrectTripName() {
    composeTestRule
        .onNodeWithTag("emptySettingsPrompt")
        .assertTextContains(
            "You're viewing the Settings screen for Sample Trip, but it's not implemented yet.")
  }

  @Test
  fun displaysBottomNavigationCorrectly() {

    // Check that the bottom navigation menu is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    // Verify that the bottom navigation has items with correct actions
    LIST_TRIP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }
}
