package com.android.voyageur.notifications

import android.content.Context
import com.android.voyageur.ui.notifications.AndroidStringProvider
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AndroidStringProviderTest {

  @Test
  fun `getString returns correct string for resource ID`() {
    // Arrange
    val mockContext = mock(Context::class.java)
    val testString = "Test String"
    val testResId = 123 // Arbitrary resource ID
    `when`(mockContext.getString(testResId)).thenReturn(testString)

    val stringProvider = AndroidStringProvider(mockContext)

    // Act
    val result = stringProvider.getString(testResId)

    // Assert
    assertEquals("Should return the correct string", testString, result)
    verify(mockContext).getString(testResId)
  }
}
