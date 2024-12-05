package com.android.voyageur.ui.trip.activities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.trip.schedule.TopBarWithImageAndText
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ActivitiesForOneDayScreen is a composable function that displays a list of activities for a
 * specific day.
 */
@Composable
fun ActivitiesForOneDayScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions
) {
  val trip =
      tripsViewModel.selectedTrip.value
          ?: return Text(text = "No trip selected. Should not happen", color = Color.Red)
  val day =
      tripsViewModel.selectedDay.value
          ?: return Text(text = "No day selected. Should not happen", color = Color.Red)

  var activities by remember {
    mutableStateOf(
        tripsViewModel
            .getActivitiesForSelectedTrip()
            .filter {
              it.startTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == day
            }
            .sortedWith(
                compareBy(
                    { it.startTime }, // First, sort by startTime
                    { it.endTime } // If startTime is equal, sort by endTime
                    )))
  }
  var showDialog by remember { mutableStateOf(false) }
  var activityToDelete by remember { mutableStateOf<Activity?>(null) }

  var totalEstimatedPrice by remember { mutableStateOf(0.0) }

  // Calculate the total estimated price whenever the activities change
  LaunchedEffect(activities) { totalEstimatedPrice = activities.sumOf { it.estimatedPrice } }

  Scaffold(
      modifier = Modifier.testTag("activitiesForOneDayScreen"),
      topBar = {
        TopBarWithImageAndText(trip, navigationActions, day.toDateWithYearString(), trip.name)
      },
      floatingActionButton = { AddActivityButton(navigationActions) },
      content = { pd ->
        if (activities.isEmpty()) {
          // Display empty prompt if there are no activities
          Box(modifier = Modifier.padding(pd).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.testTag("emptyByDayPrompt"),
                text = "You have no activities yet.",
            )
          }
        } else {
          LazyColumn(
              modifier =
                  Modifier.padding(pd).padding(top = 16.dp).fillMaxWidth().testTag("lazyColumn"),
              verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
          ) {
            activities.forEach { activity ->
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
                    tripsViewModel)
                Spacer(modifier = Modifier.height(10.dp))
              }
            }
            item {
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(16.dp)
                          .background(
                              color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                              shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                          .padding(16.dp)
                          .testTag("totalEstimatedPriceBox"),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.total_price, totalEstimatedPrice),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center))
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
                  activities = activities.filter { it != activityToDelete }
                })
          }
        }
      })
}

fun LocalDate.toDateWithYearString(): String {
  val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
  return this.format(formatter)
}
