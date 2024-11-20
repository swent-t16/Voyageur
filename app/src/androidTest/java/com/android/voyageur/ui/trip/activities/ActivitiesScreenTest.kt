package com.android.voyageur.ui.trip.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

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

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    tripRepository = mock(TripRepository::class.java)
    tripsViewModel = TripsViewModel(tripRepository)
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent { ActivitiesScreen(sampleTrip, navigationActions, tripsViewModel) }
    composeTestRule.onNodeWithTag("activitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createActivityButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
  }

  @Test
  fun displaysBottomNavigationCorrectly() {
    composeTestRule.setContent { ActivitiesScreen(sampleTrip, navigationActions, tripsViewModel) }

    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }

  @Test
  fun activitiesScreen_displaysDraftAndFinalSections() {
    composeTestRule.setContent {
      ActivitiesScreen(trip = sampleTrip, navigationActions = navigationActions, tripsViewModel)
    }

    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithText("Drafts").assertIsDisplayed()
    composeTestRule.onNodeWithText("Final").assertIsDisplayed()
  }

  @Test
  fun clickingCreateActivityButton_navigatesToAddActivityScreen() {
    composeTestRule.setContent { ActivitiesScreen(sampleTrip, navigationActions, tripsViewModel) }

    composeTestRule.onNodeWithTag("createActivityButton").performClick()

    verify(navigationActions).navigateTo(Screen.ADD_ACTIVITY)
  }

  @Test
  fun activitiesScreen_displaysActivityItems() {
    composeTestRule.setContent { ActivitiesScreen(sampleTrip, navigationActions, tripsViewModel) }

    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[0].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[1].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_${sampleTrip.activities[2].title}").assertIsDisplayed()
  }
}
