package com.android.voyageur.ui.trip.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.isDraft
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.trip.activities.ActivitiesForOneDayScreen
import com.android.voyageur.ui.trip.activities.AddActivityButton
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ByDayScreen(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
) {
  Scaffold(
      floatingActionButton = {
          AddActivityButton(navigationActions)
      },
      modifier = Modifier.testTag("byDayScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        // TODO: sort activities by their hour
        val tripActivities = trip.activities
        val groupedActivities =
            groupActivitiesByDate(tripActivities)
                .mapValues { (_, activities) ->
                  // Filter out draft activities from each group
                  activities.filter { !it.isDraft() }
                }
                .filter { (_, activities) ->
                  // Filter out groups that become empty after removing draft activities
                  activities.isNotEmpty()
                }
        //        val groupedActivities = groupActivitiesByDate(tripActivities)
        if (groupedActivities.isEmpty()) {
          Text(
              modifier = Modifier.padding(pd).testTag("emptyByDayPrompt"),
              text = "You have no activities yet. Schedule one.")
        } else {
          LazyColumn(
              modifier =
                  Modifier.padding(pd) // Use Scaffold’s padding
                      .padding(top = 16.dp) // Additional padding at the top
                      .fillMaxSize() // Fill the available width
                      .testTag("lazyColumn"),
              verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            groupedActivities.forEach { (day, activitiesForDay) ->
              item {
                DayActivityCard(tripsViewModel, day, activitiesForDay, navigationActions)
                Spacer(modifier = Modifier.height(10.dp))
              }
            }
          }
        }
      })
}

// TODO: Test this works
fun groupActivitiesByDate(activities: List<Activity>): Map<LocalDate, List<Activity>> {
  val sortedActivities =
      activities.sortedWith(
          compareBy(
              { it.startTime }, // First, sort by startTime
              { it.endTime } // If startTime is equal, sort by endTime
              ))

  return sortedActivities.groupBy { activity ->
    activity.startTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  }
}

@Composable
fun DayActivityCard(tripsViewModel: TripsViewModel, day: LocalDate, activitiesForDay: List<Activity>, navigationActions: NavigationActions) {
  Card(
      onClick = {
          tripsViewModel.selectDay(day)
          navigationActions.navigateTo(Screen.ACTIVITIES_FOR_ONE_DAY)
           },
      modifier =
          Modifier.width(353.dp)
              .height(215.dp)
              .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
              .testTag("cardItem"),
      shape = RoundedCornerShape(16.dp),
      content = {
        Text(
            text = formatDailyDate(day),
            style =
                TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF000000),
                    letterSpacing = 0.14.sp,
                ),
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
            modifier =
                Modifier.padding(horizontal = 30.dp, vertical = 10.dp).testTag("activityColumn"),
        ) {
          val numberOfActivities = activitiesForDay.size
          activitiesForDay
              .filter { !it.isDraft() }
              .take(4)
              .forEach { activity -> ActivityBox(activity) }
          if (numberOfActivities > 4) {
            Text(
                text = "and ${numberOfActivities - 3} more",
                style =
                    TextStyle(fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 4.dp))
          }
        }
      })
}

@Composable
fun ActivityBox(activity: Activity) {
  val backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White
  Box(
      modifier =
          Modifier.width(119.dp)
              .height(19.dp)
              .background(color = backgroundColor, shape = RoundedCornerShape(size = 25.dp)),
      contentAlignment = Alignment.CenterStart) {
        Text(
            text = activity.title,
            style =
                TextStyle(
                    fontSize = 10.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(500),
                    letterSpacing = 0.1.sp,
                ),
            maxLines = 1, // Limit to a single line
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp))
      }
}

// Function to format LocalDate to a "Day, d Month" format
fun formatDailyDate(date: LocalDate): String {
  val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
  return date.format(formatter)
}
