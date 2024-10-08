package com.android.sample.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.Route
import com.android.voyageur.ui.overview.OverviewScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class OverviewScreenTest {
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    composeTestRule.setContent { OverviewScreen(navigationActions) }
  }

  @Test
  fun displayTextWhenNotFinalized() {
    composeTestRule.onNodeWithTag("emptyTripPrompt").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }
}
