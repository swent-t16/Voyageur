package com.android.voyageur.ui.trip.activities

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.voyageur.R
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.components.SearchBar
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Calendar
import java.util.Date

@Composable
fun ActivitiesMapScreen(tripsViewModel: TripsViewModel) {
  var activities by remember { mutableStateOf(tripsViewModel.getActivitiesForSelectedTrip()) }
  val cameraPositionState = rememberCameraPositionState {
    position =
        com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            LatLng(
                activities.firstOrNull()?.location?.lat ?: 0.0,
                activities.firstOrNull()?.location?.lng ?: 0.0),
            10f)
  }

  var showFilterDialog by remember { mutableStateOf(false) }
  var selectedFilters by remember { mutableStateOf(setOf<String>()) }

  Column {
    // Search Bar with Filter Button
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
          SearchBar(
              placeholderId = R.string.search_activities,
              onQueryChange = { query ->
                activities =
                    tripsViewModel.getActivitiesForSelectedTrip().filter {
                      it.title.contains(query, ignoreCase = true) ||
                          it.description.contains(query, ignoreCase = true)
                    }
              },
              modifier = Modifier.weight(1f))
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
          GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
            activities.forEach { activity ->
              Marker(
                  state =
                      MarkerState(position = LatLng(activity.location.lat, activity.location.lng)),
                  title = activity.title,
                  snippet = activity.description)
            }
          }
        }
  }

  // Filter Dialog
  if (showFilterDialog) {
    FilterDialog(
        onFilterByDay = { selectedDate ->
          activities =
              tripsViewModel.getActivitiesForSelectedTrip().filter {
                val activityDate = it.startTime.toDate()
                activityDate.year == selectedDate.year &&
                    activityDate.month == selectedDate.month &&
                    activityDate.date == selectedDate.date
              }
          showFilterDialog = false
        },
        onFilterByType = { filters ->
          selectedFilters = filters
          activities =
              tripsViewModel.getActivitiesForSelectedTrip().filter {
                filters.isEmpty() || filters.contains(it.activityType.toString())
              }
          showFilterDialog = false
        },
        onClearFilters = {
          activities = tripsViewModel.getActivitiesForSelectedTrip()
          selectedFilters = emptySet()
          showFilterDialog = false
        },
        onDismiss = { showFilterDialog = false })
  }
}

@Composable
fun FilterDialog(
    onFilterByDay: (Date) -> Unit,
    onFilterByType: (Set<String>) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
  val activityTypes = ActivityType.entries.map { it.toString() }
  val context = LocalContext.current
  var selectedDate by remember { mutableStateOf<Date?>(null) }
  var tempSelectedFilters by remember { mutableStateOf(setOf<String>()) }
  var showTypeOptions by remember {
    mutableStateOf(false)
  } // Toggles between main and type options dialog

  if (showTypeOptions) {
    // Secondary dialog for activity type options
    AlertDialog(
        onDismissRequest = { showTypeOptions = false },
        title = { Text("Select Activity Types") },
        text = {
          Column {
            activityTypes.forEach { type ->
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    val isChecked = tempSelectedFilters.contains(type)
                    Checkbox(
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
              onClick = {
                onFilterByType(tempSelectedFilters)
                showTypeOptions = false
              }) {
                Text("Apply")
              }
        },
        dismissButton = { TextButton(onClick = { showTypeOptions = false }) { Text("Cancel") } })
  } else {
    // Main dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Activities") },
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
                modifier = Modifier.fillMaxWidth()) {
                  Text("Filter by Date")
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter by Type Button
            Button(
                onClick = { showTypeOptions = true }, // Opens the secondary dialog
                modifier = Modifier.fillMaxWidth()) {
                  Text("Filter by Type")
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Clear Filters Button
            Button(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)) {
                  Text("Clear Filters", color = MaterialTheme.colorScheme.onError)
                }
          }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
  }
}
