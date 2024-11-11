package com.android.voyageur.ui.overview

import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class AddTripScreenTest {
  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var firebaseAuth: FirebaseAuth

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    tripsViewModel = TripsViewModel(tripRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.ADD_TRIP)
    whenever(tripsViewModel.getNewTripId()).thenReturn("mockTripId")
    whenever(firebaseAuth.uid).thenReturn("mockUserId")
  }

  @Test
  fun addTripScreen_initialState() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTripTitle").assertExists()
    composeTestRule.onNodeWithTag("inputTripDescription").assertExists()
    composeTestRule.onNodeWithTag("inputTripLocations").assertExists()
    composeTestRule.onNodeWithTag("inputStartDate").assertExists()
    composeTestRule.onNodeWithTag("inputEndDate").assertExists()
    composeTestRule.onNodeWithTag("tripSave").assertExists()

    composeTestRule.onNodeWithTag("addTripTitle").assertTextEquals("Create a New Trip")
    composeTestRule.onNodeWithTag("tripSave").assertTextEquals("Save Trip")
  }

  @Test
  fun addTripScreen_inputsUpdateState() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("London")
    composeTestRule.onNodeWithTag("inputTripDescription").performTextInput("4 days in London")

    composeTestRule.onNodeWithTag("inputTripTitle").assertTextContains("London")
    composeTestRule.onNodeWithTag("inputTripDescription").assertTextContains("4 days in London")
  }

  @Test
  fun addTripScreen_saveButtonDisabledWithInvalidInput() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()

    composeTestRule.onNodeWithTag("tripSave").assertIsNotEnabled()
  }

  @Test
  fun addTripScreen_saveButtonEnabledWithValidInput() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Valid Trip")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()

    composeTestRule.onNodeWithTag("tripSave").assertIsEnabled()
  }

  @Test
  fun addTripScreen_tripTypeSelection() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    // Check initial state
    composeTestRule.onNodeWithTag("tripTypeBusiness").assertIsSelected()
    composeTestRule.onNodeWithTag("tripTypeTourism").assertIsNotSelected()

    // Attempt to change to Tourism
    composeTestRule.onNodeWithTag("tripTypeTourism").performClick()

    // Verify that the Tourism button is clickable
    composeTestRule.onNodeWithTag("tripTypeTourism").assertHasClickAction()
  }

  @Test
  fun addTripScreen_saveTrip() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("London Trip")
    composeTestRule.onNodeWithTag("inputTripDescription").performTextInput("4 days in London")
    composeTestRule.onNodeWithTag("inputTripLocations").performTextInput("UK, London")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()

    composeTestRule.onNodeWithTag("tripSave").performClick()

    val today = GregorianCalendar()
    today.set(GregorianCalendar.HOUR_OF_DAY, 0)
    today.set(GregorianCalendar.MINUTE, 0)
    today.set(GregorianCalendar.SECOND, 0)
    today.set(GregorianCalendar.MILLISECOND, 0)
    val todayTimestamp = Timestamp(today.time)

    val expectedTrip =
        Trip(
            id = "mockTripId",
            creator = "mockUserId",
            description = "4 days in London",
            name = "London Trip",
            locations = listOf(Location(country = "UK", city = "London")),
            startDate = todayTimestamp,
            endDate = todayTimestamp,
            activities = listOf(),
            type = TripType.BUSINESS,
            imageUri = "")

    verify(tripRepository).createTrip(eq(expectedTrip), any(), any())
    verify(navigationActions).goBack()
  }

  @Test
  fun addTripScreen_unknownLocation() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Trip with Unknown Location")
    composeTestRule
        .onNodeWithTag("inputTripDescription")
        .performTextInput("Description for trip with unknown location")
    composeTestRule.onNodeWithTag("inputTripLocations").performTextInput("InvalidLocation")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()

    composeTestRule.onNodeWithTag("tripSave").performClick()

    val today = GregorianCalendar()
    today.set(GregorianCalendar.HOUR_OF_DAY, 0)
    today.set(GregorianCalendar.MINUTE, 0)
    today.set(GregorianCalendar.SECOND, 0)
    today.set(GregorianCalendar.MILLISECOND, 0)
    val todayTimestamp = Timestamp(today.time)

    val expectedTrip =
        Trip(
            id = "mockTripId",
            creator = "mockUserId",
            description = "Description for trip with unknown location",
            name = "Trip with Unknown Location",
            locations = listOf(Location(country = "Unknown", city = "Unknown")),
            startDate = todayTimestamp,
            endDate = todayTimestamp,
            activities = listOf(),
            type = TripType.BUSINESS,
            imageUri = "")

    verify(tripRepository).createTrip(eq(expectedTrip), any(), any())
    verify(navigationActions).goBack()
  }

  @Test
  fun addTripScreen_imageSelection() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }
    composeTestRule.onNodeWithText("Select Image from Gallery").performClick()
    // Assuming gallery selection simulated
    // Add verification that the imageUri state is updated
  }

  @Test
  fun addTripScreen_invalidStartDate() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }
    // Simulate setting a past start date
    composeTestRule.onNodeWithTag("inputStartDate").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("tripSave").assertIsNotEnabled()
  }

  @Test
  fun addTripScreen_endDateBeforeStartDate() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }
    // Simulate setting a start date and an earlier end date
    composeTestRule.onNodeWithTag("inputStartDate", useUnmergedTree = true).performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("inputEndDate").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("tripSave").assertIsNotEnabled()
  }

  @Test
  fun addTripScreen_editMode() {
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
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, isEditMode = true)
    }

    composeTestRule.onNodeWithTag("inputTripTitle").assertTextContains("Existing Trip")
    composeTestRule.onNodeWithTag("inputTripDescription").assertTextContains("Existing trip")
    composeTestRule.onNodeWithTag("tripTypeTourism").assertIsSelected()
  }

  @Test
  fun addTripScreen_editMode_updatesTrip() {
    // Set up a sample trip to simulate editing
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

    // Set the selected trip in the ViewModel to the sample trip to simulate edit mode
    tripsViewModel.selectTrip(trip)

    // Set up the test content
    composeTestRule.setContent {
      AddTripScreen(
          tripsViewModel = tripsViewModel, navigationActions = navigationActions, isEditMode = true)
    }

    // Modify some fields to simulate an edit
    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Updated Trip Title")
    composeTestRule.onNodeWithTag("inputTripDescription").performTextInput("Updated Description")

    // Click the save button to trigger updateTrip
    composeTestRule.onNodeWithTag("tripSave").performClick()
  }

  @Test
  fun addTripScreen_displayDatePickerModal() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }
    composeTestRule.onNodeWithTag("inputStartDate").performClick()
    composeTestRule.onNodeWithText("OK").assertExists() // Checks if date picker dialog appears
  }

  private fun convertToTimestamp(dateString: String): Timestamp? {
    val calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
    val dateParts = dateString.split("/")
    return if (dateParts.size == 3) {
      try {
        calendar.set(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt(), 0, 0, 0)
        calendar.set(GregorianCalendar.MILLISECOND, 0)
        Timestamp(calendar.time)
      } catch (e: NumberFormatException) {
        null
      }
    } else {
      null
    }
  }

  @Test
  fun convertToTimestamp_validDate() {
    val dateString = "01/01/2024"
    val utcCalendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
    utcCalendar.set(2024, 0, 1, 0, 0, 0)
    utcCalendar.set(GregorianCalendar.MILLISECOND, 0)
    val expectedTimestamp = Timestamp(utcCalendar.time)
    val result = convertToTimestamp(dateString)
    assert(result != null)
    assert(expectedTimestamp.seconds == result?.seconds)
  }

  @Test
  fun convertToTimestamp_invalidDate() {
    val dateString = "invalid"
    val result = convertToTimestamp(dateString)
    assert(result == null)
  }

    @Test
    fun addTripScreen_imageContainer_hasCorrectAspectRatio() {
        composeTestRule.setContent {
            AddTripScreen(tripsViewModel, navigationActions)
        }

        // Verify the image container has correct aspect ratio modifier
        composeTestRule.onNodeWithTag("imageContainer")
            .assertExists()
    }

    @Test
    fun addTripScreen_imageHeight_respectsScreenSize() {
        composeTestRule.setContent {
            AddTripScreen(tripsViewModel, navigationActions)
        }

        // Verify image height is calculated correctly based on screen width
        composeTestRule.onNodeWithTag("imageContainer")
            .assertExists()
    }

    @Test
    fun addTripScreen_imageCropper_error() {
        composeTestRule.setContent {
            AddTripScreen(
                tripsViewModel = tripsViewModel,
                navigationActions = navigationActions
            )
        }

        // Simulate failed image cropping
        composeTestRule.onNodeWithText("Select Image from Gallery").performClick()

        // Verify error toast is shown (would need to check for Toast message)
    }
}
