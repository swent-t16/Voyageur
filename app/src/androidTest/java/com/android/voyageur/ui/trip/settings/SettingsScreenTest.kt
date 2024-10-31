package com.android.voyageur.ui.trip.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
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

class SettingsScreenTest {
  private val sampleTrip = Trip(name = "Sample Trip")

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent { SettingsScreen(sampleTrip, navigationActions) }
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emptySettingsPrompt").assertIsDisplayed()
  }

  @Test
  fun displaysCorrectTripName() {
    composeTestRule.setContent { SettingsScreen(sampleTrip, navigationActions) }
    composeTestRule
        .onNodeWithTag("emptySettingsPrompt")
        .assertTextContains(
            "You're viewing the Settings screen for Sample Trip, but it's not implemented yet.")
  }

  @Test
  fun displaysBottomNavigationCorrectly() {
    //    tripsViewModel.selectTrip(Trip(name = "Sample Trip"))
    composeTestRule.setContent { SettingsScreen(sampleTrip, navigationActions) }

    // Check that the bottom navigation menu is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    // Verify that the bottom navigation has items with correct actions
    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }
}
