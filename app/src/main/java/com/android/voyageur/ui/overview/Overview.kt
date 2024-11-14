package com.android.voyageur.ui.overview

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {
  val trips by tripsViewModel.trips.collectAsState()
  LaunchedEffect(trips) {
    userViewModel.getUsersByIds(
        trips.map { it.participants }.flatten(), { userViewModel._allParticipants.value = it })
  }
  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = { navigationActions.navigateTo(Screen.ADD_TRIP) },
            modifier = Modifier.testTag("createTripButton")) {
              Icon(
                  Icons.Outlined.Add,
                  "Floating action button",
                  modifier = Modifier.testTag("addIcon"))
            }
      },
      modifier = Modifier.testTag("overviewScreen"),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Your trips",
                  style = MaterialTheme.typography.headlineMedium,
                  color = MaterialTheme.colorScheme.onSurface)
            },
            modifier = Modifier.testTag("topBarTitle"),
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface))
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).testTag("overviewColumn").fillMaxSize(),
        ) {
          if (trips.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text(
                  "You have no trips yet. Schedule one.",
                  modifier = Modifier.testTag("emptyTripPrompt"),
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurface)
            }
          } else {
            val sortedTrips = trips.sortedBy { it.startDate }
            LazyColumn(
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().testTag("lazyColumn")) {
                  sortedTrips.forEach { trip ->
                    item {
                      TripItem(
                          tripsViewModel = tripsViewModel,
                          trip = trip,
                          navigationActions = navigationActions,
                          userViewModel = userViewModel)
                      Spacer(modifier = Modifier.height(10.dp))
                    }
                  }
                }
          }
        }
      }
}

@Composable
fun TripItem(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {
  // TODO: add a clickable once we implement the Schedule screens
  val dateRange = trip.startDate.toDateString() + "-" + trip.endDate.toDateString()
  val themeColor = MaterialTheme.colorScheme.onSurface
  Card(
      onClick = {
        // When opening a trip, navigate to the Schedule screen, with the daily view enabled
        navigationActions.getNavigationState().currentTabIndexForTrip = 0
        navigationActions.getNavigationState().isDailyViewSelected = true
        navigationActions.navigateTo(Screen.TOP_TABS)
        tripsViewModel.selectTrip(trip)
      },
      modifier =
          Modifier.width(cardWidth).height(cardHeight).padding(vertical = 4.dp).testTag("cardItem"),
      shape = RoundedCornerShape(16.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxSize().testTag("cardRow"),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
              Box(modifier = Modifier.width(imageWidth).fillMaxHeight()) {
                if (trip.imageUri.isNotEmpty()) {
                  Image(
                      painter = rememberAsyncImagePainter(model = trip.imageUri),
                      contentDescription = "Selected image",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize().testTag("tripImage"))
                } else {
                  Image(
                      painter = painterResource(id = R.drawable.default_trip_image),
                      contentDescription = "Trip image overview",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize().testTag("defaultTripImage"))
                }
              }

              Column(
                  modifier = Modifier.fillMaxSize().padding(16.dp).weight(2f),
                  verticalArrangement = Arrangement.Top) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = trip.name,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        text = dateRange,
                        textAlign = TextAlign.Start,
                        style =
                            TextStyle(
                                fontSize = 10.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight(500),
                                color = themeColor,
                                letterSpacing = 0.1.sp,
                            ))
                    DisplayParticipants(trip, userViewModel)
                  }
            }
      }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DisplayParticipants(trip: Trip, userViewModel: UserViewModel) {
  val numberOfParticipants = trip.participants.size - 1
  val numberToString = generateParticipantString(numberOfParticipants)

  Column(
      modifier = Modifier.fillMaxHeight().padding(start = 0.dp, end = 0.dp, top = 8.dp),
      verticalArrangement = Arrangement.Bottom, // Align top to bottom
  ) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        text = numberToString,
        textAlign = TextAlign.Start,
        style =
            TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight(500),
                color = themeColor,
                letterSpacing = 0.1.sp,
            ))
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp) // Space between avatars
        ) {
          // Display participants (limit to 5 avatars max for space reasons)
          if (numberOfParticipants > 0) {
            trip.participants
                .filter { it != Firebase.auth.uid.orEmpty() }
                .take(4)
                .forEach { participant ->
                  // TODO: Replace Box with user avatars once they are designed
                  Box(
                      modifier =
                          Modifier.size(30.dp) // Set size for the avatar circle
                              .testTag("participantAvatar")
                              .background(
                                  Color.Gray, shape = RoundedCornerShape(50)), // Circular shape
                      contentAlignment = Alignment.Center) {
                        userViewModel._allParticipants.value
                            .find { it.id == participant }
                            ?.name
                            ?.first()
                            ?.let {
                              Text(text = it.uppercaseChar().toString(), color = Color.White)
                            }
                      }
                }
            if (trip.participants.size > 4) {
              Text(
                  text = "and ${trip.participants.size - 4} more",
                  fontSize = 8.sp,
                  color = Color.Gray,
                  modifier =
                      Modifier.align(Alignment.CenterVertically)
                          .testTag("additionalParticipantsText"))
            }
          }
        }
  }
}

fun Timestamp.toDateString(): String {
  val sdf = java.text.SimpleDateFormat("MMM dd yyyy", java.util.Locale.getDefault())
  return sdf.format(this.toDate())
}

fun generateParticipantString(numberOfParticipants: Int): String {
  return when (numberOfParticipants) {
    0 -> "No participants."
    1 -> "1 Participant:"
    else -> "$numberOfParticipants Participants:"
  }
}
