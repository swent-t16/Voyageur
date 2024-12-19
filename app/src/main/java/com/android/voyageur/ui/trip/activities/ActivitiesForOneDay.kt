package com.android.voyageur.ui.trip.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.components.ActivityFilter
import com.android.voyageur.ui.components.SearchBar
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.trip.schedule.TopBarWithImageAndText
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ActivitiesForOneDayScreen is a composable function that displays a list of activities for a
 * specific day. It includes search and filter functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesForOneDayScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
) {
  val isReadOnlyView = navigationActions.getNavigationState().isReadOnlyView
  val trip =
      tripsViewModel.selectedTrip.value
          ?: return Text(text = stringResource(R.string.no_trip_selected), color = Color.Red)
  val day =
      tripsViewModel.selectedDay.value
          ?: return Text(text = stringResource(R.string.no_day_selected), color = Color.Red)

  // States for filtering and search
  var selectedFilters by remember { mutableStateOf(setOf<ActivityType>()) }
  var searchQuery by remember { mutableStateOf("") }
  var activities by remember {
    mutableStateOf(getFilteredActivities(tripsViewModel, day, searchQuery, selectedFilters))
  }
  var showDialog by remember { mutableStateOf(false) }
  var activityToDelete by remember { mutableStateOf<Activity?>(null) }
  var pricePerDay by remember { mutableStateOf(0.0) }

  // Update activities when search query or filters change
  LaunchedEffect(searchQuery, selectedFilters) {
    activities = getFilteredActivities(tripsViewModel, day, searchQuery, selectedFilters)
  }

  // Calculate the total estimated price whenever the activities change
  LaunchedEffect(activities) { pricePerDay = activities.sumOf { it.estimatedPrice } }

  Scaffold(
      modifier = Modifier.testTag("activitiesForOneDayScreen"),
      topBar = {
        Column {
          TopBarWithImageAndText(trip, navigationActions, day.toDateWithYearString(), trip.name)
          TopAppBar(
              title = {
                Box(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart) {
                      SearchBar(
                          placeholderId = R.string.activities_searchbar_placeholder,
                          onQueryChange = { searchQuery = it },
                          modifier = Modifier.testTag("searchField"))
                    }
              },
              actions = {
                ActivityFilter(
                    selectedFilters = selectedFilters,
                    onFiltersChanged = { newFilters -> selectedFilters = newFilters })
              },
              modifier = Modifier.testTag("topAppBar"))
        }
      },
      floatingActionButton = {
        if (!isReadOnlyView) {
          AddActivityButton(navigationActions)
        }
      },
      content = { pd ->
        if (activities.isEmpty()) {
          Box(modifier = Modifier.padding(pd).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.testTag("emptyByDayPrompt"),
                text = stringResource(R.string.no_activities),
            )
          }
        } else {
          val isEditable = !isReadOnlyView
          val buttonType = if (isEditable) ButtonType.DELETE else ButtonType.NOTHING
          LazyColumn(
              modifier =
                  Modifier.padding(pd).padding(top = 16.dp).fillMaxWidth().testTag("lazyColumn"),
              verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
          ) {
            activities.forEach { activity ->
              item {
                ActivityItem(
                    activity,
                    isEditable,
                    onClickButton = {
                      activityToDelete = activity
                      showDialog = true
                    },
                    buttonType,
                    navigationActions,
                    tripsViewModel)
                Spacer(modifier = Modifier.height(10.dp))
              }
            }
            item { EstimatedPriceBox(pricePerDay) }
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

/**
 * Filters and sorts activities based on specified criteria.
 *
 * @param tripsViewModel The ViewModel containing the activities data
 * @param day The specific date for which to filter activities
 * @param searchQuery The search string to filter activities by title (case-insensitive)
 * @param selectedFilters Set of activity types to filter by. If empty, all types are included
 * @return A sorted list of activities that match all the filtering criteria
 *
 * The function applies three filters:
 * 1. Date matching: Activities must occur on the specified day
 * 2. Search matching: Activity title must contain the search query (if non-empty)
 * 3. Type matching: Activity type must be in the selected filters (if any)
 *
 * The resulting list is sorted first by start time, then by end time for activities with the same
 * start time.
 */
private fun getFilteredActivities(
    tripsViewModel: TripsViewModel,
    day: LocalDate,
    searchQuery: String,
    selectedFilters: Set<ActivityType>
): List<Activity> {
  return tripsViewModel
      .getActivitiesForSelectedTrip()
      .filter {
        val matchesDate =
            it.startTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == day
        val matchesSearch =
            searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilters.isEmpty() || it.activityType in selectedFilters
        matchesDate && matchesSearch && matchesFilter
      }
      .sortedWith(compareBy({ it.startTime }, { it.endTime }))
}

/**
 * Formats a LocalDate into a localized string representation with day, abbreviated month, and year.
 *
 * @return A formatted string in the pattern "d MMM yyyy" according to the default locale. For
 *   example: "15 Dec 2024" for English locale
 *
 * The function uses the system's default locale for month abbreviation, making it suitable for
 * international use. The pattern ensures consistent formatting across the application.
 */
fun LocalDate.toDateWithYearString(): String {
  val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
  return this.format(formatter)
}
