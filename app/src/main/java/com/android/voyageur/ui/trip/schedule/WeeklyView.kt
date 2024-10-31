package com.android.voyageur.ui.trip.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TRIP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun WeeklyViewScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
) {
  val trip =
      tripsViewModel.selectedTrip.collectAsState().value
          ?: return Text(
              text = "No ToDo selected. Should not happen",
              color = Color.Red,
              modifier = Modifier.testTag("errorText"))
  val weeks = generateWeeks(trip.startDate, trip.endDate)

  Scaffold(
      modifier = Modifier.testTag("weeklyViewScreen"),
      topBar = { TopBarWithImage(selectedTrip = trip, navigationActions = navigationActions) },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TRIP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(pd).testTag("weeksColumn"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              item { Spacer(modifier = Modifier.height(16.dp)) }
              item {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(bottom = 8.dp, end = 16.dp)
                            .testTag("viewToggleRow"),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "Daily",
                          style = MaterialTheme.typography.bodyLarge,
                          color = Color.Gray,
                          modifier = Modifier.testTag("dailyToggle"))
                      Text(
                          text = " / ",
                          style = MaterialTheme.typography.bodyLarge,
                          color = Color.Gray)
                      Text(
                          text = "Weekly",
                          style = MaterialTheme.typography.bodyLarge,
                          color = Color.Black,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.testTag("weeklyToggle"))
                    }
              }

              items(weeks.size) { weekIndex ->
                WeekCard(
                    weekStart = weeks[weekIndex].first,
                    weekEnd = weeks[weekIndex].last,
                    activities = trip.activities,
                    navigationActions = navigationActions,
                    tripsViewModel = tripsViewModel,
                    trip = trip,
                    weekIndex = weekIndex)
              }
            }
      })
}

@Composable
private fun WeekCard(
    weekStart: LocalDate,
    weekEnd: LocalDate,
    activities: List<Activity>,
    navigationActions: NavigationActions,
    tripsViewModel: TripsViewModel,
    trip: Trip,
    weekIndex: Int
) {
  Card(
      modifier = Modifier.width(360.dp).testTag("weekCard_$weekIndex"),
      colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
      shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = "${formatDate(weekStart)} - ${formatDate(weekEnd)}",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 8.dp).testTag("weekRange_$weekIndex"))

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
                    date = currentDate,
                    activityCount = activitiesForDay.size,
                    navigationActions = navigationActions,
                    tripsViewModel = tripsViewModel,
                    trip = trip,
                    dayIndex = dayOffset,
                    weekIndex = weekIndex)
              }
            }
      }
}

@Composable
private fun DayActivityCount(
    date: LocalDate,
    activityCount: Int,
    navigationActions: NavigationActions,
    tripsViewModel: TripsViewModel,
    trip: Trip,
    dayIndex: Int,
    weekIndex: Int
) {
  Button(
      onClick = {
        navigationActions.navigateTo(Screen.BY_DAY)
        tripsViewModel.selectTrip(trip)
      },
      modifier = Modifier.fillMaxWidth().testTag("dayButton_${weekIndex}_$dayIndex"),
      shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                  text = "${date.dayOfWeek.toString().take(1)} ${date.dayOfMonth}",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.testTag("dayText_${weekIndex}_$dayIndex"))

              Text(
                  text = "  -  $activityCount activities",
                  style = MaterialTheme.typography.bodyLarge,
                  modifier = Modifier.testTag("activityCount_${weekIndex}_$dayIndex"))
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
  return "${date.month.toString().take(3)} ${date.dayOfMonth}"
}

data class DateRange(val first: LocalDate, val last: LocalDate)
