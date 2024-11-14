package com.android.voyageur.ui.trip.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ActivitiesForOneDayScreenTest {
  private val sampleTrip =
      Trip(
          name = "Sample Trip",
          activities =
              listOf(
                  Activity(
                      title = "Draft Activity",
                      description = "This is a draft activity",
                      estimatedPrice = 0.0,
                      activityType = ActivityType.WALK),
                  Activity(
                      title = "Final Activity 1",
                      description = "This is a final activity",
                      startTime = createTimestamp(2022, 1, 1, 12, 0),
                      endTime = createTimestamp(2022, 1, 1, 14, 0),
                      estimatedPrice = 20.0,
                      activityType = ActivityType.RESTAURANT),
                  Activity(
                      title = "Final Activity 2",
                      estimatedPrice = 10.0,
                      startTime = createTimestamp(2022, 1, 2, 12, 0),
                      endTime = createTimestamp(2022, 1, 2, 14, 0),
                      activityType = ActivityType.MUSEUM),
              ))

  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripsViewModel = mock(TripsViewModel::class.java)
    navigationActions = mock(NavigationActions::class.java)
    val selectedTripFlow = MutableStateFlow(sampleTrip)
    `when`(tripsViewModel.selectedTrip).thenReturn(selectedTripFlow)
    val selectedDayFlow = MutableStateFlow<LocalDate?>(LocalDate.of(2022, 1, 1))

    `when`(tripsViewModel.selectedDay).thenReturn(selectedDayFlow)
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent { ActivitiesForOneDayScreen(tripsViewModel, navigationActions) }
    composeTestRule.onNodeWithTag("activitiesForOneDayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createActivityButton").assertIsDisplayed()
  }

  @Test
  fun displaysOnlyActivitiesForSelectedDay() {
    composeTestRule.setContent { ActivitiesForOneDayScreen(tripsViewModel, navigationActions) }
    composeTestRule.onNodeWithText("Final Activity 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Final Activity 2").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Draft Activity").assertIsNotDisplayed()
  }

  @Test
  fun displaysNoActivitiesText() {
    val selectedDayFlow = MutableStateFlow<LocalDate?>(LocalDate.of(2022, 1, 3))
    `when`(tripsViewModel.selectedDay).thenReturn(selectedDayFlow)

    composeTestRule.setContent { ActivitiesForOneDayScreen(tripsViewModel, navigationActions) }
    composeTestRule.onNodeWithText("You have no activities yet.").assertIsDisplayed()
  }
}
