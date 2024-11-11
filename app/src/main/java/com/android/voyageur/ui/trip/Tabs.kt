package com.android.voyageur.ui.trip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.trip.activities.ActivitiesScreen
import com.android.voyageur.ui.trip.schedule.ScheduleScreen
import com.android.voyageur.ui.trip.schedule.TopBarWithImage
import com.android.voyageur.ui.trip.settings.SettingsScreen

@Composable
fun TopTabs(tripsViewModel: TripsViewModel, navigationActions: NavigationActions) {
  // Define tab items
  val tabs = listOf("Schedule", "Activities", "Settings")

  val trip =
      tripsViewModel.selectedTrip.value
          ?: return Text(text = "No trip selected. Should not happen", color = Color.Red)

  // Column for top tabs and content
  Column(modifier = Modifier.testTag("topTabs")) {
    TopBarWithImage(trip, navigationActions)
    // TabRow composable for creating top tabs
    TabRow(
        selectedTabIndex = navigationActions.currentTabIndexForTrip,
        modifier = Modifier.fillMaxWidth().testTag("tabRow"),
    ) {
      // Create each tab with a Tab composable
      tabs.forEachIndexed { index, title ->
        Tab(
            selected = navigationActions.currentTabIndexForTrip == index,
            onClick = { navigationActions.currentTabIndexForTrip = index },
            text = { Text(title) })
      }
    }

    // Display content based on selected tab
    when (navigationActions.currentTabIndexForTrip) {
      0 -> {
        ScheduleScreen(trip, navigationActions)
      }
      1 -> {
        ActivitiesScreen(trip, navigationActions)
      }
      2 -> {
        SettingsScreen(
            trip,
            navigationActions,
            tripsViewModel = tripsViewModel,
            onUpdate = {
              navigationActions.currentTabIndexForTrip = 0
              navigationActions.currentTabIndexForTrip = 2
            })
      }
    }
  }
}
