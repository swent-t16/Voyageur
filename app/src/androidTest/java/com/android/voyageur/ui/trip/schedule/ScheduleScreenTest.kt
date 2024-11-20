package com.android.voyageur.ui.trip.schedule

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class ScheduleScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var mockTrip: Trip
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var navHostController: NavHostController
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    // Set default locale for consistent testing
    Locale.setDefault(Locale.US)
    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    tripsViewModel = Mockito.mock(TripsViewModel::class.java)

    navHostController = Mockito.mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)

    mockTrip =
        Trip(
            id = "test-trip",
            name = "London Trip",
            startDate =
                Timestamp(Date.from(LocalDateTime.of(2024, 10, 3, 0, 0).toInstant(ZoneOffset.UTC))),
            endDate =
                Timestamp(Date.from(LocalDateTime.of(2024, 11, 4, 0, 0).toInstant(ZoneOffset.UTC))),
            activities =
                listOf(
                    Activity(
                        "1",
                        "Museum Visit",
                        Location("", "", "", ""),
                        startTime =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 3, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        endTime =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 3, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        0.0,
                        ActivityType.MUSEUM)))

    composeTestRule.setContent {
      ScheduleScreen(
          tripsViewModel = tripsViewModel,
          trip = mockTrip,
          navigationActions = navigationActions,
          userViewModel)
    }
  }

  @Test
  fun scheduleScreen_initialStateIsDaily() {
    // Initially Daily view should be shown
    composeTestRule.onNodeWithText("Daily").assertIsEnabled()
    composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
    composeTestRule.onNodeWithTag("byDayScreen").assertExists()
  }

  @Test
  fun scheduleScreen_toggleToWeeklyView() {
    // Click Weekly button
    composeTestRule.onNodeWithText("Weekly").performClick()

    // Verify Weekly view is shown
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertExists()
    composeTestRule.onNodeWithTag("byDayScreen").assertDoesNotExist()
  }

  @Test
  fun scheduleScreen_toggleBetweenViews() {
    // Start with Daily view
    composeTestRule.onNodeWithTag("byDayScreen").assertExists()

    // Switch to Weekly view
    composeTestRule.onNodeWithText("Weekly").performClick()
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertExists()
    composeTestRule.onNodeWithTag("byDayScreen").assertDoesNotExist()

    // Switch back to Daily view
    composeTestRule.onNodeWithText("Daily").performClick()
    composeTestRule.onNodeWithTag("byDayScreen").assertExists()
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertDoesNotExist()
  }

  @Test
  fun scheduleScreen_verifyViewToggleVisuals() {
    // Verify both buttons and separator exist
    composeTestRule.onNodeWithText("Daily").assertExists()
    composeTestRule.onNodeWithText("Weekly").assertExists()
    composeTestRule.onNodeWithText(" / ").assertExists()
  }

  @Test
  fun scheduleScreen_toggleButtonsAreClickable() {
    // Verify both buttons are enabled and can be clicked
    composeTestRule.onNodeWithText("Daily").assertHasClickAction()
    composeTestRule.onNodeWithText("Weekly").assertHasClickAction()
  }

  @Test
  fun scheduleScreen_verifyProperViewSwitching() {
    // Start with Daily view
    composeTestRule.onNodeWithTag("byDayScreen").assertExists()

    // Switch to Weekly
    composeTestRule.onNodeWithText("Weekly").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertExists()

    // Back to Daily
    composeTestRule.onNodeWithText("Daily").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("byDayScreen").assertExists()
  }

  @Test
  fun scheduleScreen_verifyWeeklyViewShowsCorrectData() {
    // Switch to Weekly view
    composeTestRule.onNodeWithText("Weekly").performClick()

    // Verify the date range is displayed
    composeTestRule.onNodeWithText("SEP 30 - OCT 6", useUnmergedTree = true).assertExists()

    // Verify the day with activity is shown
    composeTestRule.onNodeWithText("T 3", useUnmergedTree = true).assertExists()
  }

  @Test
  fun scheduleScreen_verifyToggleButtonsStayEnabled() {
    // Initially both should be enabled
    composeTestRule.onNodeWithText("Daily").assertIsEnabled()
    composeTestRule.onNodeWithText("Weekly").assertIsEnabled()

    // After switching to Weekly, both should still be enabled
    composeTestRule.onNodeWithText("Weekly").performClick()
    composeTestRule.onNodeWithText("Daily").assertIsEnabled()
    composeTestRule.onNodeWithText("Weekly").assertIsEnabled()
  }

  @Test
  fun checkIfIsDailyViewSelected_updatesProperly() {
    assert(navigationActions.getNavigationState().isDailyViewSelected)
    composeTestRule.onNodeWithText("Weekly").performClick()

    assert(!navigationActions.getNavigationState().isDailyViewSelected)
    composeTestRule.onNodeWithText("Daily").performClick()
    assert(navigationActions.getNavigationState().isDailyViewSelected)
  }
}
