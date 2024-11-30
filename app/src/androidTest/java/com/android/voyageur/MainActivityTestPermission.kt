package com.android.voyageur

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.voyageur.ui.notifications.NotificationHelper
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTestPermission {

  @get:Rule
  val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  fun testNotificationPermissionRequestOnAndroid13() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val scenario = activityRule.scenario
      scenario.onActivity { activity ->
        val permissionRequested =
          activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
        assertEquals(PackageManager.PERMISSION_DENIED, permissionRequested)
      }
    }
  }

  @Test
  fun testNotificationPermissionDeniedBehavior() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val scenario = activityRule.scenario
      scenario.onActivity { activity ->
        activity.onRequestPermissionsResult(
          100,
          arrayOf(Manifest.permission.POST_NOTIFICATIONS),
          intArrayOf(PackageManager.PERMISSION_DENIED)
        )
        assertEquals(
          PackageManager.PERMISSION_DENIED,
          activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
        )
      }
    }
  }

}