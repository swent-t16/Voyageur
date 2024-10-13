package com.android.voyageur.ui.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
) {
  val trips by tripsViewModel.trips.collectAsState()

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = { navigationActions.navigateTo(Screen.ADD_TRIP) },
            modifier = Modifier.testTag("createTripButton")) {
              Icon(Icons.Outlined.Add, "Floating action button")
            }
      },
      modifier = Modifier.testTag("overviewScreen"),
      topBar = {
        TopAppBar(title = { Text(text = "Your trips") }, modifier = Modifier.testTag("topBarTitle"))
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        Column(
            modifier = Modifier.padding(pd),
        ) {
          if (trips.isEmpty()) {
            Text(
                "You have no trips yet. Schedule one.",
                modifier = Modifier.testTag("emptyTripPrompt"))
          } else {
            val sortedTrips = trips.sortedBy { trip -> trip.startDate }
            LazyColumn(modifier = Modifier.testTag("lazyColumn")) {
              sortedTrips.forEach { trip ->
                item {
                  TripItem(trip = trip)
                  Spacer(modifier = Modifier.height(16.dp))
                }
              }
            }
          }
        }
      })
}

@Composable
fun TripItem(trip: Trip) {
  // TODO: add a clickable once we implement the Schedule screens
  Card(
      modifier = Modifier.fillMaxSize().testTag("cardItem"),
      content = {
        Text(
            text = trip.name,
            modifier = Modifier.padding(16.dp),
            fontSize = 20.sp,
            textAlign = TextAlign.Center)
      },
  )
}
