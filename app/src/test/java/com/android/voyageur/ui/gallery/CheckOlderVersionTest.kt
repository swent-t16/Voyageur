package com.android.voyageur.ui.gallery

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // This simulates an older version sdk >=28
class CheckOlderVersionTest {

  @Test
  fun checkFullPermission_onOlderVersion_withPermissionGranted() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    org.robolectric.shadows.ShadowApplication.getInstance()
        .grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
    val result = checkFullPermission(context)
    assertEquals(true, result)
  }

  @Test
  fun checkFullPermission_onOlderVersion_withPermissionDenied() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    org.robolectric.shadows.ShadowApplication.getInstance()
        .denyPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
    val result = checkFullPermission(context)
    assertEquals(false, result)
  }
}
