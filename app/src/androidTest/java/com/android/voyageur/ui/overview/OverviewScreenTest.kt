package com.android.voyageur.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class OverviewScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripModelViewModel: TripsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    composeTestRule.setContent { OverviewScreen(tripModelViewModel, navigationActions) }
  }

  @Test
  fun displayTextWhenNotFinalized() {
    composeTestRule.onNodeWithTag("emptyTripPrompt").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBarTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTripButton").assertIsDisplayed()
  }
}
