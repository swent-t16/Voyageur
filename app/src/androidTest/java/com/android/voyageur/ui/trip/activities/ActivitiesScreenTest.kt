package com.android.voyageur.ui.trip.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ActivitiesScreenTest {
  private val sampleTrip = Trip(name = "Sample Trip", activities = listOf(
    // Draft activity
    Activity(
      title = "Draft Activity",
      description = "This is a draft activity",
      estimatedPrice = 0.0,
      activityType = ActivityType.WALK
    ),
    // Finalized activity
    Activity(
      title = "Final Activity",
      description = "This is a final activity",
      startTime = Timestamp.now(),
      endDate = Timestamp.now(),
      estimatedPrice = 20.0,
      activityType = ActivityType.RESTAURANT
    )))

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent { ActivitiesScreen(sampleTrip, navigationActions) }
    composeTestRule.onNodeWithTag("activitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }


  @Test
  fun displaysBottomNavigationCorrectly() {
    //    tripsViewModel.selectTrip(Trip(name = "Sample Trip"))
    composeTestRule.setContent { ActivitiesScreen(sampleTrip, navigationActions) }

    // Check that the bottom navigation menu is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    // Verify that the bottom navigation has items with correct actions
    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }
  @Test
  fun activitiesScreen_displaysDraftAndFinalSections() {
    // Arrange: Create a sample Trip with activities
    val sampleTrip = Trip(
      name = "Sample Trip",
      activities = finalActivities
    )
//    val navigationActions = NavigationActions {}

    // Act: Launch the ActivitiesScreen
    composeTestRule.setContent {
      ActivitiesScreen(trip = sampleTrip, navigationActions = navigationActions)
    }

    // Assert: Check if "Drafts" and "Final" headers are displayed
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithText("Drafts").assertIsDisplayed()
    composeTestRule.onNodeWithText("Final").assertIsDisplayed()
  }

  @Test
  fun activityItem_expandAndCollapse() {
    // Arrange: Create a sample activity
    val sampleActivity = finalActivities.first()

    // Act: Launch the ActivityItem
    composeTestRule.setContent {
      ActivityItem(activity = sampleActivity)
    }

    // Assert: Check if the expand icon is present and clickable
    composeTestRule.onNodeWithTag("expandIcon_${sampleActivity.title}")
      .assertIsDisplayed()
      .performClick()

    // Check if the expanded content is displayed
    composeTestRule.onNodeWithText("Price").assertIsDisplayed()
    composeTestRule.onNodeWithText("${sampleActivity.estimatedPrice} CHF").assertIsDisplayed()
  }

  @Test
  fun activityCard_displaysCorrectTitleAndTime() {
    // Arrange: Use the first activity in finalActivities
    val sampleActivity = finalActivities.first()

    // Act: Launch the ActivityItem
    composeTestRule.setContent {
      ActivityItem(activity = sampleActivity)
    }

    // Assert: Check if the activity title and time are displayed
    composeTestRule.onNodeWithTag("cardItem_${sampleActivity.title}").assertIsDisplayed()
    composeTestRule.onNodeWithText(sampleActivity.title).assertIsDisplayed()
  }
}
