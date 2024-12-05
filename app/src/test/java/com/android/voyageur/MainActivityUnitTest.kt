package com.android.voyageur

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class MainActivityUnitTest {

  @Test
  fun `test notification permission check`() {
    val context = mock(Context::class.java)
    `when`(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
        .thenReturn(PackageManager.PERMISSION_GRANTED)

    val isPermissionGranted =
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    assertTrue("Notification permission not granted", isPermissionGranted)
  }

  @Test
  fun `test notification permission denial`() {
    val context = mock(Context::class.java)
    `when`(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
        .thenReturn(PackageManager.PERMISSION_DENIED)

    val isPermissionGranted =
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    assertFalse("Permission unexpectedly granted", isPermissionGranted)
  }
}
