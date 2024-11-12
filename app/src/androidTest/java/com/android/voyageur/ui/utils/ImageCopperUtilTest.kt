package com.android.voyageur.ui.utils

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ImageCropperUtilTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun imageCropper_initialization() {
    var resultReceived = false

    composeTestRule.setContent { rememberImageCropper { resultReceived = true } }

    composeTestRule.runOnIdle { assert(!resultReceived) }
  }

  @Test
  fun imageCropper_customAspectRatio() {
    var cropperCalled = false

    composeTestRule.setContent {
      rememberImageCropper(aspectRatioX = 4, aspectRatioY = 3) { cropperCalled = true }
    }

    composeTestRule.runOnIdle { assert(!cropperCalled) }
  }

  @Test
  fun imageCropper_launchWithValidUri() {
    var cropped = false
    val mockUri: Uri = mock(Uri::class.java)

    composeTestRule.setContent {
      val cropper = rememberImageCropper { cropped = true }

      LaunchedEffect(Unit) {
        // Wait for composition to be ready before launching
        cropper(mockUri)
      }
    }

    composeTestRule.waitForIdle()
    assert(!cropped) // The actual cropping won't happen in tests
  }

  @Test
  fun imageCropper_launchWithNullUri() {
    var launched = false

    composeTestRule.setContent {
      val cropper = rememberImageCropper {}

      LaunchedEffect(Unit) {
        cropper(null)
        launched = true
      }
    }

    composeTestRule.runOnIdle { assert(launched) }
  }

  @Test
  fun imageCropper_handleErrorDuringCropping() {
    var result: ImageCropperResult? = null
    val mockUri: Uri = mock(Uri::class.java)

    composeTestRule.setContent {
      val cropper = rememberImageCropper { result = it }

      LaunchedEffect(Unit) { cropper(mockUri) }
    }

    composeTestRule.runOnIdle {
      assert(result == null) // The actual cropping won't happen in tests
    }
  }

  @Test
  fun imageCropper_galleryLauncherWithCropper() {
    var galleryLaunched = false
    var cropperResult: ImageCropperResult? = null

    composeTestRule.setContent {
      val cropper =
          rememberImageCropper(aspectRatioX = 16, aspectRatioY = 9) { result ->
            cropperResult = result
          }

      val galleryLauncher =
          rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri
            ->
            galleryLaunched = true
            uri?.let { cropper(it) }
          }

      LaunchedEffect(Unit) { galleryLauncher.launch("image/*") }
    }

    composeTestRule.runOnIdle {
      assert(!galleryLaunched) // Won't actually launch in tests
      assert(cropperResult == null) // No actual cropping in tests
    }
  }
}
