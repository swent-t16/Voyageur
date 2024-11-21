package com.android.voyageur.ui.trip.assistant

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.extractActivitiesFromJson
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.trip.UiState
import com.android.voyageur.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AssistantScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var tripRepository: TripRepository
  private lateinit var mockTripsViewModel: TripsViewModel
  private lateinit var navHostController: NavHostController

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleTrip = Trip(name = "Sample Trip")
  private val sampleJson =
      "[{\"title\":\"Activity 1\", \"description\": \"Description 1\"}, {\"title\":\"Activity 2\", \"description\": \"Description 1\"}]"

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    tripsViewModel = TripsViewModel(tripRepository)
    mockTripsViewModel = mock(TripsViewModel::class.java)
  }

  @Test
  fun initialRenderingOfScreen() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { AssistantScreen(tripsViewModel, navigationActions) }
    composeTestRule.onNodeWithTag("assistantScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AIRequestTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AIRequestButton").assertIsDisplayed()
  }

  @Test
  fun loadingIndicatorDisplayedWhenLoading() {
    val uiStateFlow = MutableStateFlow(UiState.Loading)
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent { AssistantScreen(mockTripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
  }

  @Test
  fun errorTextDisplayedWhenError() {
    val errorMessage = "An error occurred"
    val uiStateFlow = MutableStateFlow(UiState.Error(errorMessage))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent { AssistantScreen(mockTripsViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("errorMessage").assertIsDisplayed()
  }

  @Test
  fun activitiesDisplayedWhenSuccess() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent { AssistantScreen(mockTripsViewModel, navigationActions) }
    composeTestRule.onNodeWithTag("cardItem_Activity 1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_Activity 2").assertIsDisplayed()
  }

  @Test
  fun activityRemovedWhenAddButtonClicked() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    val mockActivity = Activity(title = "Activity 1", description = "Description 1")
    doNothing().`when`(mockTripsViewModel).addActivityToTrip(mockActivity)
    composeTestRule.setContent { AssistantScreen(mockTripsViewModel, navigationActions) }
    // Act
    composeTestRule.onNodeWithTag("cardItem_Activity 1").assertIsDisplayed()
    // click on expand icon
    composeTestRule.onNodeWithTag("expandIcon_Activity 1").performClick()

    composeTestRule.onNodeWithTag("addIcon_Activity 1").performClick()
    verify(mockTripsViewModel).addActivityToTrip(mockActivity)

    composeTestRule.onNodeWithText("Activity 1").assertIsNotDisplayed()
  }

  @Test
  fun checkButtonIsDisabledWhenLoading() {
    val uiStateFlow = MutableStateFlow(UiState.Loading)
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent { AssistantScreen(mockTripsViewModel, navigationActions) }
    composeTestRule.onNodeWithTag("AIRequestButton").assertIsNotEnabled()
  }

  @Test
  fun checkJsonParsing() {
    val activities = extractActivitiesFromJson(sampleJson)
    assert(activities.size == 2)
    assert(activities[0].title == "Activity 1")
    assert(activities[0].description == "Description 1")
    assert(activities[1].title == "Activity 2")
    assert(activities[1].description == "Description 1")
  }
}
