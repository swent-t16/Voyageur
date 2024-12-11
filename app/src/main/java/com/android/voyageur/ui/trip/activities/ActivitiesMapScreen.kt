package com.android.voyageur.ui.trip.activities

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.components.SearchBar
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.*

// Default location for Lausanne, Switzerland
val LAUSANNE_COORDINATES = LatLng(46.519962, 6.633597)

/**
 * A screen that displays a map of activities for the selected trip. Users can search for activities
 * and filter them by date and type.
 *
 * @param tripsViewModel The `TripsViewModel` used to manage trip-related data and logic.
 * @param onActivitiesChanged callback only used for testing to notify when activities have changed.
 *   As we cannot put testTags on google map markers
 */
@Composable
fun ActivitiesMapTab(
    tripsViewModel: TripsViewModel,
    onActivitiesChanged: (List<Activity>) -> Unit = {}
) {
  var activities by remember { mutableStateOf(tripsViewModel.getActivitiesForSelectedTrip()) }
  val cameraPositionState = rememberCameraPositionState {
    position =
        com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            LatLng(
                activities.firstOrNull()?.location?.lat ?: LAUSANNE_COORDINATES.latitude,
                activities.firstOrNull()?.location?.lng ?: LAUSANNE_COORDINATES.longitude),
            10f)
  }

  var showFilterDialog by remember { mutableStateOf(false) }
  var selectedFilters by remember { mutableStateOf(setOf<String>()) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedDate by remember { mutableStateOf<Date?>(null) }

  /** Filters the activities based on the search query, selected filters, and selected date. */
  fun applyFilters() {
    activities =
        tripsViewModel.getActivitiesForSelectedTrip().filter { activity ->
          val matchesQuery =
              activity.title.contains(searchQuery, ignoreCase = true) ||
                  activity.description.contains(searchQuery, ignoreCase = true)
          val matchesFilters =
              selectedFilters.isEmpty() ||
                  selectedFilters.contains(activity.activityType.toString())
          val matchesDate =
              selectedDate == null ||
                  activity.startTime.toDate().let { date ->
                    date.year == selectedDate!!.year &&
                        date.month == selectedDate!!.month &&
                        date.date == selectedDate!!.date
                  }
          matchesQuery && matchesFilters && matchesDate
        }
    onActivitiesChanged(activities)
  }

  Column {
    // Search Bar with Filter Button
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
          SearchBar(
              placeholderId = R.string.search_activities,
              onQueryChange = { query ->
                searchQuery = query
                applyFilters()
              },
              modifier = Modifier.weight(1f).testTag("searchBar"))
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(
              modifier = Modifier.testTag("filterButton"), onClick = { showFilterDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.FilterAlt,
                    contentDescription = stringResource(R.string.filter_activities),
                    tint = MaterialTheme.colorScheme.primary)
              }
        }

    // Google Map
    Box(
        modifier =
            Modifier.fillMaxSize()
                .padding(16.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.secondary)) {
          GoogleMap(
              modifier = Modifier.fillMaxSize().testTag("GoogleMap"),
              cameraPositionState = cameraPositionState) {
                activities.forEach { activity ->
                  Marker(
                      state =
                          MarkerState(
                              position = LatLng(activity.location.lat, activity.location.lng)),
                      title = activity.title,
                      snippet = activity.description)
                }
              }
        }
  }

  // Filter Dialog
  if (showFilterDialog) {
    FilterDialog(
        onFilterByDay = { date ->
          selectedDate = date
          applyFilters()
        },
        onFilterByType = { filters ->
          selectedFilters = filters
          applyFilters()
        },
        onClearFilters = {
          activities = tripsViewModel.getActivitiesForSelectedTrip()
          selectedFilters = emptySet()
          searchQuery = ""
          selectedDate = null
          onActivitiesChanged(activities)
        },
        onDismiss = { showFilterDialog = false },
        selectedFilters = selectedFilters)
  }
}

/**
 * Dialog for filtering activities by date and type.
 *
 * @param onFilterByDay Callback for filtering activities by date.
 * @param onFilterByType Callback for filtering activities by type.
 * @param onClearFilters Callback for clearing all filters.
 * @param onDismiss Callback for dismissing the dialog.
 */
@Composable
fun FilterDialog(
    onFilterByDay: (Date) -> Unit,
    onFilterByType: (Set<String>) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit,
    selectedFilters: Set<String>
) {
  val activityTypes = ActivityType.entries.map { it.toString() }
  val context = LocalContext.current
  var selectedDate by remember { mutableStateOf<Date?>(null) }
  var tempSelectedFilters by remember { mutableStateOf(selectedFilters) }
  var showTypeOptions by remember { mutableStateOf(false) }

  if (showTypeOptions) {
    // Secondary dialog for activity type options
    AlertDialog(
        modifier = Modifier.testTag("typeFilterActivityAlertDialog"),
        onDismissRequest = { showTypeOptions = false },
        title = { Text(stringResource(R.string.select_activities_by_type)) },
        text = {
          Column {
            activityTypes.forEach { type ->
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    val isChecked = tempSelectedFilters.contains(type)
                    Checkbox(
                        modifier = Modifier.testTag("checkbox_$type"),
                        checked = isChecked,
                        onCheckedChange = { isSelected ->
                          tempSelectedFilters =
                              if (isSelected) {
                                tempSelectedFilters + type // Add type to set
                              } else {
                                tempSelectedFilters - type // Remove type from set
                              }
                        })
                    Text(text = type, modifier = Modifier.padding(start = 8.dp))
                  }
            }
          }
        },
        confirmButton = {
          TextButton(
              modifier = Modifier.testTag("applyTypeFilterButton"),
              onClick = {
                onFilterByType(tempSelectedFilters)
                showTypeOptions = false
              }) {
                Text(stringResource(R.string.apply))
              }
        },
        dismissButton = {
          TextButton(onClick = { showTypeOptions = false }) {
            Text(stringResource(R.string.cancel))
          }
        })
  } else {
    // Main dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_activities_button)) },
        text = {
          Column {
            // Filter by Date Button
            Button(
                onClick = {
                  val calendar = Calendar.getInstance()
                  DatePickerDialog(
                          context,
                          { _: DatePicker, year: Int, month: Int, day: Int ->
                            val date = Calendar.getInstance().apply { set(year, month, day) }.time
                            selectedDate = date
                            onFilterByDay(date)
                          },
                          calendar.get(Calendar.YEAR),
                          calendar.get(Calendar.MONTH),
                          calendar.get(Calendar.DAY_OF_MONTH))
                      .show()
                },
                modifier = Modifier.fillMaxWidth().testTag("mainFilterActivityAlertDialog")) {
                  Text(stringResource(R.string.filter_activities_by_date))
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter by Type Button
            Button(
                onClick = {
                  tempSelectedFilters = selectedFilters // Initialize with current selected filters
                  showTypeOptions = true
                },
                modifier = Modifier.fillMaxWidth().testTag("filterByTypeButton")) {
                  Text(stringResource(R.string.filter_activities_by_type))
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Clear Filters Button
            Button(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth().testTag("clearFiltersButton"),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)) {
                  Text(
                      stringResource(R.string.clear_filters),
                      color = MaterialTheme.colorScheme.onError)
                }
          }
        },
        confirmButton = {
          TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        })
  }
}
