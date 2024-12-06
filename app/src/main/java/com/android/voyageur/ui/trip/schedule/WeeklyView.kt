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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
                      text = "You have no weeks scheduled yet.",
                  )
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
          Modifier.width(353.dp)
              .height(285.dp)
              .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
              .testTag("weekCard_$weekIndex"),
      colors = CardDefaults.cardColors(),
      shape = RoundedCornerShape(16.dp)) {
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

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp).testTag("daysColumn"),
        ) {
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
            val isInTrip = isDateInTrip(currentDate, trip)

            ActivityBox(
                currentDate = currentDate,
                activitiesForDay = activitiesForDay,
                isEnabled = isInTrip,
                onClick =
                    if (isInTrip) {
                      {
                        tripsViewModel.selectDay(currentDate)
                        navigationActions.navigateTo(Screen.ACTIVITIES_FOR_ONE_DAY)
                      }
                    } else null)
          }
        }
      }
}

@Composable
private fun ActivityBox(
    currentDate: LocalDate,
    activitiesForDay: List<Activity>,
    isEnabled: Boolean,
    onClick: (() -> Unit)? = null
) {
  val backgroundColor = ButtonDefaults.buttonColors().containerColor
  Box(
      modifier =
          Modifier.width(130.dp)
              .height(19.dp)
              .background(
                  color = backgroundColor.copy(alpha = if (isEnabled) 1f else 0.5f),
                  shape = RoundedCornerShape(size = 25.dp))
              .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
      contentAlignment = Alignment.CenterStart) {
        Text(
            text =
                "${currentDate.dayOfWeek.toString().take(1)} ${currentDate.dayOfMonth}" +
                    if (isEnabled) " - ${activitiesForDay.size} activities" else "",
            style =
                TextStyle(
                    fontSize = 10.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(500),
                    color =
                        MaterialTheme.colorScheme.inverseOnSurface.copy(
                            alpha = if (isEnabled) 1f else 0.5f),
                    letterSpacing = 0.1.sp,
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp))
      }
}

fun formatDate(date: LocalDate): String {
  val month =
      date.format(DateTimeFormatter.ofPattern("MMM").withLocale(Locale.getDefault())).take(3)
  return "${capitalizeFirstLetter(month)} ${date.dayOfMonth}"
}

private fun capitalizeFirstLetter(text: String): String {
  return text.lowercase().replaceFirstChar { it.uppercase() }
}

private fun isDateInTrip(date: LocalDate, trip: Trip): Boolean {
  val tripStartDate =
      trip.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  val tripEndDate = trip.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  return !date.isBefore(tripStartDate) && !date.isAfter(tripEndDate)
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

data class DateRange(val first: LocalDate, val last: LocalDate)
