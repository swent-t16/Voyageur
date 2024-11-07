package com.android.voyageur.ui.trip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.trip.activities.ActivitiesScreen
import com.android.voyageur.ui.trip.activities.SAMPLE_ACTIVITIES
import com.android.voyageur.ui.trip.schedule.ByDayScreen
import com.android.voyageur.ui.trip.schedule.TopBarWithImage
import com.android.voyageur.ui.trip.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTabs(tripsViewModel: TripsViewModel, navigationActions: NavigationActions) {
  // Define tab items
  val tabs = listOf("Schedule", "Activities", "Settings")

  // Remember the currently selected tab index
  var selectedTabIndex by remember { mutableIntStateOf(0) }

  val trip =
      tripsViewModel.selectedTrip.value
          ?: return Text(text = "No trip selected. Should not happen", color = Color.Red)

  // Column for top tabs and content
  Column(modifier = Modifier.testTag("topTabs")) {
    TopBarWithImage(trip, navigationActions)
    // TabRow composable for creating top tabs
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth().testTag("tabRow"),
    ) {
      // Create each tab with a Tab composable
      tabs.forEachIndexed { index, title ->
        Tab(
            selected = selectedTabIndex == index,
            onClick = { selectedTabIndex = index },
            text = { Text(title) })
      }
    }

    // Display content based on selected tab
    when (selectedTabIndex) {
      0 -> ByDayScreen(trip, navigationActions)
      1 -> {
        trip.activities = SAMPLE_ACTIVITIES
        ActivitiesScreen(trip, navigationActions)
      }
      2 ->
          SettingsScreen(
              trip,
              navigationActions,
              tripsViewModel = tripsViewModel,
              onUpdate = {
                selectedTabIndex = 0
                selectedTabIndex = 2
              })
    }
  }
}
