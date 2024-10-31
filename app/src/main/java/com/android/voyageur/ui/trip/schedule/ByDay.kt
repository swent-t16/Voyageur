package com.android.voyageur.ui.trip.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TRIP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.overview.toDateString
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ByDayScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
) {
  val trip =
      tripsViewModel.selectedTrip.collectAsState().value
          ?: return Text(text = "No ToDo selected. Should not happen", color = Color.Red)
  Scaffold(
      // TODO: Final implementation of ByDayScreen
      modifier = Modifier.testTag("byDayScreen"),
      topBar = {
          TopBarWithImage(selectedTrip = trip, navigationActions = navigationActions)
         /**   TopAppBar(
                modifier = Modifier.testTag("topBar"),
                title = { Text("Schedule ByDay:") },
                navigationIcon = {
                  IconButton(
                      modifier = Modifier.testTag("backToOverviewButton"),
                      onClick = { navigationActions.navigateTo(Screen.OVERVIEW) }) {
                        Icon(imageVector = Icons.Outlined.Home, contentDescription = "Home")
                      }
                })
         */
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TRIP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        Text(
            modifier = Modifier
                .padding(pd)
                .testTag("emptyByDayPrompt"),
            text =
                "You're viewing the ByDay screen for ${trip.name}, but it's not implemented yet.")
      })
}

