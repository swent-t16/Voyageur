package com.android.voyageur.ui.profile.interests

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class InterestChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun interestChip_displaysInterestText() {
        // Arrange
        val interest = "Photography"

        // Act
        composeTestRule.setContent {
            InterestChip(interest = interest)
        }

        // Assert
        composeTestRule.onNodeWithText(interest).assertIsDisplayed()
    }

    @Test
    fun interestChip_hasCorrectTestTag() {
        // Arrange
        val interest = "Travel"

        // Act
        composeTestRule.setContent {
            InterestChip(interest = interest)
        }

        // Assert
        composeTestRule.onNodeWithTag("interest_$interest").assertIsDisplayed()
    }

    @Test
    fun interestChip_appliesModifiers() {
        // Arrange
        val interest = "Cooking"
        val customTag = "customTestTag"

        // Act
        composeTestRule.setContent {
            InterestChip(
                interest = interest,
                modifier = Modifier.testTag(customTag)
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(customTag).assertIsDisplayed()
    }
}
