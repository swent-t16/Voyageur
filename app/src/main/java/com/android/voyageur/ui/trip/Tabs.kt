package com.android.voyageur.ui.trip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.trip.activities.ActivitiesScreen
import com.android.voyageur.ui.trip.photos.PhotosScreen
import com.android.voyageur.ui.trip.schedule.ScheduleScreen
import com.android.voyageur.ui.trip.schedule.TopBarWithImageAndText
import com.android.voyageur.ui.trip.schedule.toDateWithYearString
import com.android.voyageur.ui.trip.schedule.toDateWithoutYearString
import com.android.voyageur.ui.trip.settings.SettingsScreen

@Composable
fun TopTabs(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    placesViewModel: PlacesViewModel
) {
  // Define tab items
  val tabs = listOf("Schedule", "Activities", "Photos", "Settings")

  // Collect selectedTrip as state to avoid calling .value directly in composition
  val trip by tripsViewModel.selectedTrip.collectAsState()

  // Check if the selected trip is null and navigate to "Overview" if true
  if (trip == null) {
    navigationActions.navigateTo(Route.OVERVIEW)
    return
  }
  // Define selectedTrip variable with trip value (trip!!)
  val selectedTrip = trip!!

  // Column for top tabs and content
  Column(modifier = Modifier.testTag("topTabs")) {
    TopBarWithImageAndText(
        selectedTrip,
        navigationActions,
        selectedTrip.name,
        selectedTrip.startDate.toDateWithoutYearString() +
            " - " +
            selectedTrip.endDate.toDateWithYearString())
    // TabRow composable for creating top tabs
    TabRow(
        selectedTabIndex = navigationActions.getNavigationState().currentTabIndexForTrip,
        modifier = Modifier.fillMaxWidth().testTag("tabRow"),
    ) {
      // Create each tab with a Tab composable
      tabs.forEachIndexed { index, title ->
        Tab(
            selected = navigationActions.getNavigationState().currentTabIndexForTrip == index,
            onClick = { navigationActions.getNavigationState().currentTabIndexForTrip = index },
            text = { Text(title) })
      }
    }

    // Display content based on selected tab
    when (navigationActions.getNavigationState().currentTabIndexForTrip) {
      0 -> ScheduleScreen(tripsViewModel, selectedTrip, navigationActions, userViewModel)
      1 -> ActivitiesScreen(navigationActions, userViewModel, tripsViewModel)
      2 -> PhotosScreen(tripsViewModel, navigationActions, userViewModel)
      3 ->
          SettingsScreen(
              selectedTrip,
              navigationActions,
              tripsViewModel = tripsViewModel,
              userViewModel = userViewModel,
              placesViewModel = placesViewModel,
              onUpdate = {
                navigationActions.getNavigationState().currentTabIndexForTrip = 0
                navigationActions.getNavigationState().currentTabIndexForTrip = 3
              })
    }
  }
}
