package com.android.voyageur.ui.trip.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.overview.AddTripScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    trip: Trip,
    navigationActions: NavigationActions,
    tripsViewModel: TripsViewModel,
    userViewModel: UserViewModel,
    onUpdate: () -> Unit = {}
) {

  Scaffold(
      // TODO: Final implementation of SettingsScreen
      modifier = Modifier.testTag("settingsScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel)
      },
      content = { pd ->
        Box(modifier = Modifier.padding(pd)) {
          AddTripScreen(
              tripsViewModel,
              navigationActions,
              isEditMode = true,
              onUpdate = onUpdate,
              userViewModel = userViewModel)
        }
      })
}
