package com.android.voyageur.ui.trip.activities

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    trip: Trip,
    navigationActions: NavigationActions,
) {
  //  val trip =
  //      tripsViewModel.selectedTrip.collectAsState().value
  //          ?: return Text(text = "No ToDo selected. Should not happen", color = Color.Red)

  Scaffold(
      // TODO: Final implementation of ActivitiesScreen
      modifier = Modifier.testTag("activitiesScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        Text(
            modifier = Modifier.padding(pd).testTag("emptyActivitiesPrompt"),
            text =
                "You're viewing the Activities screen for ${trip.name}, but it's not implemented yet.")
      })
}
