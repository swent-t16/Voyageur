package com.android.voyageur.ui.trip.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen

/**
 * Composable that displays a button that navigates to the Add Activity screen.
 *
 * @param navigationActions: The navigation actions.
 */
@Composable
fun AddActivityButton(navigationActions: NavigationActions) {
  FloatingActionButton(
      onClick = { navigationActions.navigateTo(Screen.ADD_ACTIVITY) },
      modifier = Modifier.testTag("createActivityButton")) {
        Icon(Icons.Outlined.Add, "Floating action button", modifier = Modifier.testTag("addIcon"))
      }
}

/**
 * Composable that displays an alert dialog to confirm the deletion of an activity.
 *
 * @param onDismissRequest: Callback to dismiss the dialog.
 * @param activityToDelete: The activity to delete.
 * @param tripsViewModel: The view model for the trips.
 * @param confirmButtonOnClick: Callback to confirm the deletion.
 */
@Composable
fun DeleteActivityAlertDialog(
    onDismissRequest: () -> Unit,
    activityToDelete: Activity?,
    tripsViewModel: TripsViewModel,
    confirmButtonOnClick: () -> Unit,
) {

  AlertDialog(
      modifier = Modifier.testTag("deleteActivityAlertDialog"),
      onDismissRequest = onDismissRequest,
      title = { Text("Delete Activity") },
      text = { Text("Are you sure you want to delete this activity?") },
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag("confirmDeleteButton"),
            onClick = {
              confirmButtonOnClick()
              // Perform the deletion
              activityToDelete?.let { activity -> tripsViewModel.removeActivityFromTrip(activity) }
            }) {
              Text("Delete")
            }
      },
      dismissButton = { TextButton(onClick = { onDismissRequest() }) { Text("Cancel") } })
}
/**
 * A dialog for filtering activities by type. Displays a list of available activity types with
 * checkboxes to toggle filters.
 *
 * @param selectedFilters A set of currently selected [ActivityType] filters.
 * @param onFilterChanged Callback invoked when a filter checkbox is checked or unchecked.
 * @param onDismiss Callback invoked to close the dialog.
 */
@Composable
fun FilterDialog(
    selectedFilters: Set<ActivityType>,
    onFilterChanged: (ActivityType, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = { onDismiss() },
      modifier = Modifier.testTag("filterActivityAlertDialog"),
      title = {
        Text(
            text = stringResource(R.string.filter_activity_type),
            style = MaterialTheme.typography.titleMedium)
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              ActivityType.entries.forEach { type ->
                // A row represents the activity type and a checkbox to validate the filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      Text(
                          text = type.name,
                      )
                      Checkbox(
                          modifier = Modifier.testTag("typeCheckBox_${type.name}"),
                          checked = selectedFilters.contains(type),
                          onCheckedChange = { isChecked -> onFilterChanged(type, isChecked) })
                    }
              }
            }
      },
      // Confirm button which closes the dialog
      confirmButton = {
        TextButton(onClick = { onDismiss() }, modifier = Modifier.testTag("confirmButtonDialog")) {
          Text(stringResource(R.string.confirm_text))
        }
      })
}
