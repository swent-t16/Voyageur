package com.android.voyageur.ui.trip.schedule

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
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
  private var isDailyViewSelected = false

  @Before
  fun setUp() {
    navigationActions = Mockito.mock(NavigationActions::class.java)

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
                        Location("", "", "", ""),
                        startTime =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 3, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        endDate =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 3, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        0,
                        ActivityType.MUSEUM),
                    Activity(
                        "2",
                        "",
                        Location("", "", "", ""),
                        startTime =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 8, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        endDate =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 8, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        0,
                        ActivityType.MUSEUM)))

    // Set the content with WeeklyViewScreen
    composeTestRule.setContent {
      WeeklyViewScreen(
          trip = mockTrip,
          navigationActions = navigationActions,
          onDaySelected = { isDailyViewSelected = true })
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun weeklyViewScreen_displaysCorrectWeekRanges() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("SEP 30 - OCT 6", useUnmergedTree = true).assertExists()
  }

  @Test
  fun weeklyViewScreen_displaysDaysWithCorrectActivityCounts() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("T 3", useUnmergedTree = true).assertExists()
  }

  @Test
  fun dayActivityCount_switchesToDailyView() {
    composeTestRule.waitForIdle()

    // Initially, daily view should not be selected
    assert(!isDailyViewSelected)

    // Click on a day with activities
    composeTestRule.onNodeWithText("T 3", useUnmergedTree = true).performClick()

    // Verify that daily view is now selected
    assert(isDailyViewSelected)
  }

  @Test
  fun weeklyViewScreen_hasCorrectBottomNavigation() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("bottomNavigationMenu", useUnmergedTree = false).assertExists()
  }

  @Test
  fun generateWeeks_returnsCorrectNumberOfWeeks() {
    val weeks = generateWeeks(mockTrip.startDate, mockTrip.endDate)
    assert(weeks.size >= 2) // Should contain at least two weeks for the given date range
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

    // Verify screen structure
    composeTestRule.onNodeWithTag("weeklyViewScreen", useUnmergedTree = false).assertExists()
    composeTestRule.onNodeWithTag("weeksColumn", useUnmergedTree = false).assertExists()

    // Verify week cards
    composeTestRule.onNodeWithTag("weekCard_0", useUnmergedTree = false).assertExists()
    composeTestRule.onNodeWithTag("weekRange_0", useUnmergedTree = false).assertExists()

    // Verify day buttons in first week
    for (i in 0..6) {
      composeTestRule.onNodeWithTag("dayButton_0_$i", useUnmergedTree = false).assertExists()
      composeTestRule.onNodeWithTag("dayText_0_$i", useUnmergedTree = true).assertExists()
      composeTestRule.onNodeWithTag("activityCount_0_$i", useUnmergedTree = true).assertExists()
    }

    // Verify navigation menu
    composeTestRule.onNodeWithTag("bottomNavigationMenu", useUnmergedTree = true).assertExists()
  }

  @Test
  fun weeklyViewScreen_verifyDayButtonInteraction() {
    composeTestRule.waitForIdle()

    // Initially, daily view should not be selected
    assert(!isDailyViewSelected)

    // Click first day button
    composeTestRule.onNodeWithTag("dayButton_0_0", useUnmergedTree = false).performClick()

    // Verify that daily view is now selected
    assert(isDailyViewSelected)
  }

  @Test
  fun weeklyViewScreen_verifyCorrectActivityCountsDisplayed() {
    composeTestRule.waitForIdle()

    // Find and verify activity counts for specific days
    composeTestRule
        .onNodeWithTag("activityCount_0_3", useUnmergedTree = true)
        .assertTextContains("  -  1 activities")
  }

  @Test
  fun weeklyViewScreen_verifyWeekRangeFormat() {
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag("weekRange_0", useUnmergedTree = false)
        .assertTextContains("SEP 30 - OCT 6")
  }
}
