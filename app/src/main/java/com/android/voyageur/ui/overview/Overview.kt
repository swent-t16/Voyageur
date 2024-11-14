package com.android.voyageur.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp

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
              Icon(
                  Icons.Outlined.Add,
                  "Floating action button",
                  modifier = Modifier.testTag("addIcon"))
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
            modifier = Modifier.padding(pd).testTag("overviewColumn"),
        ) {
          if (trips.isEmpty()) {
            Box(
                modifier = Modifier.padding(pd).fillMaxSize(),
                contentAlignment = Alignment.Center) {
                  Text(
                      modifier = Modifier.testTag("emptyTripPrompt"),
                      text = "You have no trips yet.",
                  )
                }
          } else {
            val sortedTrips = trips.sortedBy { trip -> trip.startDate }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally, // Center items horizontally
                modifier = Modifier.fillMaxSize().testTag("lazyColumn")) {
                  sortedTrips.forEach { trip ->
                    item {
                      TripItem(
                          tripsViewModel = tripsViewModel,
                          trip = trip,
                          navigationActions = navigationActions)
                      Spacer(modifier = Modifier.height(10.dp))
                    }
                  }
                }
          }
        }
      })
}

@Composable
fun TripItem(tripsViewModel: TripsViewModel, trip: Trip, navigationActions: NavigationActions) {
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
          Modifier.width(353.dp)
              .height(228.dp)
              .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
              .testTag("cardItem"),
      shape = RoundedCornerShape(16.dp),
      content = {
        Row(
            modifier = Modifier.fillMaxSize().testTag("cardRow"),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
              // modifier.weight(1f) is used here to set the image for 1/3 of the card
              if (trip.imageUri.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = trip.imageUri),
                    contentDescription = "Selected image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(120.dp).height(217.dp).testTag("tripImage"))
              } else {
                Image(
                    painter = painterResource(id = R.drawable.default_trip_image),
                    contentDescription = "Trip image overview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(120.dp).height(217.dp).testTag("defaultTripImage"))
              } // modifier.weight(2f) is used here to set the column to 2/3 of the card
              Column(
                  modifier = Modifier.fillMaxSize().padding(16.dp).weight(2f),
                  verticalArrangement = Arrangement.Top) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = trip.name,
                        textAlign = TextAlign.Start,
                        style =
                            TextStyle(
                                fontSize = 23.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight(500),
                                color = themeColor,
                                letterSpacing = 0.23.sp))
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, top = 4.dp),
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
                    DisplayParticipants(trip)
                  }
            }
      })
}

@Composable
fun DisplayParticipants(trip: Trip) {
  val numberOfParticipants = trip.participants.size
  val numberToString = generateParticipantString(numberOfParticipants)
  val themeColor = MaterialTheme.colorScheme.onSurface
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
            trip.participants.take(4).forEach { participant ->
              // TODO: Replace Box with user avatars once they are designed
              Box(
                  modifier =
                      Modifier.size(30.dp) // Set size for the avatar circle
                          .testTag("participantAvatar")
                          .background(Color.Gray, shape = RoundedCornerShape(50)), // Circular shape
                  contentAlignment = Alignment.Center) {
                    Text(text = participant.first().uppercaseChar().toString(), color = Color.White)
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

// Helper function to convert Timestamp to String format.
fun Timestamp.toDateString(): String {
  val sdf = java.text.SimpleDateFormat("MMM dd yyyy.", java.util.Locale.getDefault())
  return sdf.format(this.toDate())
}

// Helper function to generate the correct string
fun generateParticipantString(numberOfParticipants: Int): String {
  return if (numberOfParticipants == 0) {
    "No participants."
  } else {
    if (numberOfParticipants == 1) {
      "$numberOfParticipants Participant:"
    } else {
      "$numberOfParticipants Participants:"
    }
  }
}
