package com.android.voyageur.ui.trip.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.trip.activities.AddActivityButton
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A composable that displays a weekly view of a trip's schedule. Shows a calendar-like interface
 * with weeks broken down into days, highlighting days with activities.
 *
 * @param tripsViewModel ViewModel handling trip-related data and operations
 * @param trip The current trip being displayed
 * @param navigationActions Handler for navigation between screens
 * @param userViewModel ViewModel containing user-related data and operations
 */
@Composable
fun WeeklyViewScreen(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {
  val weeks = generateWeeks(trip.startDate, trip.endDate)

  Scaffold(
      floatingActionButton = { AddActivityButton(navigationActions) },
      modifier = Modifier.fillMaxSize().testTag("weeklyViewScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel = userViewModel)
      }) { pd ->
        Box(modifier = Modifier.fillMaxSize().padding(pd)) {
          if (weeks.isEmpty()) {
            Box(
                modifier = Modifier.padding(pd).fillMaxSize(),
                contentAlignment = Alignment.Center) {
                  Text(
                      modifier = Modifier.testTag("emptyWeeksPrompt"),
                      text = "You have no weeks scheduled yet.")
                }
          } else {
            LazyColumn(
                modifier =
                    Modifier.padding(pd).padding(top = 16.dp).fillMaxSize().testTag("weeksColumn"),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  items(weeks.size) { weekIndex ->
                    WeekCard(
                        tripsViewModel = tripsViewModel,
                        trip = trip,
                        weekStart = weeks[weekIndex].first,
                        weekEnd = weeks[weekIndex].last,
                        activities = tripsViewModel.getActivitiesForSelectedTrip(),
                        weekIndex = weekIndex,
                        navigationActions = navigationActions)
                    Spacer(modifier = Modifier.height(10.dp))
                  }
                }
          }
        }
      }
}

/**
 * A card component that displays a single week of the trip schedule. Shows the week's date range
 * and individual days with their activities.
 *
 * @param tripsViewModel ViewModel handling trip-related data and operations
 * @param trip The current trip being displayed
 * @param weekStart First day of the week
 * @param weekEnd Last day of the week
 * @param activities List of activities for the trip
 * @param weekIndex Index of the week in the overall schedule
 * @param navigationActions Handler for navigation between screens
 */
@Composable
private fun WeekCard(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    weekStart: LocalDate,
    weekEnd: LocalDate,
    activities: List<Activity>,
    weekIndex: Int,
    navigationActions: NavigationActions
) {
  Card(
      modifier =
          Modifier.width(353.dp).height(285.dp).padding(10.dp).testTag("weekCard_$weekIndex"),
      colors = CardDefaults.cardColors(),
      shape = RoundedCornerShape(16.dp)) {
        WeekHeader(weekStart, weekEnd)
        WeekDaysContent(
            weekStart = weekStart,
            activities = activities,
            trip = trip,
            tripsViewModel = tripsViewModel,
            navigationActions = navigationActions)
      }
}

/**
 * Displays the date range header for the week card.
 *
 * @param weekStart First day of the week
 * @param weekEnd Last day of the week
 */
@Composable
private fun WeekHeader(weekStart: LocalDate, weekEnd: LocalDate) {
  Text(
      text = "${formatDate(weekStart)} - ${formatDate(weekEnd)}",
      style =
          TextStyle(
              fontSize = 14.sp,
              lineHeight = 20.sp,
              fontWeight = FontWeight(500),
              letterSpacing = 0.14.sp,
          ),
      modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp))
}

/**
 * Displays a column of days for the week with their associated activities.
 *
 * @param weekStart First day of the week
 * @param activities List of all activities for the trip
 * @param trip The current trip being displayed
 * @param tripsViewModel ViewModel handling trip-related data and operations
 * @param navigationActions Handler for navigation between screens
 */
@Composable
private fun WeekDaysContent(
    weekStart: LocalDate,
    activities: List<Activity>,
    trip: Trip,
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions
) {
  Column(
      verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
      modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp).testTag("daysColumn"),
  ) {
    repeat(7) { dayOffset ->
      val currentDate = weekStart.plusDays(dayOffset.toLong())
      DayRow(
          currentDate = currentDate,
          activities = activities,
          trip = trip,
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions)
    }
  }
}

/**
 * Displays a single day row with its activities and handles click interactions.
 *
 * @param currentDate The date being displayed
 * @param activities List of all activities for the trip
 * @param trip The current trip being displayed
 * @param tripsViewModel ViewModel handling trip-related data and operations
 * @param navigationActions Handler for navigation between screens
 */
