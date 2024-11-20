package com.android.voyageur.ui.trip

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.trip.activities.EditActivityScreen
import com.google.firebase.Timestamp
import io.mockk.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class AddActivityScreenTest {

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var context: Context

  private val mockActivity =
      Activity(
          title = "Hiking",
          description = "Trail hiking in the mountains",
          location = Location("Canada", "Toronto", null, null),
          startTime =
              Timestamp(Date.from(LocalDateTime.of(2024, 10, 3, 10, 0).toInstant(ZoneOffset.UTC))),
          endTime =
              Timestamp(Date.from(LocalDateTime.of(2024, 10, 3, 11, 0).toInstant(ZoneOffset.UTC))),
          estimatedPrice = 100.0,
          activityType = ActivityType.OUTDOORS)

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripsViewModel = TripsViewModel(tripRepository)
    context = ApplicationProvider.getApplicationContext()

    `when`(navigationActions.currentRoute()).thenReturn(Screen.ADD_ACTIVITY)
    whenever(tripsViewModel.getNewTripId()).thenReturn("mockTripId")
    doNothing().`when`(tripRepository).updateTrip(any(), any(), any())

    val toastMock = mockk<Toast>(relaxed = true)
    mockkStatic(Toast::class)
    every { Toast.makeText(any(), any<String>(), any()) } returns toastMock
  }

  @Test
  fun addActivityScreen_initialState() {
    composeTestRule.setContent { AddActivityScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("addActivityTitle").assertTextEquals("Create a New Activity")
    composeTestRule.onNodeWithTag("inputActivityTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputActivityDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputActivityLocation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputStartTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputEndTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputActivityPrice").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputActivityType").assertIsDisplayed()
    composeTestRule.onNodeWithTag("activitySave").assertIsDisplayed()
  }

  @Test
  fun addActivityScreen_datePickerSelectsDate() {
    composeTestRule.setContent { AddActivityScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputDate").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("inputDate").assertIsDisplayed()
  }

  @Test
  fun addActivityScreen_timePickerSelectsTime() {
    composeTestRule.setContent { AddActivityScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputStartTime").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("inputEndTime").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
  }

  @Test
  fun addActivityScreen_selectActivityType() {
    composeTestRule.setContent { AddActivityScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputActivityType").assertHasClickAction()
    composeTestRule.onNodeWithTag("inputActivityType").performClick()
  }

  @Test
  fun addActivityScreen_saveButtonDisabledIfTitleEmpty() {
    composeTestRule.setContent { AddActivityScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("activitySave").assertIsNotEnabled()
  }

  @Test
  fun addActivityScreen_saveButtonEnabledIfTitleNonEmpty() {
    composeTestRule.setContent { AddActivityScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputActivityTitle").performTextInput("Hiking")
    composeTestRule.onNodeWithTag("activitySave").assertIsEnabled()
  }

  @Test
  fun addActivityScreen_validActivityCreated() {
    composeTestRule.setContent { AddActivityScreen(tripsViewModel, navigationActions) }

    val trip =
        Trip(
            id = "editTripId",
            creator = "mockUserId",
            description = "Existing trip",
            name = "Existing Trip",
            locations = listOf(Location(country = "France", city = "Paris")),
            startDate = Timestamp(Date()),
            endDate = Timestamp(Date()),
            activities = listOf(),
            type = TripType.TOURISM,
            imageUri = "someUri")

    tripsViewModel.selectTrip(trip)

    composeTestRule.onNodeWithTag("inputActivityTitle").performTextInput("Hiking")
    composeTestRule.onNodeWithTag("inputDate").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("activitySave").performClick()

    verify(tripRepository).updateTrip(any(), any(), any())
  }

  @Test
  fun editActivityScreen_displaysExistingActivityDetails() {
    tripsViewModel.selectActivity(mockActivity)
    composeTestRule.setContent { EditActivityScreen(navigationActions, tripsViewModel) }

    composeTestRule.onNodeWithTag("inputActivityTitle").assertTextContains("Hiking")
    composeTestRule
        .onNodeWithTag("inputActivityDescription")
        .assertTextContains("Trail hiking in the mountains")
    composeTestRule.onNodeWithTag("inputActivityLocation").assertTextContains("Toronto")
    composeTestRule.onNodeWithTag("inputDate").assertTextContains("03 Oct 2024")
  }

  @Test
  fun editActivityScreen_opensDatePicker() {
    tripsViewModel.selectActivity(mockActivity)
    composeTestRule.setContent { EditActivityScreen(navigationActions, tripsViewModel) }

    composeTestRule.onNodeWithTag("inputDate").performClick()
    composeTestRule.onNodeWithTag("datePickerModal").assertIsDisplayed()
  }

  @Test
  fun editActivityScreen_opensStartTimePicker() {
    tripsViewModel.selectActivity(mockActivity)
    composeTestRule.setContent { EditActivityScreen(navigationActions, tripsViewModel) }

    composeTestRule.onNodeWithTag("inputStartTime").performClick()
    composeTestRule.onNodeWithTag("timePickerDialog").assertIsDisplayed()
  }

  @Test
  fun editActivityScreen_showsErrorForEmptyTitle() {
    tripsViewModel.selectActivity(mockActivity)
    composeTestRule.setContent { EditActivityScreen(navigationActions, tripsViewModel) }

    // Clear the title
    composeTestRule.onNodeWithTag("inputActivityTitle").performTextClearance()
    composeTestRule.onNodeWithTag("activitySave").assertIsNotEnabled()
  }
}
