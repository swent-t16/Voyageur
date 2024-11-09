package com.android.voyageur.ui.profile.interests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class InterestChipEditableTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun interestChipEditable_displaysInterestText() {
    // Arrange
    val interest = "Photography"

    // Act
    composeTestRule.setContent { InterestChipEditable(interest = interest, onRemove = {}) }

    // Assert
    composeTestRule.onNodeWithText(interest).assertIsDisplayed()
  }

  @Test
  fun interestChipEditable_displaysRemoveIcon() {
    // Arrange
    val interest = "Travel"

    // Act
    composeTestRule.setContent { InterestChipEditable(interest = interest, onRemove = {}) }

    // Assert
    composeTestRule.onNodeWithTag("removeInterestButton_$interest").assertIsDisplayed()
  }

  @Test
  fun interestChipEditable_showsConfirmationDialog_whenRemoveIconClicked() {
    // Arrange
    val interest = "Cooking"

    // Act
    composeTestRule.setContent { InterestChipEditable(interest = interest, onRemove = {}) }

    // Click on the remove icon
    composeTestRule.onNodeWithTag("removeInterestButton_$interest").performClick()

    // Assert
    composeTestRule.onNodeWithText("Remove Interest").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Are you sure you want to remove \"$interest\" from your interests?")
        .assertIsDisplayed()
  }

  @Test
  fun interestChipEditable_callsOnRemove_whenConfirmed() {
    // Arrange
    val interest = "Hiking"
    val onRemoveMock = mock(Runnable::class.java) // Using Runnable as a simple interface to mock

    // Act
    composeTestRule.setContent {
      InterestChipEditable(interest = interest, onRemove = { onRemoveMock.run() })
    }

    // Click on the remove icon
    composeTestRule.onNodeWithTag("removeInterestButton_$interest").performClick()

    // Confirm deletion
    composeTestRule.onNodeWithText("Remove").performClick()

    // Assert
    verify(onRemoveMock).run()
  }

  @Test
  fun interestChipEditable_doesNotCallOnRemove_whenCancelled() {
    // Arrange
    val interest = "Reading"
    val onRemoveMock = mock(Runnable::class.java)

    // Act
    composeTestRule.setContent {
      InterestChipEditable(interest = interest, onRemove = { onRemoveMock.run() })
    }

    // Click on the remove icon
    composeTestRule.onNodeWithTag("removeInterestButton_$interest").performClick()

    // Cancel deletion
    composeTestRule.onNodeWithText("Cancel").performClick()

    // Assert
    verify(onRemoveMock, never()).run()
  }
}
