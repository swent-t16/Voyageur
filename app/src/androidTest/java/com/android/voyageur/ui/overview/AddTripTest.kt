package com.android.voyageur.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
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
    composeTestRule.onNodeWithTag("inputTripCreator").assertExists()
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
    composeTestRule.onNodeWithTag("inputTripCreator").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("inputTripParticipants").performTextInput("Alice, Bob")

    composeTestRule.onNodeWithTag("inputTripTitle").assertExists()
    composeTestRule.onNodeWithTag("inputTripDescription").assertExists()
    composeTestRule.onNodeWithTag("inputTripCreator").assertExists()
    composeTestRule.onNodeWithTag("inputTripParticipants").assertExists()
  }

  @Test
  fun doesNotSubmitWithInvalidDate() {
    composeTestRule.setContent { AddTripScreen(tripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("inputStartDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputStartDate").performTextInput("notadate")

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
}
