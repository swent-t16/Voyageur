package com.android.voyageur.ui.trip

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class TopTabsTest {
  val sampleTrip = Trip(name = "Sample Trip")

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var navHostController: NavHostController
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    tripsViewModel = TripsViewModel(tripRepository)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
  }

  @Test
  fun topAppBar_DisplaysTripName() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions, userViewModel) }

    // Verify that the TopAppBar displays the trip name
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sample Trip").assertIsDisplayed()
  }

  @Test
  fun tabRow_DisplaysTabsCorrectly() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions, userViewModel) }

    // Verify that each tab is displayed with the correct title
    composeTestRule.onNodeWithTag("tabRow").assertIsDisplayed()
    composeTestRule.onNodeWithText("Schedule").assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
    composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
  }

  @Test
  fun tabRow_SwitchesContentOnTabClick() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions, userViewModel) }

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
    // Select the sample trip to set up the test state
    tripsViewModel.selectTrip(sampleTrip)

    // Set the content to launch the composable
    composeTestRule.setContent { TopTabs(tripsViewModel, navigationActions, userViewModel) }

    // Verify that each tab is displayed with the correct title
    composeTestRule.onNodeWithText("Schedule").assertExists()
    composeTestRule.onNodeWithText("Activities").assertExists()
    composeTestRule.onNodeWithText("Settings").assertExists()

    // Click on "Activities" tab
    composeTestRule.onNodeWithText("Activities").performClick()

    // Assert that the currentTabIndexForTrip has been updated to 1 (Activities tab)
    assert(navigationActions.getNavigationState().currentTabIndexForTrip == 1)

    // Click on "Settings" tab
    composeTestRule.onNodeWithText("Settings").performClick()

    // Assert that the currentTabIndexForTrip has been updated to 2 (Settings tab)
    assert(navigationActions.getNavigationState().currentTabIndexForTrip == 2)

    // Click on "Schedule" tab
    composeTestRule.onNodeWithText("Schedule").performClick()

    // Assert that the currentTabIndexForTrip has been updated to 0 (Schedule tab)
    assert(navigationActions.getNavigationState().currentTabIndexForTrip == 0)
  }
}
