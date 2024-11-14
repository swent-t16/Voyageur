package com.android.voyageur.ui.trip.activities

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivitiesScreen(
    trip: Trip,
    navigationActions: NavigationActions,
) {

  val drafts =
      trip.activities.filter { activity ->
        activity.startTime == Timestamp(0, 0) || activity.endTime == Timestamp(0, 0)
      }
  val final =
      trip.activities
          .filter { activity ->
            activity.startTime != Timestamp(0, 0) && activity.endTime != Timestamp(0, 0)
          }
          .sortedWith(
              compareBy(
                  { it.startTime }, // First, sort by startTime
                  { it.endTime } // If startTime is equal, sort by endTime
                  ))

  Scaffold(
      // TODO: Search Bar
      modifier = Modifier.testTag("activitiesScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      floatingActionButton = { AddActivityButton(navigationActions) },
      content = { pd ->
        LazyColumn(
            modifier =
                Modifier.padding(pd).padding(top = 16.dp).fillMaxWidth().testTag("lazyColumn"),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
        ) {
          item {
            Text(
                text = "Drafts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp))
          }
          drafts.forEach { activity ->
            item {
              ActivityItem(activity)
              Spacer(modifier = Modifier.height(10.dp))
            }
          }
          item {
            Text(
                text = "Final",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp))
          }
          final.forEach { activity ->
            item {
              ActivityItem(activity)
              Spacer(modifier = Modifier.height(10.dp))
            }
          }
        }
      })
}

/** Composable that displays a button that navigates to the Add Activity screen. */
@Composable
fun AddActivityButton(navigationActions: NavigationActions) {
  FloatingActionButton(
      onClick = { navigationActions.navigateTo(Screen.ADD_ACTIVITY) },
      modifier = Modifier.testTag("createActivityButton")) {
        Icon(Icons.Outlined.Add, "Floating action button", modifier = Modifier.testTag("addIcon"))
      }
}
