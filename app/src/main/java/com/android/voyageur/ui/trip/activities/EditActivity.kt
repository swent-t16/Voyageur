package com.android.voyageur.ui.trip.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.trip.AddActivityScreen

/**
 * A composable function that provides the UI for editing an existing activity in a trip.
 *
 * This screen is built on top of the `AddActivityScreen` composable and pre-fills the form with the
 * details of the currently selected activity.
 *
 * @param navigationActions Provides navigation actions for navigating between screens in the app.
 * @param tripsViewModel The `TripsViewModel` instance used to manage trip-related data, including
 *   the selected activity.
 * @param placesViewModel The `PlacesViewModel` instance used to provide location-related data for
 *   the activity.
 *
 * ## Behavior
 * - The composable observes the `selectedActivity` state from the `TripsViewModel` and passes the
 *   selected activity as an `existingActivity` to the `AddActivityScreen` for editing.
 * - The screen uses a `Scaffold` for consistent Material3 UI layout, with the main content
 *   displayed inside the scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityScreen(
    navigationActions: NavigationActions,
    tripsViewModel: TripsViewModel,
    placesViewModel: PlacesViewModel
) {
  // Collect the currently selected activity from the TripsViewModel state
  val activity by tripsViewModel.selectedActivity.collectAsState()
  val selectedActivity = activity!!

  Scaffold(
      modifier = Modifier.testTag("editActivityScreen"),
      content = { pd ->
        Box(modifier = Modifier.padding(pd)) {
          // Display the AddActivityScreen pre-filled with the selected activity
          AddActivityScreen(
              tripsViewModel,
              navigationActions,
              placesViewModel,
              existingActivity = selectedActivity)
        }
      })
}
