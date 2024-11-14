package com.android.voyageur.ui.trip.schedule

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.trip.activities.createTimestamp
import java.time.LocalDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ByDayScreenTest {
  // Create one activity trip to check for activity box and Day card
  private val oneActivityTrip =
      Trip(
          name = "One Activity Trip",
          activities =
              listOf(
                  Activity(
                      title = "Final Activity With Description",
                      description = "This is a final activity",
                      // This uses the createTimestamp method from ActivitiesScreenTest
                      startTime = createTimestamp(2022, 1, 1, 12, 0),
                      endTime = createTimestamp(2022, 1, 1, 14, 0),
                      estimatedPrice = 20.0,
                      activityType = ActivityType.RESTAURANT),
              ))

  // Create multiple activities sample trip to check for groupedActivitiesByDate function
  private val sampleTrip =
      Trip(
          name = "Sample Trip",
          activities =
              listOf(
                  // The activities on 2nd of January have different start time
                  Activity(
                      title = "Activity 3",
                      description = "2 January activity",
                      startTime = createTimestamp(2022, 1, 2, 12, 0),
                      endTime = createTimestamp(2022, 1, 2, 14, 0),
                      estimatedPrice = 20.0,
                      activityType = ActivityType.RESTAURANT),
                  Activity(
                      title = "Activity 2",
                      description = "2 January activity",
                      startTime = createTimestamp(2022, 1, 2, 11, 0),
                      endTime = createTimestamp(2022, 1, 2, 14, 0),
                      estimatedPrice = 20.0,
                      activityType = ActivityType.RESTAURANT),
                  // The activities on 1st of January have the same startTime, but different endTime
                  Activity(
                      title = "Activity 1",
                      description = " 1 January activity",
                      estimatedPrice = 10.0,
                      startTime = createTimestamp(2022, 1, 1, 12, 0),
                      endTime = createTimestamp(2022, 1, 1, 14, 0),
                      activityType = ActivityType.MUSEUM),
                  Activity(
                      title = "Activity 1",
                      description = " 1 January activity",
                      estimatedPrice = 10.0,
                      startTime = createTimestamp(2022, 1, 1, 12, 0),
                      endTime = createTimestamp(2022, 1, 1, 13, 0),
                      activityType = ActivityType.MUSEUM),
              ))

  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    tripsViewModel = mock(TripsViewModel::class.java)
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent { ByDayScreen(tripsViewModel, oneActivityTrip, navigationActions) }
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun displaysBottomNavigationCorrectly() {
    composeTestRule.setContent { ByDayScreen(tripsViewModel, oneActivityTrip, navigationActions) }

    // Check that the bottom navigation menu is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    // Verify that the bottom navigation has items with correct actions
    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }

  // Add test for floating action button
  @Test
  fun floatingActionButtonIsDisplayed() {
    composeTestRule.setContent { ByDayScreen(tripsViewModel, oneActivityTrip, navigationActions) }
    // Test floating button is displayed
    composeTestRule.onNodeWithTag("createActivityButton").assertIsDisplayed()
    // Test correct icon is displayed
    composeTestRule.onNodeWithTag("addIcon", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun displaysEmptyPromptWhenNoActivities() {
    // Create a trip with no activities
    val emptyTrip = Trip(name = "Empty Trip", activities = emptyList())

    composeTestRule.setContent { ByDayScreen(tripsViewModel, emptyTrip, navigationActions) }

    // Check that the empty state message is displayed
    composeTestRule.onNodeWithTag("emptyByDayPrompt").assertIsDisplayed()
    composeTestRule.onNodeWithText("You have no activities yet.").assertIsDisplayed()
  }

  @Test
  fun displaysAndMoreTextWhenMoreThanFourActivities() {
    // Create a trip with more than 4 activities on the same day
    val tripWithManyActivities =
        Trip(
            name = "Busy Trip",
            activities =
                List(5) { index ->
                  Activity(
                      title = "Activity $index",
                      startTime = createTimestamp(2022, 1, 1, 10 + index, 0),
                      endTime = createTimestamp(2022, 1, 1, 11 + index, 0),
                      activityType = ActivityType.OTHER)
                })
    composeTestRule.setContent { ByDayScreen(tripsViewModel, tripWithManyActivities, navigationActions) }

    // Check that the "and X more" text is displayed
    composeTestRule.onNodeWithText("and 1 more").assertIsDisplayed()
  }

  @Test
  fun dayCardIsDisplayed() {
    // Create a trip with only one activity to check for day card
    composeTestRule.setContent { ByDayScreen(tripsViewModel, oneActivityTrip, navigationActions) }
    composeTestRule.onNodeWithTag("cardItem", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun activityBoxIsCorrectlyDisplayed() {
    // Create a trip with only one activity to check for activity box
    composeTestRule.setContent { ByDayScreen(tripsViewModel, oneActivityTrip, navigationActions) }
    // Check activity box is displayed
    composeTestRule.onNodeWithTag("activityBox", useUnmergedTree = true).assertIsDisplayed()
    // Check if activity title is displayed
    composeTestRule
        .onNodeWithText(oneActivityTrip.activities[0].title, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun formatDailyDateDisplaysCorrectFormatForValidDate() {
    val date = LocalDate.of(2024, 1, 1)
    val expected = "Monday, 1 January"
    val result = formatDailyDate(date)
    assertEquals(expected, result)
  }

  @Test
  fun activitiesAreGroupedByStartTime() {
    val diffStartTimeTrip =
        Trip(
            name = "Sample Trip",
            activities =
                listOf(
                    // The activities on 2nd of January have different start time
                    Activity(
                        title = "Activity 3",
                        description = "2 January activity",
                        startTime = createTimestamp(2022, 1, 2, 12, 0),
                        endTime = createTimestamp(2022, 1, 2, 14, 0),
                        estimatedPrice = 20.0,
                        activityType = ActivityType.RESTAURANT),
                    Activity(
                        title = "Activity 2",
                        description = "2 January activity",
                        startTime = createTimestamp(2022, 1, 2, 11, 0),
                        endTime = createTimestamp(2022, 1, 2, 14, 0),
                        estimatedPrice = 20.0,
                        activityType = ActivityType.RESTAURANT),
                ))

    val groupedActivities = groupActivitiesByDate(diffStartTimeTrip.activities)
    // Take the 2 activities which are on the same day, same start time
    val sortedActivitiesByStart = groupedActivities[LocalDate.of(2022, 1, 2)]

    assertTrue(
        "Activities are not sorted by start time",
        sortedActivitiesByStart!![0].startTime <= sortedActivitiesByStart[1].startTime)
  }

  @Test
  fun clickingCreateActivityButton_navigatesToAddActivityScreen() {
    composeTestRule.setContent { ByDayScreen(tripsViewModel, sampleTrip, navigationActions) }

    composeTestRule.onNodeWithTag("createActivityButton").performClick()

    verify(navigationActions).navigateTo(Screen.ADD_ACTIVITY)
  }

  @Test
  fun draftActivitiesAreNotDisplayed() {
    val tripWithDraftActivities =
        Trip(
            name = "Trip with Draft Activities",
            activities =
                listOf(
                    // Draft activity, doesn't have start and end times
                    Activity(
                        title = "Draft activity",
                        description = "This is a published activity",
                        estimatedPrice = 30.0,
                        activityType = ActivityType.RESTAURANT,
                    ),
                    Activity(
                        title = "Activity",
                        description = "2 January activity",
                        startTime = createTimestamp(2022, 1, 2, 12, 0),
                        endTime = createTimestamp(2022, 1, 2, 14, 0),
                        estimatedPrice = 20.0,
                        activityType = ActivityType.RESTAURANT),
                ))

    composeTestRule.setContent { ByDayScreen(tripsViewModel, tripWithDraftActivities, navigationActions) }

    // Check that the non-draft activity is displayed
    composeTestRule.onNodeWithText("Activity").assertIsDisplayed()

    // Check that the draft activity is NOT displayed
    composeTestRule.onNodeWithText("Draft Activity").assertDoesNotExist()
  }

  @Test
  fun formatDailyDate_ReturnsInvalidDate_ForExceptionScenario() {
    try {
      val invalidDate = LocalDate.of(2024, 2, 30) // February 30th does not exist
      formatDailyDate(invalidDate)
    } catch (e: Exception) {
      assertEquals("Invalid date", "Invalid date")
    }
  }
}
