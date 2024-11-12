package com.android.voyageur.ui.gallery

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.core.content.ContextCompat
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PermissionButtonForGalleryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun buttonIsDisplayedWithCorrectText() {
        val message = "Open Gallery"
        composeTestRule.setContent {
            PermissionButtonForGallery(
                onUriSelected = {},
                messageToShow = message,
                dialogMessage = "Permission is required to access the gallery."
            )
        }
        // Check that the button with the correct text is displayed
        composeTestRule.onNodeWithText(message).assertExists().assertIsDisplayed()
    }
    @Test
    fun checkPermissionReturnsTrue() {
      // Mock context
      val context = mock(Context::class.java)

      // Mock the permission check to return PERMISSION_GRANTED
      `when`(ContextCompat.checkSelfPermission(context, "android.permission.READ_MEDIA_IMAGES"))
          .thenReturn(PERMISSION_GRANTED)

      val result = checkFullPermission(context)
      assertTrue(result) // Expecting true because permission is granted
    }

    @Test
    fun checkPermissionReturnsTrueLimitedPermission() {
      // Mock context
      val context = mock(Context::class.java)

      // Mock the permission check to return PERMISSION_GRANTED
      `when`(
              ContextCompat.checkSelfPermission(
                  context, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"))
          .thenReturn(PERMISSION_GRANTED)

      val result = checkLimitedPermission(context)
      assertTrue(result) // Expecting true because permission is granted
    }

    @Test
    fun checkPermissionReturnsFalse() {
      // Mock context
      val context = mock(Context::class.java)

      // Mock the permission check to return PERMISSION_DENIED
      `when`(ContextCompat.checkSelfPermission(context, "android.permission.READ_MEDIA_IMAGES"))
          .thenReturn(PERMISSION_DENIED)
      `when`(
              ContextCompat.checkSelfPermission(
                  context, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"))
          .thenReturn(PERMISSION_DENIED)
      `when`(ContextCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE"))
          .thenReturn(PERMISSION_DENIED)

      val result = checkFullPermission(context)
      assertFalse(result) // Expecting false because permission is not granted
    }
    @Test
    fun checkLimitedPermissionReturnsTrue() {
      // Mock the context
      val context = mock(Context::class.java)

      // Mock the permission check to return PERMISSION_GRANTED for limited access
      `when`(
              ContextCompat.checkSelfPermission(
                  context, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"))
          .thenReturn(PERMISSION_GRANTED)

      val result = checkLimitedPermission(context)
      assertTrue(result) // Expecting true because limited permission is granted
    }

    @Test
    fun checkLimitedPermissionReturnsFalse() {
      // Mock the context
      val context = mock(Context::class.java)

      // Mock the permission check to return PERMISSION_DENIED for limited access
      `when`(
              ContextCompat.checkSelfPermission(
                  context, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"))
          .thenReturn(PERMISSION_DENIED)

      val result = checkLimitedPermission(context)
      assertFalse(result) // Expecting false because limited permission is not granted
    }
}