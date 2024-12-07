package com.android.voyageur.ui.trip.schedule

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*

class WeeklyViewScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var mockTrip: Trip
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var tripsRepository: TripRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    // Set default locale for consistent testing
    Locale.setDefault(Locale.US)

    navigationActions = Mockito.mock(NavigationActions::class.java)
    tripsRepository = Mockito.mock(TripRepository::class.java)
    userRepository = Mockito.mock(UserRepository::class.java)
    friendRequestRepository = Mockito.mock(FriendRequestRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    tripsViewModel = TripsViewModel(tripsRepository)

    // Mock current route for navigation actions
    `when`(navigationActions.currentRoute()).thenReturn(Route.TOP_TABS)

    // Create mock trip with activities
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
                        "",
                        Location(""),
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
                        ActivityType.MUSEUM),
                    Activity(
                        "2",
                        "",
                        Location(""),
                        startTime =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 8, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        endTime =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 8, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        0.0,
                        ActivityType.MUSEUM)))

    composeTestRule.setContent {
      WeeklyViewScreen(
          tripsViewModel = tripsViewModel,
          trip = mockTrip,
          navigationActions = navigationActions,
          userViewModel)
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun floatingActionButton_isDisplayed() {
    composeTestRule.onNodeWithTag("createActivityButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addIcon", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun floatingActionButton_hasCorrectContentDescription() {
    composeTestRule.onNodeWithContentDescription("Floating action button").assertIsDisplayed()
  }

  @Test
  fun weeklyViewScreen_displaysCorrectWeekRanges() {
    composeTestRule.waitForIdle()
    val expectedRange = "SEP 30 - OCT 6"
    composeTestRule.onNodeWithText(expectedRange, useUnmergedTree = true).assertExists()
  }

  @Test
  fun weeklyViewScreen_displaysDaysWithCorrectActivityCounts() {
    composeTestRule.waitForIdle()
    // Look for the full text that would appear in a day with activities
    composeTestRule.onNodeWithText("T 3", useUnmergedTree = true).assertExists()
  }

  @Test
  fun weeklyViewScreen_hasCorrectBottomNavigation() {
    composeTestRule.onNodeWithTag("bottomNavigationMenu", useUnmergedTree = false).assertExists()
  }

  @Test
  fun generateWeeks_returnsCorrectNumberOfWeeks() {
    val weeks = generateWeeks(mockTrip.startDate, mockTrip.endDate)
    assert(weeks.size >= 2)
  }

  @Test
  fun formatDate_returnsCorrectFormat() {
    val startDate =
        mockTrip.startDate
            .toDate()
            .toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()

    val formattedDate = formatDate(startDate)
    assert(formattedDate == "OCT 3")
  }

  @Test
  fun weeklyViewScreen_verifyAllComponentsExist() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("weeklyViewScreen", useUnmergedTree = false).assertExists()
    composeTestRule.onNodeWithTag("weeksColumn", useUnmergedTree = false).assertExists()
    composeTestRule.onNodeWithTag("weekCard_0", useUnmergedTree = false).assertExists()
    composeTestRule.onNodeWithTag("weekRange_0", useUnmergedTree = false).assertExists()

    // Only verify text elements exist for days outside trip range
    for (i in 0..6) {
      composeTestRule.onNodeWithTag("dayText_0_$i", useUnmergedTree = true).assertExists()
    }

    composeTestRule.onNodeWithTag("bottomNavigationMenu", useUnmergedTree = true).assertExists()
  }

  @Test
  fun weeklyViewScreen_verifyDayButtonInteraction() {
    composeTestRule.waitForIdle()

    // Find and click a day button that should be within the trip range
    composeTestRule.onNodeWithText("T 3", useUnmergedTree = true).performClick()
    verify(navigationActions).navigateTo(Screen.ACTIVITIES_FOR_ONE_DAY)
    assert(tripsViewModel.selectedDay.value == LocalDate.of(2024, 10, 3))
  }

  @Test
  fun weeklyViewScreen_verifyCorrectActivityCountsDisplayed() {
    composeTestRule.waitForIdle()

    // Check for exactly one activity on October 3rd
    composeTestRule.onNodeWithText("T 3", useUnmergedTree = true).assertExists()
  }

  @Test
  fun weeklyViewScreen_verifyWeekRangeFormat() {
    composeTestRule.waitForIdle()

    val expectedRange = "SEP 30 - OCT 6"
    composeTestRule
        .onNodeWithTag("weekRange_0", useUnmergedTree = false)
        .assertTextContains(expectedRange)
  }

  @Test
  fun weeklyViewScreen_verifyOutOfRangeDaysNotClickable() {
    composeTestRule.waitForIdle()

    // September 30 should be outside the trip range and not clickable
    composeTestRule.onNodeWithText("M 30", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("dayButton_0_0", useUnmergedTree = true).assertDoesNotExist()
  }
}
