package com.android.voyageur.ui.search

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.overview.DisplayParticipants

/**
 * DiscoverContent composable displays the trips that the user can discover. When the user presses
 * on View Details Button, they are re-directed to the TABS screen in Read-Only View, where they can
 * see the schedule of the featured trip, with disabled functions (such as adding a new activity).
 *
 * @param query the searchBar query that helps filter the field
 * @param tripsViewModel ViewModel that provides the trips to display.
 * @param userViewModel ViewModel that provides the user information.
 * @param navigationActions Handler for navigation between screens.
 * @param modifier Modifier to apply to this layout node.
 */
@Composable
fun DiscoverContent(
    query: String,
    tripsViewModel: TripsViewModel,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions,
    modifier: Modifier = Modifier
) {
  val userId = userViewModel.user.collectAsState().value?.id
  // Fetch trips
  tripsViewModel.getFeed(userId ?: "")
  val trips =
      tripsViewModel.feed.collectAsState().value.filter { trip ->
        trip.name.contains(query, ignoreCase = true)
      }
  val pagerState = rememberPagerState(pageCount = { trips.size })

  if (trips.isEmpty()) {
    NoTripsFound()
  } else {
    HorizontalPager(state = pagerState, modifier = modifier.fillMaxSize().testTag("pager")) { page
      ->
      TripCard(
          trip = trips[page],
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }
  }
}

/**
 * TripCard composable displays a card with the trip information.
 *
 * @param trip Trip to display.
 * @param userViewModel ViewModel that provides the user information.
 */
@Composable
fun TripCard(
    trip: Trip,
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {

  // Workaround to allow Toast in onSuccess function
  val context = LocalContext.current
  val copiedMsg = stringResource(R.string.trip_copied_message, trip.name)

  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
              .padding(16.dp)
              .testTag("tripCard_${trip.id}"),
      contentAlignment = Alignment.Center) {
        Column(
            modifier =
                Modifier.fillMaxWidth(0.9f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)) {
              // Trip Image
              if (trip.imageUri.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(trip.imageUri),
                    contentDescription = "Image of ${trip.name}",
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)))
              } else {
                Image(
                    painter = rememberAsyncImagePainter(model = R.drawable.default_trip_image),
                    contentDescription = "Trip Image",
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)))
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Trip Name
              Text(
                  text = trip.name,
                  textAlign = TextAlign.Center,
                  style = MaterialTheme.typography.headlineMedium,
                  modifier = Modifier.fillMaxWidth())

              Spacer(modifier = Modifier.height(8.dp))

              // Trip Description
              Text(
                  text = trip.description,
                  style = MaterialTheme.typography.bodyMedium,
                  maxLines = 3,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.fillMaxWidth())

              Spacer(modifier = Modifier.weight(1f))

              // Participants and View Details Button
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Bottom) {
                    DisplayParticipants(
                        trip = trip,
                        userViewModel = userViewModel,
                        modifier = Modifier.weight(1f),
                        arrangement = Arrangement.Bottom)
                    TextButton(
                        onClick = {
                          navigationActions.getNavigationState().currentTabIndexForTrip = 0
                          navigationActions.getNavigationState().isDailyViewSelected = true
                          navigationActions.getNavigationState().isReadOnlyView = true
                          navigationActions.navigateTo(Screen.TOP_TABS)
                          tripsViewModel.selectTrip(trip)
                        },
                        modifier = Modifier.testTag("viewTripDetailsButton")) {
                          Text(text = stringResource(R.string.view_details))
                        }

                    // Copy Button
                    IconButton(
                        onClick = {
                          tripsViewModel.selectTrip(trip)
                          tripsViewModel.copyTrip(userViewModel) {
                            Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                          }
                        },
                        modifier = Modifier.testTag("copyTripDetailsButton")) {
                          Icon(
                              imageVector = Icons.Default.ContentCopy,
                              contentDescription = stringResource(R.string.copy_trip_button),
                              tint = MaterialTheme.colorScheme.primary)
                        }
                  }
            }
      }
}

/** NoTripsFound composable displays a message when no trips are found. */
@Composable
fun NoTripsFound() {
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
              .padding(16.dp)
              .testTag("noTripsFound"),
      contentAlignment = Alignment.Center) {
        Column(
            modifier =
                Modifier.fillMaxWidth(0.9f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                  imageVector = Icons.Outlined.ImageSearch,
                  contentDescription = stringResource(R.string.no_trips_to_discover),
                  modifier = Modifier.size(48.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                  text = stringResource(R.string.no_trips_to_discover),
                  fontSize = 18.sp,
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                  text = stringResource(R.string.check_back_letter),
                  fontSize = 14.sp,
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.onSurface)
            }
      }
}
