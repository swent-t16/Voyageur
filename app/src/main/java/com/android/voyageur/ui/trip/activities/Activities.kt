package com.android.voyageur.ui.trip.activities

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.components.ActivityFilter
import com.android.voyageur.ui.components.SearchBar
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
/**
 * The main screen for managing and displaying activities in a trip. It provides two categorized
 * lists: drafts and final activities. The screen includes filtering by type, navigation, and and
 * options to edit and delete activities.
 *
 * @param navigationActions Provides actions for navigating between screens.
 * @param userViewModel The [UserViewModel] instance for managing user-related data.
 * @param tripsViewModel The [TripsViewModel] instance for accessing trip and activity data.
 * @param isReadOnly Boolean which determines if the user is in Read Only View and cannot
 *   edit/add/delete activities.
 */
fun ActivitiesScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    tripsViewModel: TripsViewModel,
    isReadOnly: Boolean = false
) {
  // States for filtering
  var selectedFilters by remember { mutableStateOf(setOf<ActivityType>()) }
  var searchQuery by remember { mutableStateOf("") }

  var drafts by remember {
    mutableStateOf(
        tripsViewModel.getActivitiesForSelectedTrip().filter { activity ->
          val matchesTimestamp =
              activity.startTime == Timestamp(0, 0) || activity.endTime == Timestamp(0, 0)
          val matchesSearch =
              searchQuery.isEmpty() || activity.title.contains(searchQuery, ignoreCase = true)
          matchesTimestamp && matchesSearch
        })
  }

  var final by remember {
    mutableStateOf(
        tripsViewModel
            .getActivitiesForSelectedTrip()
            .filter { activity ->
              val matchesTimestamp =
                  activity.startTime != Timestamp(0, 0) && activity.endTime != Timestamp(0, 0)
              val matchesSearch =
                  searchQuery.isEmpty() || activity.title.contains(searchQuery, ignoreCase = true)
              matchesTimestamp && matchesSearch
            }
            .sortedWith(compareBy({ it.startTime }, { it.endTime })))
  }

  LaunchedEffect(searchQuery) {
    drafts =
        tripsViewModel.getActivitiesForSelectedTrip().filter { activity ->
          val matchesTimestamp =
              activity.startTime == Timestamp(0, 0) || activity.endTime == Timestamp(0, 0)
          val matchesSearch =
              searchQuery.isEmpty() || activity.title.contains(searchQuery, ignoreCase = true)
          matchesTimestamp && matchesSearch
        }

    final =
        tripsViewModel
            .getActivitiesForSelectedTrip()
            .filter { activity ->
              val matchesTimestamp =
                  activity.startTime != Timestamp(0, 0) && activity.endTime != Timestamp(0, 0)
              val matchesSearch =
                  searchQuery.isEmpty() || activity.title.contains(searchQuery, ignoreCase = true)
              matchesTimestamp && matchesSearch
            }
            .sortedWith(compareBy({ it.startTime }, { it.endTime }))
  }

  var showDialog by remember { mutableStateOf(false) }
  var activityToDelete by remember { mutableStateOf<Activity?>(null) }
  var totalEstimatedPrice by remember { mutableDoubleStateOf(0.0) }

  LaunchedEffect(final, selectedFilters) {
    totalEstimatedPrice =
        final
            .filter { activity ->
              selectedFilters.isEmpty() || activity.activityType in selectedFilters
            }
            .sumOf { it.estimatedPrice }
  }

  Scaffold(
      modifier = Modifier.testTag("activitiesScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel = userViewModel,
            tripsViewModel = tripsViewModel)
      },
      topBar = {
        TopAppBar(
            title = {
              Box(
                  modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                  contentAlignment = Alignment.CenterStart) {
                    SearchBar(
                        placeholderId = R.string.activities_searchbar_placeholder,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("searchField"))
                  }
            },
            actions = {
              ActivityFilter(
                  selectedFilters = selectedFilters,
                  onFiltersChanged = { newFilters -> selectedFilters = newFilters })
            },
            modifier = Modifier.height(80.dp).testTag("topAppBar"))
      },
      floatingActionButton = {
        if (!isReadOnly) {
          AddActivityButton(navigationActions)
        }
      },
      content = { pd ->
        if (drafts.isEmpty() && final.isEmpty()) {
          // Display empty prompt if there are no activities
          Box(modifier = Modifier.padding(pd).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.testTag("emptyActivitiesPrompt"),
                text = stringResource(R.string.no_activities_scheduled),
            )
          }
        } else {
          val isEditable = !isReadOnly
          val buttonType = if (isEditable) ButtonType.DELETE else ButtonType.NOTHING
          LazyColumn(
              modifier = Modifier.padding(pd).fillMaxWidth().testTag("lazyColumn"),
              verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
          ) {
            item {
              if (drafts.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.drafts),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp))
              }
            }
            drafts.forEach { activity ->
              item {
                if (selectedFilters.isEmpty() || activity.activityType in selectedFilters) {
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
            }
            item {
              if (final.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.final_activities),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 10.dp))
              }
            }
            final.forEach { activity ->
              item {
                if (selectedFilters.isEmpty() || activity.activityType in selectedFilters) {
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
            }
            item { EstimatedPriceBox(totalEstimatedPrice) }
          }

          if (showDialog) {
            DeleteActivityAlertDialog(
                onDismissRequest = { showDialog = false },
                activityToDelete = activityToDelete,
                tripsViewModel = tripsViewModel,
                confirmButtonOnClick = {
                  showDialog = false
                  final = final.filter { it != activityToDelete }
                  drafts = drafts.filter { it != activityToDelete }
                })
          }
        }
      })
}

/**
 * Composable that contains the total price of all activities and displays it in a box at the bottom
 * of the screen.
 *
 * @param price The total estimated price of all activities.
 */
@Composable
fun EstimatedPriceBox(price: Double) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .padding(16.dp)
              .background(
                  color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                  shape = RoundedCornerShape(8.dp))
              .padding(16.dp)
              .testTag("totalEstimatedPriceBox"),
      contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.total_price, price),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center))
      }
}
