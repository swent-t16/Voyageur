package com.android.voyageur.ui.trip

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import io.mockk.*
import java.util.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class AddActivityScreenTest {

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var context: Context

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripsViewModel = TripsViewModel(tripRepository)
    context = ApplicationProvider.getApplicationContext()

    `when`(navigationActions.currentRoute()).thenReturn(Screen.ADD_ACTIVITY)

    mockkStatic(Toast::class)
    every { Toast.makeText(any(), any<String>(), any()) } returns mockk()
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

  //    @Test
  //    fun addActivityScreen_validActivityCreatedAndNavigateBack() {
  //        composeTestRule.setContent {
  //            AddActivityScreen(tripsViewModel, navigationActions)
  //        }
  //
  //        composeTestRule.onNodeWithTag("inputActivityTitle").performTextInput("Hiking")
  //        composeTestRule.onNodeWithTag("inputDate").performClick()
  //        composeTestRule.onNodeWithText("OK").performClick()
  //        composeTestRule.onNodeWithTag("activitySave").performClick()
  //
  //        Mockito.verify(tripRepository).updateTrip(any(), any(), any())
  //        Mockito.verify(navigationActions).goBack()
  //    }
}
