package com.android.voyageur.ui.trip.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.overview.AddTripScreen

/**
 * Represents the Settings screen of the app, where the user can edit trip details.
 *
 * This composable renders a `Scaffold` that contains a `BottomNavigationMenu` and a content area
 * where an `AddTripScreen` is displayed in edit mode, allowing the user to modify the details of an
 * existing trip.
 *
 * The screen utilizes several ViewModels (`TripsViewModel`, `UserViewModel`, `PlacesViewModel`) and
 * navigation actions (`NavigationActions`) to manage the user interface and handle navigation
 * within the app.
 *
 * @param trip The trip object that is being edited on this screen.
 * @param navigationActions A set of actions that manage navigation throughout the app.
 * @param tripsViewModel The ViewModel that manages trip data and operations.
 * @param userViewModel The ViewModel that manages user data and operations.
 * @param placesViewModel The ViewModel that handles place data for the trip.
 * @param onUpdate A lambda function to be called when the trip is updated. This allows the caller
 *   to react to changes made on the screen (optional, default is empty).
 */
@Composable
fun SettingsScreen(
    trip: Trip,
    navigationActions: NavigationActions,
    tripsViewModel: TripsViewModel,
    userViewModel: UserViewModel,
    placesViewModel: PlacesViewModel,
    onUpdate: () -> Unit = {}
) {

  Scaffold(
      modifier = Modifier.testTag("settingsScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel,
            tripsViewModel)
      },
      content = { pd ->
        Box(modifier = Modifier.padding(pd)) {
          AddTripScreen(
              tripsViewModel,
              navigationActions,
              isEditMode = true,
              onUpdate = onUpdate,
              userViewModel = userViewModel,
              placesViewModel = placesViewModel)
        }
      })
}
