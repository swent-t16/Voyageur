package com.android.voyageur.ui.overview

import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

class AddTripScreenTest {
  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripsViewModel = TripsViewModel(tripRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.ADD_TRIP)
    whenever(tripsViewModel.getNewTripId()).thenReturn("mockTripId")
  }

  private fun <T> anyNonNull(): T = Mockito.any<T>()

  @Test
  fun addTripScreen_initialState() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTripTitle").assertExists()
    composeTestRule.onNodeWithTag("inputTripDescription").assertExists()
    composeTestRule.onNodeWithTag("inputTripParticipants").assertExists()
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
    composeTestRule.onNodeWithTag("inputTripParticipants").performTextInput("Alice, Bob")

    composeTestRule.onNodeWithTag("inputTripTitle").assertExists()
    composeTestRule.onNodeWithTag("inputTripDescription").assertExists()
    composeTestRule.onNodeWithTag("inputTripParticipants").assertExists()
  }

  @Test
  fun addTripScreen_doesNotSubmitWithInvalidDate() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputStartDate").performTextInput("notadate")

    composeTestRule.onNodeWithTag("inputEndDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputEndDate").performTextInput("05/01/2024")

    composeTestRule.onNodeWithTag("tripSave").performClick()

    verify(tripRepository, never()).updateTrip(anyNonNull(), anyNonNull(), anyNonNull())
  }

  @Test
  fun addTripScreen_doesNotSubmitWithInvalidTitle() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputTripTitle").performTextClearance()
    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("")

    composeTestRule.onNodeWithTag("inputStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputStartDate").performTextInput("01/01/2024")

    composeTestRule.onNodeWithTag("inputEndDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputEndDate").performTextInput("05/01/2024")

    composeTestRule.onNodeWithTag("tripSave").performClick()

    verify(tripRepository, never()).updateTrip(anyNonNull(), anyNonNull(), anyNonNull())
  }

  @Test
  fun addTripScreen_tripTypeSelection() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("tripTypeBusiness").performClick()
    assert(tripsViewModel.tripType.value == TripType.BUSINESS)

    composeTestRule.onNodeWithTag("tripTypeTourism").performClick()
    assert(tripsViewModel.tripType.value == TripType.TOURISM)
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

    assertNotNull(result)
    assertEquals(expectedTimestamp.seconds, result?.seconds)
  }

  @Test
  fun convertToTimestamp_invalidDate() {
    val dateString = "invalid"
    val result = convertToTimestamp(dateString)

    assertNull(result)
  }
}
