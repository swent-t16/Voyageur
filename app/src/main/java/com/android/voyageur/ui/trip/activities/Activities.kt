package com.android.voyageur.ui.trip.activities

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    trip: Trip,
    navigationActions: NavigationActions,
) {

  //    val finalActivities = trip.activities.filter { activity ->
  //        activity.startTime != Timestamp(0, 0) && activity.endDate != Timestamp(0, 0)
  //    }

  val finalActivities =
      listOf(
          Activity(
              title = "Breakfast",
              description = "",
              activityType = ActivityType.RESTAURANT,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 20.25,
              location = Location()),
          Activity(
              title = "Dinner",
              description = "",
              activityType = ActivityType.RESTAURANT,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 23.25,
              location = Location()),
          Activity(
              title = "Dinner2",
              description = "",
              activityType = ActivityType.RESTAURANT,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 23.25,
              location = Location()),
          Activity(
              title = "Dinner3",
              description = "",
              activityType = ActivityType.RESTAURANT,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 23.25,
              location = Location()),
          Activity(
              title = "Dinner4",
              description = "",
              activityType = ActivityType.RESTAURANT,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 23.25,
              location = Location()),
          Activity(
              title = "Dinner5",
              description = "",
              activityType = ActivityType.RESTAURANT,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 23.25,
              location = Location()),
          Activity(
              title = "Too long name to be displayed on the Activity Box",
              description = "",
              activityType = ActivityType.OTHER,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 00.00,
              location = Location()),
          Activity(
              title = "Visit city",
              description = "",
              activityType = ActivityType.OTHER,
              //                startTime = oneDayAfterNow,
              //                endDate = oneDayAfterNow,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 00.00,
              location = Location()))

  Scaffold(
      // TODO: Final implementation of ActivitiesScreen
      modifier = Modifier.testTag("activitiesScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        LazyColumn(
            modifier =
                Modifier.padding(pd) // Use Scaffold’s padding
                    .padding(top = 16.dp) // Additional padding at the top
                    .fillMaxWidth() // Fill the available width
                    .testTag("lazyColumn"),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
        ) {
          item { Text(text = "Drafts",               fontSize = 24.sp,
              fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp)) }
          finalActivities.forEach { activity ->
            item {
              ActivityItem(activity, onDelete = { /* Handle delete action */})
              Spacer(modifier = Modifier.height(10.dp))
            }
          }
        }
      })
}

@Composable
fun ActivityItem(
    activity: Activity,
    onDelete: (Activity) -> Unit // Callback function for delete action
) {
  // State to track expanded or collapsed status
  var isExpanded by remember { mutableStateOf(false) }

  // Format the start and end times
  val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
  val startTimeFormatted = timeFormat.format(activity.startTime.toDate())
  val endTimeFormatted = timeFormat.format(activity.endDate.toDate())
    Card(
       shape = MaterialTheme.shapes.medium,
      modifier = Modifier.padding(start = 10.dp, end = 10.dp)
          .testTag("cardItem"),
        content = {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
          Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()) {
                Column (modifier = Modifier.weight(1f)) {
                  Text(text = activity.title, fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(end = 8.dp),
                      fontSize = 16.sp)
                  Text(
                      text = "$startTimeFormatted - $endTimeFormatted",
                      fontSize = 12.sp,
                      color = Color.Gray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Delete icon, visible only when expanded
                  AnimatedVisibility(visible = isExpanded) {
                    IconButton(onClick = { onDelete(activity) }) {
                      Icon(
                          imageVector =
                              Icons.TwoTone.Delete, // Use Icons.Default.Delete for trashcan
                          contentDescription = "Delete Activity",
                          tint = Color.Red)
                    }
                  }

                  // Expand/collapse icon with rotation
                  IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f))
                  }
                }
              }

          // Expandable content with animation
          AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
              Text(
                  text = "Description",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp,
                  modifier = Modifier.padding(vertical = 2.dp))
              Text(
                  text = activity.description,
                  fontSize = 14.sp,
                  modifier = Modifier.padding(bottom = 8.dp))

              Text(
                  text = "Price",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp,
                  modifier = Modifier.padding(vertical = 2.dp))
              Text(
                  text = "${activity.estimatedPrice} CHF",
                  fontSize = 14.sp,
                  modifier = Modifier.padding(bottom = 8.dp))

              Text(
                  text = "Type",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp,
                  modifier = Modifier.padding(vertical = 2.dp))
              Text(text = activity.activityType.toString(), fontSize = 14.sp)
            }
          }
        }
      })
}
