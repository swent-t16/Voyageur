package com.android.voyageur.ui.trip.activities

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.activity.Activity
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
