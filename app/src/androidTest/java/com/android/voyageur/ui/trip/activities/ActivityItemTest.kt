package com.android.voyageur.ui.trip.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import org.junit.Rule
import org.junit.Test

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

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun activityItem_expandAndCollapse() {

    composeTestRule.setContent { ActivityItem(sampleActivityWithDescription) }

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
    composeTestRule.setContent { ActivityItem(sampleActivityWithDescription) }

    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText(sampleActivityWithDescription.title).assertIsDisplayed()
  }

  @Test
  fun activityCard_displaysCorrectDescription() {

    composeTestRule.setContent { ActivityItem(sampleActivityWithDescription) }
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

    composeTestRule.setContent { ActivityItem(sampleActivityWithoutDescription) }
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

    composeTestRule.setContent { ActivityItem(sampleActivityWithDescription) }

    composeTestRule
        .onNodeWithTag("cardItem_${sampleActivityWithDescription.title}")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("01/01/2022 12:00 PM - 02:00 PM").assertIsDisplayed()
  }
}
