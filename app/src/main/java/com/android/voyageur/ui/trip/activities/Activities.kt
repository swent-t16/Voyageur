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
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.activity.hasDescription
import com.android.voyageur.model.activity.hasEndDate
import com.android.voyageur.model.activity.hasStartTime
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
      trip.activities.filter { activity ->
        activity.startTime != Timestamp(0, 0) && activity.endTime != Timestamp(0, 0)
      }

  Scaffold(
      // TODO: Search Bar
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

/** Composable that displays an activity item in a card view. */
@Composable
fun ActivityItem(
    activity: Activity,
) {
  var isExpanded by remember { mutableStateOf(false) }

  val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
  val timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

  // Convert Timestamp to LocalDateTime for formatting
  val startLocalDateTime =
      activity.startTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
  val endLocalDateTime =
      activity.endTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

  // Format the date and time
  val dateFormatted = dateFormat.format(startLocalDateTime)
  val startTimeFormatted = timeFormat.format(startLocalDateTime)
  val endTimeFormatted = timeFormat.format(endLocalDateTime)

  Card(
      shape = MaterialTheme.shapes.medium,
      modifier = Modifier.padding(start = 10.dp, end = 10.dp).testTag("cardItem_${activity.title}"),
      content = {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
          Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                      text = activity.title,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(end = 8.dp),
                      fontSize = 16.sp)
                  if (activity.hasStartTime() && activity.hasEndDate()) {
                    Text(
                        text = "$dateFormatted $startTimeFormatted - $endTimeFormatted",
                        fontSize = 12.sp,
                        color = Color.Gray)
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Delete icon, visible only when expanded
                  AnimatedVisibility(visible = isExpanded) {
                    IconButton(onClick = { /*TODO: delete activity*/}) {
                      Icon(
                          imageVector = Icons.TwoTone.Delete,
                          contentDescription = "Delete Activity",
                          tint = Color.Red)
                    }
                  }

                  // Expand/collapse icon
                  IconButton(
                      onClick = { isExpanded = !isExpanded },
                      modifier = Modifier.testTag("expandIcon_${activity.title}")) {
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
              if (activity.hasDescription()) {
                Text(
                    text = "Description",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp))
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp))
              }
              // TODO: Add location once we have the final location model
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

fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Timestamp {
  val calendar = java.util.Calendar.getInstance()
  calendar.set(year, month - 1, day, hour, minute) // Month is 0-based
  return Timestamp(calendar.time)
}

val SAMPLE_ACTIVITIES =
    listOf(
        Activity(
            title = "Breakfast",
            description = "",
            activityType = ActivityType.RESTAURANT,
            startTime = createTimestamp(2024, 11, 3, 10, 0),
            endTime = createTimestamp(2024, 11, 3, 12, 30),
            estimatedPrice = 20.25,
            location = Location()),
        Activity(
            title = "Lunch",
        ),
        Activity(
            title = "Dinner",
            description = "Dinner description",
            activityType = ActivityType.RESTAURANT,
            startTime = Timestamp.now(),
            endTime = Timestamp.now(),
            estimatedPrice = 23.25,
            location = Location()),
        Activity(
            title = "Dinner2",
            description = "",
            activityType = ActivityType.RESTAURANT,
            startTime = Timestamp.now(),
            endTime = Timestamp.now(),
            estimatedPrice = 23.25,
            location = Location()),
        Activity(
            title = "Dinner3",
            description = "",
            activityType = ActivityType.RESTAURANT,
            startTime = createTimestamp(2024, 11, 3, 19, 30),
            endTime = createTimestamp(2024, 11, 3, 21, 0),
            estimatedPrice = 23.25,
            location = Location()),
        Activity(
            title = "Dinner4",
            description = "Too long description to be displayed on the Activity Box",
            activityType = ActivityType.RESTAURANT,
            startTime = Timestamp.now(),
            endTime = Timestamp.now(),
            estimatedPrice = 23.25,
            location = Location()),
        Activity(
            title = "Dinner5",
            description = "",
            activityType = ActivityType.RESTAURANT,
            startTime = Timestamp.now(),
            endTime = Timestamp.now(),
            estimatedPrice = 23.25,
            location = Location()),
        Activity(
            title = "Too long name to be displayed on the Activity Box",
            description = "",
            activityType = ActivityType.OTHER,
            startTime = Timestamp.now(),
            endTime = Timestamp.now(),
            estimatedPrice = 00.00,
            location = Location()),
        Activity(
            title = "Visit city",
            description = "",
            activityType = ActivityType.OTHER,
            startTime = Timestamp.now(),
            endTime = Timestamp.now(),
            estimatedPrice = 00.00,
            location = Location()))
