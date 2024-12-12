package com.android.voyageur.ui.trip

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
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
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
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
  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel

  private val mockActivity =
      Activity(
          title = "Hiking",
          description = "Trail hiking in the mountains",
          location = Location(name = "Toronto"),
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

    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.ADD_ACTIVITY)
    whenever(tripsViewModel.getNewTripId()).thenReturn("mockTripId")
    doNothing().`when`(tripRepository).updateTrip(any(), any(), any())

    val toastMock = mockk<Toast>(relaxed = true)
    mockkStatic(Toast::class)
    every { Toast.makeText(any(), any<String>(), any()) } returns toastMock
  }

  @Test
  fun addActivityScreen_initialState() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("addActivity").assertIsDisplayed()

    composeTestRule.onNodeWithTag("addActivityTitle").assertTextEquals("Create a New Activity")
    composeTestRule.onNodeWithTag("inputActivityTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputActivityDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputStartTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputEndTime").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputActivityPrice").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputActivityType").assertIsDisplayed()
    composeTestRule.onNodeWithTag("activitySave").assertIsDisplayed()
  }

  @Test
  fun addActivityScreen_datePickerSelectsDate() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputDate").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("inputDate").assertIsDisplayed()
  }

  @Test
  fun addActivityScreen_timePickerSelectsTime() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputStartTime").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("inputEndTime").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
  }

  @Test
  fun addActivityScreen_selectActivityType() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputActivityType").assertHasClickAction()
    composeTestRule.onNodeWithTag("inputActivityType").performClick()
  }

  @Test
  fun addActivityScreen_saveButtonDisabledIfTitleEmpty() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("activitySave").assertIsNotEnabled()
  }

  @Test
  fun addActivityScreen_saveButtonEnabledIfTitleNonEmpty() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputActivityTitle").performTextInput("Hiking")
    composeTestRule.onNodeWithTag("activitySave").assertIsEnabled()
  }

  @Test
  fun addActivityScreen_validPrice() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputActivityPrice").performTextInput("60.0")
    composeTestRule.onNodeWithTag("inputActivityPrice").assertTextContains("60.0")
  }

  @Test
  fun addActivityScreen_invalidPrice() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputActivityPrice").performTextInput("60.2435")
    composeTestRule.onNodeWithTag("activitySave").assertIsNotEnabled()
  }

  @Test
  fun addActivityScreen_activityTypeDropdown() {
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    composeTestRule.onNodeWithTag("activityTypeDropdown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("activityTypeDropdown").performClick()
    composeTestRule.onNodeWithTag("expandedDropdown").assertIsDisplayed()
  }

  @Test
  fun test_endTimestamp_initialization_with_endTime_provided() {
    val calendar = Calendar.getInstance()
    calendar.set(2024, Calendar.NOVEMBER, 21, 0, 0, 0) // Activity date at midnight
    val activityDate = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 15) // Set endTime to 3:30 PM
    calendar.set(Calendar.MINUTE, 30)
    val endTime = calendar.timeInMillis

    val selectedTripStartDate = Timestamp(Date(activityDate))
    val selectedTripEndDate = Timestamp(Date(activityDate + (24 * 60 * 60 * 1000))) // Next day

    val trip =
        Trip(
            startDate = selectedTripStartDate,
            endDate = selectedTripEndDate,
            activities = emptyList())

    val tripsViewModel = mock(TripsViewModel::class.java)
    val mutableStateFlow = MutableStateFlow(trip)
    whenever(tripsViewModel.selectedTrip).thenReturn(mutableStateFlow)

    val normalizedDate =
        Calendar.getInstance()
            .apply {
              timeInMillis = activityDate
              set(Calendar.HOUR_OF_DAY, 0)
              set(Calendar.MINUTE, 0)
              set(Calendar.SECOND, 0)
              set(Calendar.MILLISECOND, 0)
            }
            .time

    val endTimestamp =
        Timestamp(
            Calendar.getInstance()
                .apply {
                  time = normalizedDate
                  set(
                      Calendar.HOUR_OF_DAY,
                      Calendar.getInstance()
                          .apply { timeInMillis = endTime }
                          .get(Calendar.HOUR_OF_DAY))
                  set(
                      Calendar.MINUTE,
                      Calendar.getInstance().apply { timeInMillis = endTime }.get(Calendar.MINUTE))
                }
                .time)

    // Assert
    val expectedCalendar = Calendar.getInstance()
    expectedCalendar.time = normalizedDate
    expectedCalendar.set(Calendar.HOUR_OF_DAY, 15)
    expectedCalendar.set(Calendar.MINUTE, 30)

    assertEquals(expectedCalendar.time, endTimestamp.toDate())
  }

  @Test
  fun test_startTimestamp_initialization_with_startTime_provided() {
    val calendar = Calendar.getInstance()
    calendar.set(2024, Calendar.NOVEMBER, 21, 0, 0, 0) // Activity date at midnight
    val activityDate = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 17) // Set endTime to 5:45 PM
    calendar.set(Calendar.MINUTE, 45)
    val startTime = calendar.timeInMillis

    val selectedTripStartDate = Timestamp(Date(activityDate))
    val selectedTripEndDate = Timestamp(Date(activityDate + (24 * 60 * 60 * 1000))) // Next day

    val trip =
        Trip(
            startDate = selectedTripStartDate,
            endDate = selectedTripEndDate,
            activities = emptyList())

    val tripsViewModel = mock(TripsViewModel::class.java)
    val mutableStateFlow = MutableStateFlow(trip)
    whenever(tripsViewModel.selectedTrip).thenReturn(mutableStateFlow)

    val normalizedDate =
        Calendar.getInstance()
            .apply {
              timeInMillis = activityDate
              set(Calendar.HOUR_OF_DAY, 0)
              set(Calendar.MINUTE, 0)
              set(Calendar.SECOND, 0)
              set(Calendar.MILLISECOND, 0)
            }
            .time

    val startTimestamp =
        Timestamp(
            Calendar.getInstance()
                .apply {
                  time = normalizedDate
                  set(
                      Calendar.HOUR_OF_DAY,
                      Calendar.getInstance()
                          .apply { timeInMillis = startTime }
                          .get(Calendar.HOUR_OF_DAY))
                  set(
                      Calendar.MINUTE,
                      Calendar.getInstance()
                          .apply { timeInMillis = startTime }
                          .get(Calendar.MINUTE))
                }
                .time)

    // Assert
    val expectedCalendar = Calendar.getInstance()
    expectedCalendar.time = normalizedDate
    expectedCalendar.set(Calendar.HOUR_OF_DAY, 17)
    expectedCalendar.set(Calendar.MINUTE, 45)

    assertEquals(expectedCalendar.time, startTimestamp.toDate())
  }

  @Test
  fun editActivityScreen_displaysExistingActivityDetails() {
    tripsViewModel.selectActivity(mockActivity)
    composeTestRule.setContent {
      EditActivityScreen(navigationActions, tripsViewModel, placesViewModel)
    }

    composeTestRule.onNodeWithTag("editActivityScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("inputActivityTitle").assertTextContains("Hiking")
    composeTestRule
        .onNodeWithTag("inputActivityDescription")
        .assertTextContains("Trail hiking in the mountains")
    composeTestRule.onNodeWithTag("searchTextField").assertTextContains("Toronto")
    composeTestRule.onNodeWithTag("inputDate").assertTextContains("03 Oct 2024")
  }

  @Test
  fun editActivityScreen_opensDatePicker() {
    tripsViewModel.selectActivity(mockActivity)
    composeTestRule.setContent {
      EditActivityScreen(navigationActions, tripsViewModel, placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputDate").performClick()
    composeTestRule.onNodeWithTag("datePickerModal").assertIsDisplayed()
  }

  @Test
  fun editActivityScreen_opensStartTimePicker() {
    tripsViewModel.selectActivity(mockActivity)
    composeTestRule.setContent {
      EditActivityScreen(navigationActions, tripsViewModel, placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputStartTime").performClick()
    composeTestRule.onNodeWithTag("timePickerDialog").assertIsDisplayed()
  }

  @Test
  fun editActivityScreen_showsErrorForEmptyTitle() {
    tripsViewModel.selectActivity(mockActivity)
    composeTestRule.setContent {
      EditActivityScreen(navigationActions, tripsViewModel, placesViewModel)
    }

    // Clear the title
    composeTestRule.onNodeWithTag("inputActivityTitle").performTextClearance()
    composeTestRule.onNodeWithTag("activitySave").assertIsNotEnabled()
  }

  @Test
  fun checkActivityLocation() {
    val mockAct =
        Activity(
            title = "Test Location",
            location =
                Location(
                    id = "mockID",
                    name = "Greek Project",
                    address = "Rue de EPFL",
                    lat = 19.9,
                    lng = 65.0),
            startTime =
                Timestamp(
                    Date.from(LocalDateTime.of(2024, 10, 3, 10, 0).toInstant(ZoneOffset.UTC))),
            endTime =
                Timestamp(
                    Date.from(LocalDateTime.of(2024, 10, 3, 11, 0).toInstant(ZoneOffset.UTC))),
            estimatedPrice = 100.0,
            activityType = ActivityType.OUTDOORS)

    tripsViewModel.selectActivity(mockAct)
    composeTestRule.setContent {
      AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
    }

    assertEquals("mockID", mockAct.location.id)
    assertEquals("Greek Project", mockAct.location.name)
    assertEquals("Rue de EPFL", mockAct.location.address)
    assertEquals(19.9, mockAct.location.lat, 0.10)
    assertEquals(65.0, mockAct.location.lng, 0.10)
  }
}
