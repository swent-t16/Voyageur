package com.android.voyageur.ui.overview

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
import com.android.voyageur.ui.navigation.*
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
) {
  val trips by tripsViewModel.trips.collectAsState()
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Calculate responsive padding based on screen width
  val horizontalPadding = (screenWidth * 0.04f).coerceAtLeast(8.dp)

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
                  items(sortedTrips) { trip ->
                    TripItem(
                        tripsViewModel = tripsViewModel,
                        trip = trip,
                        navigationActions = navigationActions)
                  }
                }
          }
        }
      }
}

@Composable
fun TripItem(tripsViewModel: TripsViewModel, trip: Trip, navigationActions: NavigationActions) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Calculate responsive card width and height
  val cardWidth = (screenWidth - 32.dp).coerceAtMost(400.dp)
  val cardHeight = (cardWidth * 0.6f).coerceAtLeast(180.dp)
  val imageWidth = (cardWidth * 0.35f).coerceAtLeast(100.dp)
  val dateRange = trip.startDate.toDateString() + " - " + trip.endDate.toDateString()

  Card(
      onClick = {
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))

                    DisplayParticipants(trip)
                  }
            }
      }
}

@Composable
fun DisplayParticipants(trip: Trip) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val avatarSize = (screenWidth * 0.06f).coerceIn(24.dp, 32.dp)
  val numberOfParticipants = trip.participants.size
  val numberToString = generateParticipantString(numberOfParticipants)

  Column(
      modifier = Modifier.fillMaxHeight().padding(start = 0.dp, end = 0.dp, top = 8.dp),
      verticalArrangement = Arrangement.Bottom) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            text = numberToString,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
              if (numberOfParticipants > 0) {
                trip.participants.take(4).forEach { participant ->
                  Surface(
                      modifier = Modifier.size(avatarSize).testTag("participantAvatar"),
                      shape = CircleShape,
                      color = MaterialTheme.colorScheme.secondaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                          Text(
                              text = participant.first().uppercase(),
                              style = MaterialTheme.typography.bodySmall,
                              color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                      }
                }

                if (trip.participants.size > 4) {
                  Text(
                      text = "and ${trip.participants.size - 4} more",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
