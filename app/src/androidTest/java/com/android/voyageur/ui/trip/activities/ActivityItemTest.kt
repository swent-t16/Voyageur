package com.android.voyageur.ui.trip.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ActivityItemTest {
  private val sampleActivityWithoutDescription =
      Activity(
          title = "Final Activity Without Description",
          estimatedPrice = 10.0,
          startTime = createTimestamp(2022, 1, 2, 12, 0),
          endTime = createTimestamp(2022, 1, 2, 14, 0),
          activityType = ActivityType.MUSEUM)
  private val sampleActivityWithDescription =
      Activity(
          title = "Final Activity With Description",
          description = "This is a final activity",
          startTime = createTimestamp(2022, 1, 1, 12, 0),
          endTime = createTimestamp(2022, 1, 1, 14, 0),
          estimatedPrice = 20.0,
          activityType = ActivityType.RESTAURANT)

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripsViewModel = TripsViewModel(tripRepository)
  }

  @Test
  fun activityItem_expandAndCollapse() {

    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }

    composeTestRule.onNodeWithText("Price").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Type").assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
        .performClick()

    // Check if the expanded content is displayed
    composeTestRule.onNodeWithText("Price").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("${sampleActivityWithDescription.estimatedPrice} CHF")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Type").assertIsDisplayed()
  }

  @Test
  fun activityCard_displaysCorrectTitle() {
    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }

    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText(sampleActivityWithDescription.title).assertIsDisplayed()
  }

  @Test
  fun activityCard_displaysCorrectDescription() {

    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }
    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText(sampleActivityWithDescription.description).assertIsDisplayed()
  }

  @Test
  fun activityCard_doesNotDisplayDescription() {

    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithoutDescription,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }
    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivityWithoutDescription.title}")
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithoutDescription.title}")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText(sampleActivityWithoutDescription.description)
        .assertIsNotDisplayed()
  }

  @Test
  fun activityCard_displaysCorrectDateAndTime() {

    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }

    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("01/01/2022 12:00 PM - 02:00 PM").assertIsDisplayed()
  }

  @Test
  fun activityCard_displaysDeleteButton() {
    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          buttonPurpose = ButtonType.DELETE,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }
    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivityWithDescription.title}")
        .performClick()
    composeTestRule
        .onNodeWithTag("deleteIcon_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
  }

  @Test
  fun activityCard_displaysAddButton() {
    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          buttonPurpose = ButtonType.ADD,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }
    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivityWithDescription.title}")
        .performClick()
    composeTestRule
        .onNodeWithTag("addIcon_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
  }

  @Test
  fun activityCard_displaysEditButton() {
    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          isEditable = true,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }
    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivityWithDescription.title}")
        .performClick()
    composeTestRule
        .onNodeWithTag("editIcon_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
  }

  @Test
  fun activityCard_doesNotDisplayEditButton() {
    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          isEditable = false,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }
    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivityWithDescription.title}")
        .performClick()
    composeTestRule
        .onNodeWithTag("editIcon_${sampleActivityWithDescription.title}")
        .assertIsNotDisplayed()
  }

  @Test
  fun clickingOnEditButton_navigatesToEditActivityScreen() {
    doNothing().`when`(navigationActions).navigateTo(Screen.EDIT_ACTIVITY)
    composeTestRule.setContent {
      ActivityItem(
          activity = sampleActivityWithDescription,
          isEditable = true,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel)
    }
    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivityWithDescription.title}")
        .performClick()
    composeTestRule.onNodeWithTag("editIcon_${sampleActivityWithDescription.title}").performClick()
    verify(navigationActions).navigateTo(Screen.EDIT_ACTIVITY)
    assert(tripsViewModel.selectedActivity.value == sampleActivityWithDescription)
  }
    @Test
    fun activityCard_displaysCorrectLocationName() {
        // Create a sample activity with a location name
       val sampleActivityWithLocation = Activity(
            title = "Final Activity Without Description",
            estimatedPrice = 10.0,
            startTime = createTimestamp(2022, 1, 2, 12, 0),
            endTime = createTimestamp(2022, 1, 2, 14, 0),
            location = Location(name = "EPFL Lausanne"),
            activityType = ActivityType.MUSEUM)

        composeTestRule.setContent {
            ActivityItem(
                activity = sampleActivityWithLocation,
                navigationActions = navigationActions,
                tripsViewModel = tripsViewModel
            )
        }
        // Expand the activity card
        composeTestRule
            .onNodeWithTag("expandIcon_${sampleActivityWithLocation.title}")
            .assertIsDisplayed()
            .performClick()

        // Check if the location section is displayed
        composeTestRule.onNodeWithText("Location").assertIsDisplayed()
        composeTestRule.onNodeWithText("EPFL Lausanne").assertIsDisplayed()
    }
}
