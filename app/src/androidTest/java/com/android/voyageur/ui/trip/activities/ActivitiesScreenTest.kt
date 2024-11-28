package com.android.voyageur.ui.trip.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Timestamp {
  val calendar = java.util.Calendar.getInstance()
  calendar.set(year, month - 1, day, hour, minute) // Month is 0-based
  return Timestamp(calendar.time)
}

class ActivitiesScreenTest {
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
                      title = "Final Activity With Description",
                      description = "This is a final activity",
                      startTime = createTimestamp(2022, 1, 1, 12, 0),
                      endTime = createTimestamp(2022, 1, 1, 14, 0),
                      estimatedPrice = 20.0,
                      activityType = ActivityType.RESTAURANT),
                  Activity(
                      title = "Final Activity Without Description",
                      estimatedPrice = 10.0,
                      startTime = createTimestamp(2022, 1, 2, 12, 0),
                      endTime = createTimestamp(2022, 1, 2, 14, 0),
                      activityType = ActivityType.MUSEUM),
              ))

  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var tripRepository: TripRepository
  private lateinit var mockTripsViewModel: TripsViewModel

  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    tripRepository = mock(TripRepository::class.java)
    tripsViewModel = TripsViewModel(tripRepository)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    mockTripsViewModel = mock(TripsViewModel::class.java)
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, tripsViewModel)
    }
    composeTestRule.onNodeWithTag("activitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createActivityButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("totalEstimatedPriceBox").assertIsDisplayed()
  }

  @Test
  fun displaysBottomNavigationCorrectly() {
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, tripsViewModel)
    }

    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }

  @Test
  fun activitiesScreen_displaysDraftAndFinalSections() {
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions = navigationActions, userViewModel, tripsViewModel)
    }

    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithText("Drafts").assertIsDisplayed()
    composeTestRule.onNodeWithText("Final").assertIsDisplayed()
  }

  @Test
  fun clickingCreateActivityButton_navigatesToAddActivityScreen() {
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, tripsViewModel)
    }

    composeTestRule.onNodeWithTag("createActivityButton").performClick()

    verify(navigationActions).navigateTo(Screen.ADD_ACTIVITY)
  }

  @Test
  fun activitiesScreen_displaysActivityItems() {
    `when`(mockTripsViewModel.getActivitiesForSelectedTrip()).thenReturn(sampleTrip.activities)
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, mockTripsViewModel)
    }

    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[0].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[1].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[2].title}").assertIsDisplayed()
  }

  @Test
  fun clickingDeleteButton_displaysDeleteActivityAlertDialog() {
    `when`(mockTripsViewModel.getActivitiesForSelectedTrip()).thenReturn(sampleTrip.activities)
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, mockTripsViewModel)
    }

    composeTestRule.onNodeWithTag("expandIcon_${sampleTrip.activities[0].title}").performClick()
    composeTestRule.onNodeWithTag("deleteIcon_${sampleTrip.activities[0].title}").performClick()
    composeTestRule.onNodeWithTag("deleteActivityAlertDialog").assertIsDisplayed()
  }

  @Test
  fun clickingDeleteButton_removesActivityFromTrip() {
    `when`(mockTripsViewModel.getActivitiesForSelectedTrip()).thenReturn(sampleTrip.activities)

    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, mockTripsViewModel)
    }

    // test with draft activity
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[0].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expandIcon_${sampleTrip.activities[0].title}").performClick()
    composeTestRule.onNodeWithTag("deleteIcon_${sampleTrip.activities[0].title}").performClick()
    composeTestRule.onNodeWithTag("confirmDeleteButton").performClick()
    composeTestRule
        .onNodeWithTag("cardItem_${sampleTrip.activities[0].title}")
        .assertIsNotDisplayed()
    verify(mockTripsViewModel).removeActivityFromTrip(sampleTrip.activities[0])

    // test with final activity
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[1].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expandIcon_${sampleTrip.activities[1].title}").performClick()
    composeTestRule.onNodeWithTag("deleteIcon_${sampleTrip.activities[1].title}").performClick()
    composeTestRule.onNodeWithTag("confirmDeleteButton").performClick()
    composeTestRule
        .onNodeWithTag("cardItem_${sampleTrip.activities[1].title}")
        .assertIsNotDisplayed()
    verify(mockTripsViewModel).removeActivityFromTrip(sampleTrip.activities[1])
  }

  @Test
  fun clickingFilterButton_displaysFilterDialog() {
    `when`(mockTripsViewModel.getActivitiesForSelectedTrip()).thenReturn(sampleTrip.activities)
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, mockTripsViewModel)
    }
    // Click the filter button
    composeTestRule.onNodeWithTag("filterButton").performClick()
    composeTestRule.onNodeWithTag("filterActivityAlertDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("typeCheckBox_WALK").assertExists()
    composeTestRule.onNodeWithTag("confirmButtonDialog").performClick()
    composeTestRule.onNodeWithTag("filterActivityAlertDialog").assertDoesNotExist()
  }

  @Test
  fun selectingFilter_updatesDisplayedActivities() {
    `when`(mockTripsViewModel.getActivitiesForSelectedTrip()).thenReturn(sampleTrip.activities)
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, mockTripsViewModel)
    }

    composeTestRule.onNodeWithTag("filterButton").performClick()
    // Select the "WALK" filter
    composeTestRule.onNodeWithTag("typeCheckBox_WALK").performClick()
    composeTestRule
        .onNodeWithTag("typeCheckBox_WALK")
        .assertIsOn() // Verify the checkbox is checked
    composeTestRule.onNodeWithTag("confirmButtonDialog").performClick()

    // Verify that only WALK activities are displayed
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[0].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[1].title}").assertDoesNotExist()
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[2].title}").assertDoesNotExist()
  }

  @Test
  fun clearingFilter_displaysAllActivities() {
    `when`(mockTripsViewModel.getActivitiesForSelectedTrip()).thenReturn(sampleTrip.activities)
    composeTestRule.setContent {
      ActivitiesScreen(navigationActions, userViewModel, mockTripsViewModel)
    }

    composeTestRule.onNodeWithTag("filterButton").performClick()
    // Select the "RESTAURANT" filter using the updated test tag
    composeTestRule.onNodeWithTag("typeCheckBox_RESTAURANT").performClick()

    composeTestRule.onNodeWithTag("confirmButtonDialog").performClick()

    // Verify that only RESTAURANT activities are displayed
    composeTestRule.onNodeWithTag("cardItem_Final Activity With Description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_Draft Activity").assertDoesNotExist()
    composeTestRule
        .onNodeWithTag("cardItem_Final Activity Without Description")
        .assertDoesNotExist()

    // Open the filter dialog again
    composeTestRule.onNodeWithTag("filterButton").performClick()

    // Deselect the "RESTAURANT" filter using the updated test tag
    composeTestRule.onNodeWithTag("typeCheckBox_RESTAURANT").performClick()
    composeTestRule.onNodeWithTag("confirmButtonDialog").performClick()

    // Verify all activities are displayed
    composeTestRule.onNodeWithTag("cardItem_Draft Activity").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_Final Activity With Description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_Final Activity Without Description").assertIsDisplayed()
  }
}
