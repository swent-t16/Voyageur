package com.android.voyageur.ui.trip.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.NavigationState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doReturn

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
  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var navHostController: NavHostController
  private lateinit var tripsViewModel: TripsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripsViewModel = mock(TripsViewModel::class.java)
    navHostController = Mockito.mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    mockNavigationActions = mock(NavigationActions::class.java)
    val selectedTripFlow = MutableStateFlow(sampleTrip)
    `when`(tripsViewModel.selectedTrip).thenReturn(selectedTripFlow)
    val selectedDayFlow = MutableStateFlow<LocalDate?>(LocalDate.of(2022, 1, 1))
    `when`(tripsViewModel.selectedDay).thenReturn(selectedDayFlow)
    `when`(tripsViewModel.getActivitiesForSelectedTrip()).thenReturn(sampleTrip.activities)
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

  @Test
  fun clickingDeleteButton_displaysDeleteActivityAlertDialog() {
    composeTestRule.setContent { ActivitiesForOneDayScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("expandIcon_${sampleTrip.activities[1].title}").performClick()
    composeTestRule.onNodeWithTag("deleteIcon_${sampleTrip.activities[1].title}").performClick()
    composeTestRule.onNodeWithTag("deleteActivityAlertDialog").assertIsDisplayed()
  }

  @Test
  fun clickingDeleteButton_removesActivityFromTrip() {
    composeTestRule.setContent { ActivitiesForOneDayScreen(tripsViewModel, navigationActions) }

    // test with final activity
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[1].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expandIcon_${sampleTrip.activities[1].title}").performClick()
    composeTestRule.onNodeWithTag("deleteIcon_${sampleTrip.activities[1].title}").performClick()
    composeTestRule.onNodeWithTag("confirmDeleteButton").performClick()
    composeTestRule
        .onNodeWithTag("cardItem_${sampleTrip.activities[1].title}")
        .assertIsNotDisplayed()
    verify(tripsViewModel).removeActivityFromTrip(sampleTrip.activities[1])
  }

  @Test
  fun deleteAndEditButtons_NotDisplayedInROV() {
    // Mock NavigationActions to return a NavigationState with isReadOnlyView = true
    val navigationState = NavigationState()
    navigationState.isReadOnlyView = true
    doReturn(navigationState).`when`(mockNavigationActions).getNavigationState()
    composeTestRule.setContent { ActivitiesForOneDayScreen(tripsViewModel, mockNavigationActions) }

    // Assert the delete button is not displayed
    composeTestRule.onNodeWithTag("expandIcon_${sampleTrip.activities[1].title}").isDisplayed()
    composeTestRule
        .onNodeWithTag("deleteIcon_${sampleTrip.activities[1].title}")
        .assertDoesNotExist()
    composeTestRule.onNodeWithTag("deleteActivityAlertDialog").assertDoesNotExist()
    // Assert the edit button is not displayed
    composeTestRule.onNodeWithTag("editIcon_${sampleTrip.activities[1].title}").assertDoesNotExist()
  }
}
