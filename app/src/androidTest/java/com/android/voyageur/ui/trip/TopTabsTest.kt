package com.android.voyageur.ui.trip

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.NavigationState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn

class TopTabsTest {
  val sampleTrip = Trip(name = "Sample Trip")

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var navHostController: NavHostController
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel
  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var tripInviteRepository: TripInviteRepository
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    tripInviteRepository = mock(TripInviteRepository::class.java)
    tripsViewModel = TripsViewModel(tripRepository, tripInviteRepository)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)
    mockNavigationActions = mock(NavigationActions::class.java)
  }

  @Test
  fun topAppBar_DisplaysTripName() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent {
      TopTabs(tripsViewModel, navigationActions, userViewModel, placesViewModel)
    }

    composeTestRule.onNodeWithTag("topTabs").assertIsDisplayed()
    // Verify that the TopAppBar displays the trip name
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sample Trip").assertIsDisplayed()
  }

  @Test
  fun tabRow_DisplaysTabsCorrectly() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent {
      TopTabs(tripsViewModel, navigationActions, userViewModel, placesViewModel)
    }

    // Verify that each tab is displayed with the correct title
    composeTestRule.onNodeWithTag("tabRow").assertIsDisplayed()
    composeTestRule.onNodeWithText("Schedule").assertIsDisplayed()
    composeTestRule.onNodeWithText("Activities").assertIsDisplayed()
    composeTestRule.onNodeWithText("Photos").assertIsDisplayed()
    composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
  }

  @Test
  fun tabRow_SwitchesContentOnTabClick() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent {
      TopTabs(tripsViewModel, navigationActions, userViewModel, placesViewModel)
    }

    // Initially, the first tab (Schedule) should be selected
    composeTestRule.onNodeWithText("Schedule").assertIsSelected()

    // Verify that ByDayScreen content is shown initially
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()

    // Switch to the "Activities" tab and verify
    composeTestRule.onNodeWithText("Activities").performClick()
    composeTestRule.onNodeWithText("Activities").assertIsSelected()
    composeTestRule.onNodeWithTag("activitiesScreen").assertIsDisplayed()

    // Switch to the "Photos" tab and verify
    composeTestRule.onNodeWithText("Photos").performClick()
    composeTestRule.onNodeWithTag("photosScreen").assertIsDisplayed()

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
    composeTestRule.setContent {
      TopTabs(tripsViewModel, navigationActions, userViewModel, placesViewModel)
    }

    // Verify that each tab is displayed with the correct title
    composeTestRule.onNodeWithText("Schedule").assertExists()
    composeTestRule.onNodeWithText("Activities").assertExists()
    composeTestRule.onNodeWithText("Map").assertExists()
    composeTestRule.onNodeWithText("Photos").assertExists()
    composeTestRule.onNodeWithText("Settings").assertExists()

    // Click on "Activities" tab
    composeTestRule.onNodeWithText("Activities").performClick()

    // Assert that the currentTabIndexForTrip has been updated to 1 (Activities tab)
    assert(navigationActions.getNavigationState().currentTabIndexForTrip == 1)

    // Click on "Map" tab
    composeTestRule.onNodeWithText("Map").performClick()

    // Assert that the currentTabIndexForTrip has been updated to 2 (Map tab)
    assert(navigationActions.getNavigationState().currentTabIndexForTrip == 2)

    // Click on "Photos" tab
    composeTestRule.onNodeWithText("Photos").performClick()

    // Assert that the currentTabIndexForTrip has been updated to 2 (Photos tab)
    assert(navigationActions.getNavigationState().currentTabIndexForTrip == 3)

    // Click on "Settings" tab
    composeTestRule.onNodeWithText("Settings").performClick()

    // Assert that the currentTabIndexForTrip has been updated to 3 (Settings tab)
    assert(navigationActions.getNavigationState().currentTabIndexForTrip == 4)

    // Click on "Schedule" tab
    composeTestRule.onNodeWithText("Schedule").performClick()

    // Assert that the currentTabIndexForTrip has been updated to 0 (Schedule tab)
    assert(navigationActions.getNavigationState().currentTabIndexForTrip == 0)
  }

  @Test
  fun tabRow_SwitchesContentOnTabClickInROV() {
    // Mock NavigationActions to return a NavigationState with isReadOnlyView = true
    val navigationState = NavigationState()
    navigationState.isReadOnlyView = true
    doReturn(navigationState).`when`(mockNavigationActions).getNavigationState()
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent {
      TopTabs(tripsViewModel, mockNavigationActions, userViewModel, placesViewModel)
    }

    // Initially, the first tab (Schedule) should be selected
    composeTestRule.onNodeWithText("Schedule").assertIsSelected()

    // Verify that ByDayScreen content is shown initially
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()

    // Switch to the "Activities" tab and verify
    composeTestRule.onNodeWithText("Activities").performClick()
    composeTestRule.onNodeWithText("Activities").assertIsSelected()
    composeTestRule.onNodeWithTag("activitiesScreen").assertIsDisplayed()

    // Assert "Photos" tab does not exist
    composeTestRule.onNodeWithText("Photos").assertDoesNotExist()
    composeTestRule.onNodeWithTag("photosScreen").assertDoesNotExist()

    // Assert "Settings" tab does not exist
    composeTestRule.onNodeWithText("Settings").assertDoesNotExist()
    composeTestRule.onNodeWithTag("settingsScreen").assertDoesNotExist()
  }
}
