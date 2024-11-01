package com.android.voyageur.ui.trip.schedule

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ByDayScreenTest {
  private val sampleTrip = Trip(name = "Sample Trip")

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent { ByDayScreen(sampleTrip, navigationActions) }
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    // composeTestRule.onNodeWithTag("emptyByDayPrompt").assertIsDisplayed()
  }
  /*
   @Test
   fun displaysCorrectTripName() {
     composeTestRule.setContent { ByDayScreen(sampleTrip, navigationActions) }
     composeTestRule
         .onNodeWithTag("emptyByDayPrompt")
         .assertTextContains(
             "You're viewing the ByDay screen for Sample Trip, but it's not implemented yet.")
   }
  */

  @Test
  fun displaysBottomNavigationCorrectly() {
    //    tripsViewModel.selectTrip(Trip(name = "Sample Trip"))
    composeTestRule.setContent { ByDayScreen(sampleTrip, navigationActions) }

    // Check that the bottom navigation menu is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    // Verify that the bottom navigation has items with correct actions
    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }
}
