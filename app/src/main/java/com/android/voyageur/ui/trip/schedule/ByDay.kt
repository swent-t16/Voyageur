package com.android.voyageur.ui.trip.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.isDraft
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.trip.activities.AddActivityButton
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
/**
 * Displays the ByDayScreen, which organizes and displays activities grouped by the day in which
 * they take place. Each card corresponds to a single day, and activities for that day are displayed
 * in a scrollable list. Users can view activities, navigate to a detailed screen, or add new
 * activities (if not in read-only mode).
 *
 * @param tripsViewModel The ViewModel responsible for managing the trip's activities and data.
 * @param trip The trip whose activities are being displayed and organized by date.
 * @param navigationActions Handles navigation actions such as switching to other screens.
 * @param userViewModel The ViewModel responsible for user-specific data and state.
 * @param isReadOnlyView Boolean which determines if the user is in Read Only View and cannot add
 *   new activities.
 */
@Composable
fun ByDayScreen(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    isReadOnlyView: Boolean = false
) {
  Scaffold(
      floatingActionButton = {
        if (!isReadOnlyView) {
          AddActivityButton(navigationActions)
        }
      },
      modifier = Modifier.testTag("byDayScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel = userViewModel,
            tripsViewModel)
      },
      content = { pd ->
        val tripActivities = tripsViewModel.getActivitiesForSelectedTrip()
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
        if (groupedActivities.isEmpty()) {
          // Display empty prompt if there are no activities
          Box(modifier = Modifier.padding(pd).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.testTag("emptyByDayPrompt"),
                text = stringResource(R.string.empty_activities_prompt),
            )
          }
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

/**
 * Function to sort Activities. It sorts by startTime, and if startTime is equal sorts by endTime.
 * It groups the activities in a map with the day as key.
 */
fun groupActivitiesByDate(activities: List<Activity>): Map<LocalDate, List<Activity>> {
  val sortedActivities =
      activities.sortedWith(
          compareBy(
              { it.startTime }, // First, sort by startTime
              { it.endTime } // If startTime is equal, sort by endTime
              ))
  // Group into a map with the day as key
  return sortedActivities.groupBy { activity ->
    activity.startTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  }
}

@Composable
/**
 * Day Card which displays the date and a column with activities for the corresponding days.
 *
 * @param tripsViewModel The ViewModel responsible for managing the trip's activities and state.
 * @param day The date for which the card is displayed.
 * @param activitiesForDay A list of activities that occur on the given date.
 * @param navigationActions Handles navigation to other screens, such as the detailed activity view.
 */
private fun DayActivityCard(
    tripsViewModel: TripsViewModel,
    day: LocalDate,
    activitiesForDay: List<Activity>,
    navigationActions: NavigationActions
) {
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
      colors = CardDefaults.cardColors(),
      shape = RoundedCornerShape(16.dp),
      content = {
        Text(
            text = formatDailyDate(day),
            style =
                TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(500),
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
            // Displays additional text in case of too many activities
            Text(
                text = stringResource(R.string.additional_activities, (numberOfActivities - 4)),
                style =
                    TextStyle(fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 4.dp))
          }
        }
      })
}

@Composable
/**
 * Activity box which displays activity title.
 *
 * @param activity The activity whose title should be displayed
 */
private fun ActivityBox(activity: Activity) {
  // Appropriate background for both Light and Dark Themes
  val backgroundColor = ButtonDefaults.buttonColors().containerColor
  Box(
      modifier =
          Modifier.width(119.dp)
              .testTag("activityBox")
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
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    letterSpacing = 0.1.sp,
                ),
            maxLines = 1, // Limit to a single line
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp))
      }
}

/** Formats the date of the day */
fun formatDailyDate(date: LocalDate?): String {
  // Wrap in a try-catch to avoid an exception crashing the app
  return try {
    date?.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)) ?: "Invalid date"
  } catch (e: Exception) {
    "Invalid date"
  }
}
