package com.android.voyageur.ui.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class ImageCropperUtilTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()


    @Test
    fun imageCropper_initialization() {
        var resultReceived = false

        composeTestRule.setContent {
            val cropper = rememberImageCropper {
                resultReceived = true
            }
        }

        composeTestRule.runOnIdle {
            assert(!resultReceived)
        }
    }

    @Test
    fun imageCropper_customAspectRatio() {
        var cropperCalled = false

        composeTestRule.setContent {
            val cropper = rememberImageCropper(
                aspectRatioX = 4,
                aspectRatioY = 3
            ) {
                cropperCalled = true
            }
        }

        composeTestRule.runOnIdle {
            assert(!cropperCalled)
        }
    }
}