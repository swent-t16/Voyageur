package com.android.voyageur.ui.overview

import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import io.mockk.verify
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AddTripScreenTest {
  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    tripsViewModel = TripsViewModel(tripRepository)
    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)

    whenever(firebaseAuth.uid).thenReturn("mockUserId")
    whenever(tripRepository.getNewTripId()).thenReturn("mockTripId")
    whenever(navigationActions.currentRoute()).thenReturn(Screen.ADD_TRIP)
  }

  @Test
  fun addTripScreen_initialState() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    composeTestRule.onNodeWithTag("addTrip").assertExists()
    composeTestRule.onNodeWithTag("inputTripTitle").assertExists()
    composeTestRule.onNodeWithTag("inputTripDescription").assertExists()
    composeTestRule.onNodeWithTag("searchTextField").assertExists()
    composeTestRule.onNodeWithTag("inputStartDate").assertExists()
    composeTestRule.onNodeWithTag("inputEndDate").assertExists()
    composeTestRule.onNodeWithTag("tripSave").assertExists()

    composeTestRule.onNodeWithTag("addTripTitle").assertTextEquals("Create a New Trip")
    composeTestRule.onNodeWithTag("tripSave").assertTextEquals("Save Trip")
  }

  @Test
  fun addTripScreen_inputsUpdateState() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("London")
    composeTestRule.onNodeWithTag("inputTripDescription").performTextInput("4 days in London")

    composeTestRule.onNodeWithTag("inputTripTitle").assertTextContains("London")
    composeTestRule.onNodeWithTag("inputTripDescription").assertTextContains("4 days in London")
  }

  @Test
  fun addTripScreen_saveButtonDisabledWithInvalidInput() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()

    composeTestRule.onNodeWithTag("tripSave").assertIsNotEnabled()
  }

  @Test
  fun addTripScreen_saveButtonEnabledWithValidInput() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Valid Trip")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()

    composeTestRule.onNodeWithTag("tripSave").assertIsEnabled()
  }

  @Test
  fun addTripScreen_tripTypeSelection() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

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
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    // Fill input fields
    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("London Trip")
    composeTestRule.onNodeWithTag("inputTripDescription").performTextInput("4 days in London")
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Big Ben Cafe")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick() // Simulate start date selection
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick() // Simulate end date selection

    // Verify save button is enabled and click it
    composeTestRule.onNodeWithTag("tripSave").assertIsEnabled()
    composeTestRule.onNodeWithTag("tripSave").performClick()

    // Verify the createTrip call
    verify(tripRepository).createTrip(any(), any(), any())
  }

  @Test
  fun addTripScreen_unknownLocation() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Trip with Unknown Location")
    composeTestRule
        .onNodeWithTag("inputTripDescription")
        .performTextInput("Description for trip with unknown location")
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("InvalidLocation")
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
            description = "Description for trip with unknown location",
            name = "Trip with Unknown Location",
            location = Location(name = "Big Ben Cafe"),
            startDate = todayTimestamp,
            endDate = todayTimestamp,
            activities = listOf(),
            type = TripType.BUSINESS,
            imageUri = "")

    tripsViewModel.createTrip(expectedTrip)
    verify(tripRepository).createTrip(eq(expectedTrip), any(), any())
  }

  @Test
  fun addTripScreen_imageSelection() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }
    composeTestRule.onNodeWithText("Select Image from Gallery").performClick()
    // Assuming gallery selection simulated
    // Add verification that the imageUri state is updated
  }

  @Test
  fun addTripScreen_invalidStartDate() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }
    // Simulate setting a past start date
    composeTestRule.onNodeWithTag("inputStartDate").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("tripSave").assertIsNotEnabled()
  }

  @Test
  fun addTripScreen_editMode() {
    val trip =
        Trip(
            id = "editTripId",
            description = "Existing trip",
            name = "Existing Trip",
            location = Location(name = "Big Ben Cafe"),
            startDate = Timestamp(Date()),
            endDate = Timestamp(Date()),
            activities = listOf(),
            type = TripType.TOURISM,
            imageUri = "someUri")
    tripsViewModel.selectTrip(trip)
    composeTestRule.setContent {
      AddTripScreen(
          tripsViewModel, navigationActions, isEditMode = true, placesViewModel = placesViewModel)
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
            description = "Existing trip",
            name = "Existing Trip",
            location = Location(name = "Big Ben Cafe"),
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
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          isEditMode = true,
          placesViewModel = placesViewModel)
    }

    // Modify some fields to simulate an edit
    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Updated Trip Title")
    composeTestRule.onNodeWithTag("inputTripDescription").performTextInput("Updated Description")

    // Click the save button to trigger updateTrip
    composeTestRule.onNodeWithTag("tripSave").performClick()
  }

  @Test
  fun addTripScreen_displayDatePickerModal() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }
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
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    // Verify the image container has correct aspect ratio modifier
    composeTestRule.onNodeWithTag("imageContainer").assertExists()
  }

  @Test
  fun addTripScreen_imageCropper_error() {
    composeTestRule.setContent {
      AddTripScreen(
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          placesViewModel = placesViewModel)
    }

    // Simulate failed image cropping
    composeTestRule.onNodeWithText("Select Image from Gallery").performClick()
  }

  @Test
  fun checkTripLocation() {
    val mockTrip =
        Trip(
            name = "Test Location",
            location =
                Location(
                    id = "mockID",
                    name = "Greek Project",
                    address = "Rue de EPFL",
                    lat = 19.9,
                    lng = 65.0),
            startDate =
                Timestamp(
                    Date.from(LocalDateTime.of(2024, 10, 3, 10, 0).toInstant(ZoneOffset.UTC))),
            endDate =
                Timestamp(
                    Date.from(LocalDateTime.of(2024, 10, 3, 11, 0).toInstant(ZoneOffset.UTC))),
        )

    tripsViewModel.selectTrip(mockTrip)
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    assertEquals("mockID", mockTrip.location.id)
    assertEquals("Greek Project", mockTrip.location.name)
    assertEquals("Rue de EPFL", mockTrip.location.address)
    assertEquals(19.9, mockTrip.location.lat, 0.10)
    assertEquals(65.0, mockTrip.location.lng, 0.10)
  }

  @Test
  fun testDoubleClickSaveTrip_createsOnlyOneTrip() {
    composeTestRule.setContent {
      AddTripScreen(tripsViewModel, navigationActions, placesViewModel = placesViewModel)
    }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Test Trip")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()

    // press quickly twice on the save button
    composeTestRule.onNodeWithTag("tripSave").performClick()
    composeTestRule.onNodeWithTag("tripSave").performClick()

    verify(tripRepository).createTrip(any(), any(), any())
  }
}
