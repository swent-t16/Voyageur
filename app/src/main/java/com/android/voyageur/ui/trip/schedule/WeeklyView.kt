package com.android.voyageur.ui.trip.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
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

@Composable
fun WeeklyViewScreen(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
) {
  val weeks = generateWeeks(trip.startDate, trip.endDate)

  Scaffold(
      floatingActionButton = { AddActivityButton(navigationActions) },
      modifier = Modifier.fillMaxSize().testTag("weeklyViewScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { pd ->
        Box(modifier = Modifier.fillMaxSize().padding(pd)) {
          LazyColumn(
              modifier = Modifier.fillMaxWidth().testTag("weeksColumn"),
              verticalArrangement = Arrangement.spacedBy(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                items(weeks.size) { weekIndex ->
                  WeekCard(
                      tripsViewModel = tripsViewModel,
                      trip = trip,
                      weekStart = weeks[weekIndex].first,
                      weekEnd = weeks[weekIndex].last,
                      activities = trip.activities,
                      weekIndex = weekIndex,
                      navigationActions = navigationActions)
                }
              }
        }
      }
}

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
      modifier = Modifier.width(340.dp).testTag("weekCard_$weekIndex"),
      colors = CardDefaults.cardColors(),
      shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced spacing between days
            ) {
              Text(
                  text = "${formatDate(weekStart)} - ${formatDate(weekEnd)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 6.dp).testTag("weekRange_$weekIndex"))

              for (dayOffset in 0..6) {
                val currentDate = weekStart.plusDays(dayOffset.toLong())
                val activitiesForDay =
                    activities.filter { activity ->
                      val activityDate =
                          activity.startTime
                              .toDate()
                              .toInstant()
                              .atZone(ZoneId.systemDefault())
                              .toLocalDate()
                      activityDate == currentDate
                    }

                DayActivityCount(
                    tripsViewModel = tripsViewModel,
                    date = currentDate,
                    activityCount = activitiesForDay.size,
                    trip = trip,
                    dayIndex = dayOffset,
                    weekIndex = weekIndex,
                    navigationActions = navigationActions)
              }
            }
      }
}

@Composable
private fun DayActivityCount(
    tripsViewModel: TripsViewModel,
    date: LocalDate,
    activityCount: Int,
    trip: Trip,
    dayIndex: Int,
    weekIndex: Int,
    navigationActions: NavigationActions
) {
  val tripStartDate =
      trip.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  val tripEndDate = trip.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  val isDateInTrip = !date.isBefore(tripStartDate) && !date.isAfter(tripEndDate)

  if (isDateInTrip) {
    Button(
        onClick = {
          tripsViewModel.selectDay(date)
          navigationActions.navigateTo(Screen.ACTIVITIES_FOR_ONE_DAY)
        },
        modifier =
            Modifier.fillMaxWidth()
                .height(36.dp) // Reduced height
                .testTag("dayButton_${weekIndex}_$dayIndex"),
        shape = RoundedCornerShape(20.dp)) {
          Row(
              modifier = Modifier.padding(horizontal = 8.dp), // Reduced padding
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${date.dayOfWeek.toString().take(1)} ${date.dayOfMonth}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("dayText_${weekIndex}_$dayIndex"))

                Text(
                    text = "  -  $activityCount activities",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("activityCount_${weekIndex}_$dayIndex"))
              }
        }
  } else {
    // Non-clickable row for dates outside the trip
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .height(36.dp) // Same height as button for consistency
                .padding(horizontal = 16.dp), // Matching button padding
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = "${date.dayOfWeek.toString().take(1)} ${date.dayOfMonth}",
              style = MaterialTheme.typography.bodyMedium,
              color =
                  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), // Disabled state color
              fontWeight = FontWeight.Normal,
              modifier = Modifier.testTag("dayText_${weekIndex}_$dayIndex"))
        }
  }
}

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

fun formatDate(date: LocalDate): String {
  val formatter = DateTimeFormatter.ofPattern("MMM d").withLocale(Locale.getDefault())

  return date.format(formatter).uppercase()
}

data class DateRange(val first: LocalDate, val last: LocalDate)
