package com.android.voyageur.ui.trip.assistant

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.trip.UiState
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class AssistantScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var tripRepository: TripRepository
  private lateinit var mockTripsViewModel: TripsViewModel
  private lateinit var navHostController: NavHostController
  private lateinit var userViewModel: UserViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleTrip = Trip(name = "Sample Trip", participants = listOf("user123", "contact1"))
  private val sampleJson =
      "[{\"title\":\"Activity 1\", \"description\": \"Description 1\", \"year\": 2022, \"month\": 1, \"day\": 1, \"startTimeHour\": 12, \"startTimeMinute\": 0, \"endTimeHour\": 14, \"endTimeMinute\": 0}" +
          ",{\"title\":\"Activity 2\", \"description\": \"Description 2\", \"year\": 2022, \"month\": 1, \"day\": 2, \"startTimeHour\": 12, \"startTimeMinute\": 0, \"endTimeHour\": 14, \"endTimeMinute\": 0}]"
  private val mockUser =
      User(
          id = "user123",
          name = "John Doe",
          email = "john.doe@example.com",
          profilePicture = "https://example.com/profile.jpg",
          bio = "Traveler",
          username = "johndoe",
          contacts = listOf(),
          interests = listOf("hiking", "cycling"))
  private lateinit var tripInviteRepository: TripInviteRepository

  private val mockContacts =
      listOf(
          User(id = "contact1", name = "Contact 1", interests = listOf("hiking", "art")),
          User(id = "contact2", name = "Contact 2", interests = listOf("restaurants", "music")),
          mockUser)

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    tripInviteRepository = mock(TripInviteRepository::class.java)
    tripsViewModel = TripsViewModel(tripRepository, tripInviteRepository)
    mockTripsViewModel = mock(TripsViewModel::class.java)
    userViewModel = mock(UserViewModel::class.java)

    // We need this because userViewModel.user is not an open property
    val userField = UserViewModel::class.java.getDeclaredField("user")
    userField.isAccessible = true
    userField.set(userViewModel, MutableStateFlow(mockUser))

    val contactsField = UserViewModel::class.java.getDeclaredField("contacts")
    contactsField.isAccessible = true
    contactsField.set(userViewModel, MutableStateFlow(mockContacts))
  }

  @Test
  fun initialRenderingOfScreen() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent { AssistantScreen(tripsViewModel, navigationActions, userViewModel) }
    composeTestRule.onNodeWithTag("assistantScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AIRequestTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AIRequestButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("initialStateText").assertIsDisplayed()
  }

  @Test
  fun loadingIndicatorDisplayedWhenLoading() {
    val uiStateFlow = MutableStateFlow(UiState.Loading)
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }

    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
  }

  @Test
  fun errorTextDisplayedWhenError() {
    val errorMessage = "An error occurred"
    val uiStateFlow = MutableStateFlow(UiState.Error(errorMessage))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }

    composeTestRule.onNodeWithTag("errorMessage").assertIsDisplayed()
  }

  @Test
  fun activitiesDisplayedWhenSuccess() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }
    composeTestRule.onNodeWithTag("cardItem_Activity 1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_Activity 2").assertIsDisplayed()
  }

  @Test
  fun emptyActivitiesPromptDisplayedWhenAllActivitiesAdded() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    val mockActivity1 = Activity(title = "Activity 1", description = "Description 1")
    val mockActivity2 = Activity(title = "Activity 2", description = "Description 2")
    doNothing().`when`(mockTripsViewModel).addActivityToTrip(mockActivity1)
    doNothing().`when`(mockTripsViewModel).addActivityToTrip(mockActivity2)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }
    composeTestRule.onNodeWithTag("expandIcon_Activity 1").performClick()
    composeTestRule.onNodeWithTag("addIcon_Activity 1").performClick()
    composeTestRule.onNodeWithTag("addIcon_Activity 2").performClick()
    composeTestRule.onNodeWithTag("emptyActivitiesPrompt").assertIsDisplayed()
  }

  @Test
  fun activityRemovedWhenAddButtonClicked() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    val mockActivity = Activity(title = "Activity 1", description = "Description 1")
    doNothing().`when`(mockTripsViewModel).addActivityToTrip(mockActivity)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }
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
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }
    composeTestRule.onNodeWithTag("AIRequestButton").assertIsNotEnabled()
  }

  @Test
  fun checkSettingsDialogIsDisplayedWhenSettingsButtonClicked() {
    val uiStateFlow = MutableStateFlow(UiState.Loading)
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }
    composeTestRule.onNodeWithTag("settingsButton").performClick()
    composeTestRule.onNodeWithTag("settingsDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("provideFinalActivitiesSwitch").assertIsDisplayed()
    composeTestRule.onNodeWithTag("provideFinalActivitiesSwitch").assertIsOff()
    composeTestRule.onNodeWithTag("useUserInterestsSwitch").assertIsDisplayed()
    composeTestRule.onNodeWithTag("useUserInterestsSwitch").assertIsOff()
    composeTestRule.onNodeWithTag("closeDialogButton").assertIsDisplayed()
  }

  @Test
  fun checkDraftActivitiesDisplayedInitially() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }
    composeTestRule.onNodeWithTag("cardItem_Activity 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("2022/01/01 12:00 PM - 02:00 PM").assertIsNotDisplayed()
  }

  @Test
  fun checkFinalActivitiesDisplayedWhenSwitched() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }
    composeTestRule.onNodeWithTag("settingsButton").performClick()
    composeTestRule.onNodeWithTag("provideFinalActivitiesSwitch").performClick()
    composeTestRule.onNodeWithTag("closeDialogButton").performClick()
    composeTestRule.onNodeWithText("01/01/2022 12:00 PM - 02:00 PM").assertIsDisplayed()
    // check the call to sendActivitiesPrompt happens with provideFinalActivities = true
    composeTestRule.onNodeWithTag("AIRequestButton").performClick()
    verify(mockTripsViewModel)
        .sendActivitiesPrompt(
            any(), eq(sampleTrip), eq(""), eq(emptyList()), provideFinalActivities = eq(true))
  }

  @Test
  fun checkInterestsUsedWhenSwitched() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }

    composeTestRule.onNodeWithTag("settingsButton").performClick()
    composeTestRule.onNodeWithTag("useUserInterestsSwitch").performClick()
    composeTestRule.onNodeWithTag("closeDialogButton").performClick()
    // check the call to sendActivitiesPrompt happens with useUserInterests = true
    composeTestRule.onNodeWithTag("AIRequestButton").performClick()
    verify(mockTripsViewModel)
        .sendActivitiesPrompt(
            any(),
            eq(sampleTrip),
            eq(""),
            eq(listOf("hiking", "cycling")),
            provideFinalActivities = eq(false))
  }

  @Test
  fun checkAllParticipantsInterestsUsedWhenSwitched() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }

    composeTestRule.onNodeWithTag("settingsButton").performClick()
    composeTestRule.onNodeWithTag("useParticipantsInterestsSwitch").performClick()
    composeTestRule.onNodeWithTag("closeDialogButton").performClick()
    // check the call to sendActivitiesPrompt happens with useUserInterests = true
    composeTestRule.onNodeWithTag("AIRequestButton").performClick()
    verify(mockTripsViewModel)
        .sendActivitiesPrompt(
            any(),
            eq(sampleTrip),
            eq(""),
            eq(listOf("hiking", "cycling", "art")),
            provideFinalActivities = eq(false))
  }

  @Test
  fun checkInterestsSwitchesAlternate() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)
    composeTestRule.setContent {
      AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
    }
    composeTestRule.onNodeWithTag("settingsButton").performClick()
    composeTestRule.onNodeWithTag("useUserInterestsSwitch").performClick()
    composeTestRule.onNodeWithTag("useParticipantsInterestsSwitch").assertIsOff()
    composeTestRule.onNodeWithTag("useUserInterestsSwitch").assertIsOn()
    composeTestRule.onNodeWithTag("useParticipantsInterestsSwitch").performClick()
    composeTestRule.onNodeWithTag("useUserInterestsSwitch").assertIsOff()
    composeTestRule.onNodeWithTag("useParticipantsInterestsSwitch").assertIsOn()
  }

  @Test
  fun assistantScreen_doneAction_preventsPromptSubmissionWhenLoading() {
    val uiStateFlow = MutableStateFlow(UiState.Loading)
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)

    val keyboardController = mock(SoftwareKeyboardController::class.java)

    composeTestRule.setContent {
      CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboardController) {
        AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
      }
    }
    // Simulate entering a prompt
    val inputPrompt = "Test prompt"
    composeTestRule.onNodeWithTag("AIRequestTextField").performTextInput(inputPrompt)

    // Simulate the Done action
    composeTestRule.onNodeWithTag("AIRequestTextField").performImeAction()

    verify(keyboardController).hide()
    verify(mockTripsViewModel, times(0)).sendActivitiesPrompt(any(), any(), any(), any(), any())
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun pressingEnterKey_submitsPromptWhenNotLoading() {
    val uiStateFlow = MutableStateFlow(UiState.Success(sampleJson))
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)

    val keyboardController = mock(SoftwareKeyboardController::class.java)

    composeTestRule.setContent {
      CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboardController) {
        AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
      }
    }
    // Simulate entering a prompt
    val inputPrompt = "Test prompt"
    composeTestRule.onNodeWithTag("AIRequestTextField").performTextInput(inputPrompt)

    // Find the TextField and simulate the Enter key event
    composeTestRule.onNodeWithTag("AIRequestTextField").performKeyInput { pressKey(Key.Enter) }

    verify(keyboardController).hide()
    verify(mockTripsViewModel).sendActivitiesPrompt(any(), any(), eq(inputPrompt), any(), any())
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun pressingEnterKey_doesNotSubmitPromptWhenLoading() {
    val uiStateFlow = MutableStateFlow(UiState.Loading)
    val tripFlow = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripFlow)
    `when`(mockTripsViewModel.uiState).thenReturn(uiStateFlow)

    val keyboardController = mock(SoftwareKeyboardController::class.java)

    composeTestRule.setContent {
      CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboardController) {
        AssistantScreen(mockTripsViewModel, navigationActions, userViewModel)
      }
    }
    // Simulate entering a prompt
    val inputPrompt = "Test prompt"
    composeTestRule.onNodeWithTag("AIRequestTextField").performTextInput(inputPrompt)

    // Find the TextField and simulate the Enter key event
    composeTestRule.onNodeWithTag("AIRequestTextField").performKeyInput { pressKey(Key.Enter) }

    verify(keyboardController).hide()
    verify(mockTripsViewModel, times(0))
        .sendActivitiesPrompt(any(), any(), eq(inputPrompt), any(), any())
  }
}
