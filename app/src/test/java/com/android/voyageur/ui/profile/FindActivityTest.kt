package com.android.voyageur.ui.profile

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FindActivityTest {

  @Test
  fun contextIsComponentActivity_returnsActivity() {
    // Arrange: Create a ComponentActivity using Robolectric
    val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

    // Act: Call findActivity on the activity itself
    val result = activity.findActivity()

    // Assert: The result should be the same activity
    assertEquals(activity, result)
  }

  @Test
  fun contextIsWrappedComponentActivity_returnsActivity() {
    // Arrange: Create a ComponentActivity using Robolectric
    val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

    // Wrap the activity in a ContextWrapper
    val contextWrapper = ContextWrapper(activity)

    // Act: Call findActivity on the wrapped context
    val result = contextWrapper.findActivity()

    // Assert: The result should be the original activity
    assertEquals(activity, result)
  }

  @Test
  fun contextIsDeeplyWrappedComponentActivity_returnsActivity() {
    // Arrange: Create a ComponentActivity using Robolectric
    val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

    // Wrap the activity multiple times
    val contextWrapperLevel1 = ContextWrapper(activity)
    val contextWrapperLevel2 = ContextWrapper(contextWrapperLevel1)
    val contextWrapperLevel3 = ContextWrapper(contextWrapperLevel2)

    // Act: Call findActivity on the deeply wrapped context
    val result = contextWrapperLevel3.findActivity()

    // Assert: The result should be the original activity
    assertEquals(activity, result)
  }
}