@Composable
private fun DayRow(
    currentDate: LocalDate,
    activities: List<Activity>,
    trip: Trip,
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions
) {
  val activitiesForDay = getActivitiesForDay(activities, currentDate)
  val isInTrip = isDateInTrip(currentDate, trip)
  val dayText = buildDayText(currentDate, activitiesForDay.size, isInTrip)

  Box(
      modifier =
          Modifier.width(130.dp)
              .height(19.dp)
              .background(
                  color =
                      ButtonDefaults.buttonColors()
                          .containerColor
                          .copy(alpha = if (isInTrip) 1f else 0.5f),
                  shape = RoundedCornerShape(size = 25.dp))
              .then(
                  if (isInTrip) {
                    Modifier.clickable {
                      tripsViewModel.selectDay(currentDate)
                      navigationActions.navigateTo(Screen.ACTIVITIES_FOR_ONE_DAY)
                    }
                  } else Modifier),
      contentAlignment = Alignment.CenterStart) {
        DayText(dayText, isInTrip)
      }
}

/**
 * Displays the formatted text for a day with appropriate styling based on trip status.
 *
 * @param dayText The formatted text to display for the day
 * @param isInTrip Whether this day falls within the trip dates
 */
@Composable
private fun DayText(dayText: String, isInTrip: Boolean) {
  Text(
      text = dayText,
      style =
          TextStyle(
              fontSize = 10.sp,
              lineHeight = 20.sp,
              fontWeight = FontWeight(500),
              color =
                  MaterialTheme.colorScheme.inverseOnSurface.copy(
                      alpha = if (isInTrip) 1f else 0.5f),
              letterSpacing = 0.1.sp,
          ),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = 10.dp))
}

/**
 * Filters activities to find those occurring on a specific date.
 *
 * @param activities List of all activities to filter
 * @param currentDate The date to filter activities for
 * @return List of activities occurring on the specified date
 */
private fun getActivitiesForDay(
    activities: List<Activity>,
    currentDate: LocalDate
): List<Activity> {
  return activities.filter { activity ->
    val activityDate =
        activity.startTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    activityDate == currentDate
  }
}

/**
 * Builds the display text for a day in the weekly view. Format varies based on whether the day is
 * within the trip dates and has activities.
 *
 * @param date The date to generate text for
 * @param activityCount Number of activities on this date
 * @param isInTrip Whether the date falls within the trip's date range
 * @return Formatted string representation of the day (e.g., "M 15 - 2 activities" or "M 15")
 */
private fun buildDayText(date: LocalDate, activityCount: Int, isInTrip: Boolean): String {
  val dayInitial = date.dayOfWeek.toString().take(1)
  val dayNumber = date.dayOfMonth
  return if (isInTrip && activityCount == 1) {
    "$dayInitial $dayNumber - $activityCount activity"
  } else if (isInTrip && activityCount > 1) {
    "$dayInitial $dayNumber - $activityCount activity"
  } else {
    "$dayInitial $dayNumber"
  }
}

/**
 * Formats a date into a readable string representation. Converts the date to format "MMM d" (e.g.
 * "Oct 15")
 *
 * @param date The date to format
 * @return Formatted date string
 */
fun formatDate(date: LocalDate): String {
  val month =
      date.format(DateTimeFormatter.ofPattern("MMM").withLocale(Locale.getDefault())).take(3)
  return "${capitalizeFirstLetter(month)} ${date.dayOfMonth}"
}

/**
 * Capitalizes the first letter of a given text string.
 *
 * @param text The text to capitalize
 * @return Text with first letter capitalized
 */
private fun capitalizeFirstLetter(text: String): String {
  return text.lowercase().replaceFirstChar { it.uppercase() }
}

/**
 * Determines if a given date falls within the trip's date range.
 *
 * @param date The date to check
 * @param trip The trip to check against
 * @return true if the date is within the trip's start and end dates, false otherwise
 */
private fun isDateInTrip(date: LocalDate, trip: Trip): Boolean {
  val tripStartDate =
      trip.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  val tripEndDate = trip.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  return !date.isBefore(tripStartDate) && !date.isAfter(tripEndDate)
}

/**
 * Generates a list of week ranges that cover the entire trip duration. Each week starts on Monday
 * and ends on Sunday.
 *
 * @param startTimestamp The trip's start timestamp
 * @param endTimestamp The trip's end timestamp
 * @return List of DateRange objects representing each week
 */
fun generateWeeks(startTimestamp: Timestamp, endTimestamp: Timestamp): List<DateRange> {
  val startDate = startTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  val endDate = endTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

  val weeks = mutableListOf<DateRange>()

  var currentWeekStart = startDate.with(DayOfWeek.MONDAY)
  val lastWeekStart = endDate.with(DayOfWeek.MONDAY)

  while (currentWeekStart <= lastWeekStart) {
    weeks.add(DateRange(first = currentWeekStart, last = currentWeekStart.plusDays(6)))
    currentWeekStart = currentWeekStart.plusWeeks(1)
  }

  return weeks
}

/**
 * Data class representing a range of dates from first to last. Used to represent a week in the
 * schedule view.
 *
 * @property first The first date in the range
 * @property last The last date in the range
 */
data class DateRange(val first: LocalDate, val last: LocalDate)
