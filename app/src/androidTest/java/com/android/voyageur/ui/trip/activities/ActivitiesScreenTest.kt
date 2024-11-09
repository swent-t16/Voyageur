package com.android.voyageur.ui.trip.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

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
                      title = "Final Activity",
                      description = "This is a final activity",
                      startTime = Timestamp.now(),
                      endTime = Timestamp.now(),
                      estimatedPrice = 20.0,
                      activityType = ActivityType.RESTAURANT)))

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
    composeTestRule.setContent { ActivitiesScreen(sampleTrip, navigationActions) }

    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }

  @Test
  fun activitiesScreen_displaysDraftAndFinalSections() {
    val sampleTrip = Trip(name = "Sample Trip", activities = SAMPLE_ACTIVITIES)

    composeTestRule.setContent {
      ActivitiesScreen(trip = sampleTrip, navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithText("Drafts").assertIsDisplayed()
    composeTestRule.onNodeWithText("Final").assertIsDisplayed()
  }

  @Test
  fun activityItem_expandAndCollapse() {
    val sampleActivity = SAMPLE_ACTIVITIES.first()

    composeTestRule.setContent { ActivityItem(activity = sampleActivity) }

    composeTestRule.onNodeWithText("Price").assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag("expandIcon_${sampleActivity.title}")
        .assertIsDisplayed()
        .performClick()

    // Check if the expanded content is displayed
    composeTestRule.onNodeWithText("Price").assertIsDisplayed()
    composeTestRule.onNodeWithText("${sampleActivity.estimatedPrice} CHF").assertIsDisplayed()
  }

  @Test
  fun activityCard_displaysCorrectTitle() {
    val sampleActivity = SAMPLE_ACTIVITIES.first()

    composeTestRule.setContent { ActivityItem(activity = sampleActivity) }

    composeTestRule.onNodeWithTag("cardItem_${sampleActivity.title}").assertIsDisplayed()
    composeTestRule.onNodeWithText(sampleActivity.title).assertIsDisplayed()
  }
}
