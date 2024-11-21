package com.android.voyageur.ui.trip.activities

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivitiesScreen(
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    tripsViewModel: TripsViewModel
) {

//  var drafts =
//      trip.activities.filter { activity ->
//        activity.startTime == Timestamp(0, 0) || activity.endTime == Timestamp(0, 0)
//      }

    var drafts by remember { mutableStateOf(emptyList<Activity>()) }
    var final by remember { mutableStateOf(emptyList<Activity>()) }
//  val final =
//      trip.activities
//          .filter { activity ->
//            activity.startTime != Timestamp(0, 0) && activity.endTime != Timestamp(0, 0)
//          }
//          .sortedWith(
//              compareBy(
//                  { it.startTime }, // First, sort by startTime
//                  { it.endTime } // If startTime is equal, sort by endTime
//                  ))
  val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch the activities from the trip
    LaunchedEffect (Unit) {
        isLoading = true
        drafts = trip.activities.filter { activity ->
            activity.startTime == Timestamp(0, 0) || activity.endTime == Timestamp(0, 0)
        }
        final =    trip.activities
          .filter { activity ->
            activity.startTime != Timestamp(0, 0) && activity.endTime != Timestamp(0, 0)
          }
          .sortedWith(
              compareBy(
                  { it.startTime }, // First, sort by startTime
                  { it.endTime } // If startTime is equal, sort by endTime
                  ))
        isLoading = false

    }

  Scaffold(
      // TODO: Search Bar
      modifier = Modifier.testTag("activitiesScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel = userViewModel)
      },
      floatingActionButton = { AddActivityButton(navigationActions) },
      content = { pd ->
          if (isLoading) {
              // Display a loading indicator while fetching data
              Box(
                  modifier = Modifier.fillMaxSize(),
                  contentAlignment = Alignment.Center
              ) {
                  CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
              }
          }
          else {
              LazyColumn(
                  modifier =
                  Modifier
                      .padding(pd)
                      .padding(top = 16.dp)
                      .fillMaxWidth()
                      .testTag("lazyColumn"),
                  verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
              ) {
                  item {
                      Text(
                          text = "Drafts",
                          fontSize = 24.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(start = 10.dp)
                      )
                  }
                  drafts.forEach { activity ->
                      item {
                          ActivityItem(
                              activity,
                              true,
                              onClickButton = {
                                  Toast.makeText(
                                      context,
                                      "Delete activity not implemented yet",
                                      Toast.LENGTH_SHORT
                                  )
                                      .show()
                              },
                              ButtonType.DELETE,
                              navigationActions,
                              tripsViewModel
                          )
                          Spacer(modifier = Modifier.height(10.dp))
                      }
                  }
                  item {
                      Text(
                          text = "Final",
                          fontSize = 24.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(start = 10.dp)
                      )
                  }
                  final.forEach { activity ->
                      item {
                          ActivityItem(
                              activity,
                              true,
                              onClickButton = {
                                  activityToDelete = activity
                                  showDialog = true
                              },
                              ButtonType.DELETE,
                              navigationActions,
                              tripsViewModel
                          )
                          Spacer(modifier = Modifier.height(10.dp))
                      }
                  }
              }

              if (showDialog) {
                  DeleteActivityAlertDialog(
                      onDismissRequest = { showDialog = false },
                      activityToDelete = activityToDelete,
                      tripsViewModel = tripsViewModel,
                      confirmButtonOnClick = {
                          showDialog = false
                          tripsViewModel.selectedTrip.value?.let {
                              tripsViewModel.removeActivityFromTrip(activityToDelete!!)
                              final = final.filter { it != activityToDelete }
                          }
                          final = final.filter { it != activityToDelete }
                      }
                  )
              }
          }
      })
}

