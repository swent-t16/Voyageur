package com.android.sample.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.navigation.NavigationActions
import com.android.sample.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ProfileScreenTest {
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    `when`(navigationActions.currentRoute()).thenReturn(Route.PROFILE)
    composeTestRule.setContent { ProfileScreen(navigationActions) }
  }

  @Test
  fun displayTextWhenNotFinalized() {
    composeTestRule.onNodeWithTag("emptyProfilePrompt").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }
}
