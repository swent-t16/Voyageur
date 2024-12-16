package com.android.voyageur.ui.overview

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.components.NoResultsFound
import com.android.voyageur.ui.components.SearchBar
import com.android.voyageur.ui.formFields.UserIcon
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.util.TimeZone
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Composable function that renders the main overview screen of the app.
 *
 * This screen displays a list of trips and allows the user to add a new trip or navigate to other
 * parts of the app. If no trips are available, it displays an appropriate message.
 *
 * @param tripsViewModel The ViewModel containing the state and logic for handling trips.
 * @param navigationActions Actions to handle navigation between screens.
 * @param userViewModel The ViewModel containing the state and logic for user data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun OverviewScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
) {
  val unfilteredTrips by tripsViewModel.trips.collectAsState()
  val isLoadingUser by userViewModel.isLoading.collectAsState()
  val isLoadingTrip by tripsViewModel.isLoading.collectAsState()
  var isLoading = false
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available
  var searchQuery by remember { mutableStateOf("") }
  var showOnlyFavorites by remember { mutableStateOf(false) }
  val user by userViewModel.user.collectAsState()
  if (user == null) {
    // Don't compose the UI yet if the user is not loaded
    return
  }
  LaunchedEffect(user) {
    //   update favorite trips by removing deleted trips or trips that the user is no longer a
    // participant of
    val updatedFavoriteTrips =
        user!!.favoriteTrips.filter { tripId -> unfilteredTrips.any { trip -> trip.id == tripId } }
    if (updatedFavoriteTrips.size != user!!.favoriteTrips.size) {
      val updatedUser = user!!.copy(favoriteTrips = updatedFavoriteTrips)
      userViewModel.updateUser(updatedUser)
    }
  }

  val trips =
      if (showOnlyFavorites) unfilteredTrips.filter { user!!.favoriteTrips.contains(it.id) }
      else unfilteredTrips

  LaunchedEffect(isLoadingUser, isLoadingTrip) { isLoading = isLoadingUser || isLoadingTrip }

  LoadParticipantsEffect(trips, userViewModel)

  Scaffold(
      floatingActionButton = { AddTripFAB(isConnected, navigationActions) },
      modifier = Modifier.testTag("overviewScreen"),
      topBar = {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          SearchBar(
              placeholderId = R.string.overview_searchbar_placeholder,
              onQueryChange = { searchQuery = it },
              modifier =
                  Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                      .testTag("searchField")
                      .weight(1f))

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
              onClick = { showOnlyFavorites = !showOnlyFavorites },
              modifier = Modifier.testTag("favoriteFilterButton").padding(end = 8.dp)) {
                Icon(
                    imageVector =
                        if (showOnlyFavorites) Icons.Filled.Favorite
                        else Icons.Default.FavoriteBorder,
                    contentDescription =
                        if (showOnlyFavorites) stringResource(R.string.show_all_trips)
                        else stringResource(R.string.show_favorite_trips),
                    tint = MaterialTheme.colorScheme.onSurface)
              }
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel)
      },
      content = { pd ->
        OverviewContent(
            isLoading = isLoading,
            trips = trips,
            searchQuery = searchQuery,
            showOnlyFavorites = showOnlyFavorites,
            padding = pd,
            tripsViewModel = tripsViewModel,
            navigationActions = navigationActions,
            userViewModel = userViewModel,
            user = user!!)
      })
}

/**
 * Side effect composable that loads participant data when trips are updated.
 *
 * This composable monitors the trips list and updates the user contacts when the list changes,
 * ensuring that participant information is always current.
 *
 * @param trips The current list of trips to extract participants from
 * @param userViewModel The ViewModel containing user-related state and logic
 */
@Composable
private fun LoadParticipantsEffect(trips: List<Trip>, userViewModel: UserViewModel) {
  LaunchedEffect(trips) {
    if (trips.isNotEmpty()) {
      userViewModel.getUsersByIds(
          trips
              .map { it.participants + (userViewModel._user.value?.contacts ?: listOf()) }
              .flatten()
              .toSet()
              .toList()) {
            userViewModel._contacts.value = it
          }
    }
  }
}

/**
 * Composable that renders the Floating Action Button for adding new trips.
 *
 * This button is disabled when there's no internet connection and shows a toast message informing
 * the user about the connectivity requirement.
 *
 * @param isConnected Boolean indicating whether the device has internet connectivity
 * @param navigationActions Actions to handle navigation between screens
 */
@Composable
private fun AddTripFAB(isConnected: Boolean, navigationActions: NavigationActions) {
  val context = LocalContext.current
  FloatingActionButton(
      onClick = {
        if (isConnected) {
          navigationActions.navigateTo(Screen.ADD_TRIP)
        } else {
          Toast.makeText(context, R.string.notification_no_internet_text, Toast.LENGTH_SHORT).show()
        }
      },
      modifier = Modifier.testTag("createTripButton")) {
        Icon(
            Icons.Outlined.Add,
            stringResource(R.string.floating_button),
            modifier = Modifier.testTag("addIcon"))
      }
}

/**
 * Composable that renders the main content of the overview screen.
 *
 * Handles the display of loading indicator, empty state, and the list of trips based on the current
 * state of the app.
 *
 * @param isLoading Boolean indicating whether data is currently being loaded
 * @param trips List of all available trips
 * @param searchQuery Current search query for filtering trips
 * @param showOnlyFavorites Boolean indicating whether the user is viewing only favorite trips
 * @param padding PaddingValues to apply to the content
 * @param tripsViewModel ViewModel containing trips-related state and logic
 * @param navigationActions Actions to handle navigation between screens
 * @param userViewModel ViewModel containing user-related state and logic
 * @param user The current user data
 */
@Composable
private fun OverviewContent(
    isLoading: Boolean,
    trips: List<Trip>,
    searchQuery: String,
    showOnlyFavorites: Boolean,
    padding: PaddingValues,
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    user: User
) {
  if (isLoading) {
    CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
    return
  }

  Column(
      modifier = Modifier.padding(padding).testTag("overviewColumn"),
  ) {
    if (trips.isEmpty()) {
      EmptyTripsMessage(showOnlyFavorites)
    } else {
      TripsList(
          trips = trips,
          searchQuery = searchQuery,
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          user = user)
    }
  }
}

/**
 * Composable that displays a message when no trips are available.
 *
 * Shows a centered message prompting the user to create their first trip or indicating that no
 * favorite trips are available.
 *
 * @param showOnlyFavorites Boolean indicating whether the user is viewing only favorite trips
 */
@Composable
private fun EmptyTripsMessage(showOnlyFavorites: Boolean) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(
        modifier = Modifier.testTag("emptyTripPrompt"),
        text =
            if (!showOnlyFavorites) stringResource(R.string.empty_trip_prompt)
            else stringResource(R.string.no_favorite_trips),
    )
  }
}

/**
 * Composable that renders the list of trips.
 *
 * Displays a scrollable list of trip items, filtered according to the search query. Shows a "no
 * results" message when the search yields no matches.
 *
 * @param trips List of all available trips
 * @param searchQuery Current search query for filtering trips
 * @param tripsViewModel ViewModel containing trips-related state and logic
 * @param navigationActions Actions to handle navigation between screens
 * @param userViewModel ViewModel containing user-related state and logic
 * @param user The current user data
 */
@Composable
private fun TripsList(
    trips: List<Trip>,
    searchQuery: String,
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    user: User
) {
  val filteredTrips = filterTrips(trips, searchQuery)

  if (searchQuery.isNotEmpty() && filteredTrips.isEmpty()) {
    NoResultsFound(modifier = Modifier.testTag("noSearchResults"))
    return
  }

  LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxSize().testTag("lazyColumn")) {
        items(filteredTrips) { trip ->
          TripItem(
              tripsViewModel = tripsViewModel,
              trip = trip,
              navigationActions = navigationActions,
              userViewModel = userViewModel,
              user = user)
        }
      }
}

/**
 * Filters and sorts the list of trips based on a search query.
 *
 * If no search query is provided, returns all trips sorted by start date. If a search query exists,
 * returns trips whose names contain the query (case-insensitive), sorted by start date.
 *
 * @param trips List of trips to filter
 * @param searchQuery Search query to filter trips by
 * @return Filtered and sorted list of trips
 */
private fun filterTrips(trips: List<Trip>, searchQuery: String): List<Trip> {
  return if (searchQuery.isEmpty()) {
    trips.sortedByDescending { it.startDate }
  } else {
    trips
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .sortedByDescending { it.startDate }
  }
}

/**
 * Composable function that renders an individual trip item card in the overview screen.
 *
 * Each card displays the trip's name, date range, and participants. The user can expand the card to
 * see additional options such as adding the trip to the calendar or deleting it.
 *
 * @param tripsViewModel The ViewModel containing the state and logic for handling trips.
 * @param trip The trip data to display in this card.
 * @param navigationActions Actions to handle navigation between screens.
 * @param userViewModel The ViewModel containing the state and logic for user data.
 * @param user The current user data.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun TripItem(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    user: User
) {
  val dateRange = trip.startDate.toDateString() + " - " + trip.endDate.toDateString()
  val themeColor = MaterialTheme.colorScheme.onSurface
  var isExpanded by remember { mutableStateOf(false) }
  var showDialog by remember { mutableStateOf(false) }
  var leaveTrip by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available

  // Permission launcher to access calendar
  val requestPermissionLauncher =
      rememberLauncherForActivityResult(
          ActivityResultContracts.RequestPermission(),
      ) { isGranted: Boolean ->
        if (isGranted) {
          // Launch the calendar when permission is granted
          openGoogleCalendar(context, trip)
        } else {
          // Inform the user that the permission is required
          Toast.makeText(context, R.string.denied_calendar_permission, Toast.LENGTH_SHORT).show()
        }
      }
  Card(
      onClick = {
        // When opening a trip, navigate to the Schedule screen, with the daily view enabled
        navigationActions.getNavigationState().currentTabIndexForTrip = 0
        navigationActions.getNavigationState().isDailyViewSelected = true
        navigationActions.getNavigationState().isReadOnlyView = false
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
              Box(modifier = Modifier.aspectRatio(0.55f).fillMaxSize().testTag("tripImageBox")) {
                // Display image or default image
                if (trip.imageUri.isNotEmpty()) {
                  Image(
                      painter = rememberAsyncImagePainter(model = trip.imageUri),
                      contentDescription = stringResource(R.string.selected_image_description),
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize().testTag("tripImage"))
                } else {
                  Image(
                      painter = painterResource(id = R.drawable.default_trip_image),
                      contentDescription = stringResource(R.string.trip_image_overview_description),
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize().testTag("defaultTripImage"))
                }

                // Heart icon on top of the image
                IconButton(
                    onClick = {
                      val updatedFavoriteTrips =
                          if (user.favoriteTrips.contains(trip.id)) {
                            user.favoriteTrips.toMutableList().apply { remove(trip.id) }
                          } else {
                            user.favoriteTrips.toMutableList().apply { add(trip.id) }
                          }
                      val updatedUser = user.copy(favoriteTrips = updatedFavoriteTrips)
                      userViewModel.updateUser(updatedUser)
                    },
                    modifier =
                        Modifier.align(Alignment.TopStart)
                            .padding(8.dp)
                            .testTag("favoriteButton_${trip.name}")) {
                      Icon(
                          imageVector =
                              if (user.favoriteTrips.contains(trip.id)) Icons.TwoTone.Favorite
                              else Icons.Default.FavoriteBorder,
                          contentDescription =
                              if (user.favoriteTrips.contains(trip.id)) "Unmark as Favorite"
                              else "Mark as Favorite",
                          tint =
                              if (user.favoriteTrips.contains(trip.id)) Color.Red
                              else Color.DarkGray,
                          modifier =
                              Modifier.drawBehind {
                                // Draw white outline for the heart button
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.5f),
                                    radius = size.minDimension / 2 + 4.dp.toPx(),
                                    center = center)
                              })
                    }
              }
              // modifier.weight(2f) is used here to set the column to 2/3 of the card
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

              Box(modifier = Modifier.align(Alignment.Top)) {
                IconButton(
                    enabled = isConnected,
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("expandIcon_${trip.name}")) {
                      Icon(
                          imageVector = Icons.Default.MoreVert,
                          contentDescription =
                              if (isExpanded) stringResource(R.string.collapse_text)
                              else stringResource(R.string.expand_text))
                    }
                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
                      DropdownMenuItem(
                          onClick = {
                            isExpanded = false
                            showDialog = true
                          },
                          text = { Text(stringResource(R.string.delete_text)) },
                          modifier = Modifier.testTag("deleteMenuItem_${trip.name}"))
                      DropdownMenuItem(
                          onClick = {
                            isExpanded = false
                            if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.WRITE_CALENDAR) ==
                                PackageManager.PERMISSION_GRANTED) {
                              // Permission is already granted
                              openGoogleCalendar(context, trip)
                            } else { // Request calendar permission to user
                              requestPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                            }
                          },
                          text = { Text(stringResource(R.string.add_to_calendar_text)) },
                          modifier = Modifier.testTag("addToCalendarMenuItem_${trip.name}"))
                      DropdownMenuItem(
                          onClick = {
                            isExpanded = false
                            leaveTrip = true
                          },
                          text = { Text(stringResource(R.string.leave_trip_text)) },
                          modifier = Modifier.testTag("leaveMenuItem_${trip.name}"))
                    }
              }
            }
      })
  // Confirmation Dialog for deleting the trip
  if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text(text = stringResource(R.string.remove_trip_text)) },
        text = { Text(stringResource(R.string.remove_trip_confirmation, trip.name)) },
        confirmButton = {
          TextButton(
              onClick = {
                tripsViewModel.deleteTripById(trip.id)
                Toast.makeText(context, R.string.deleted_trip_text, Toast.LENGTH_SHORT).show()
                showDialog = false
              }) {
                Text(stringResource(R.string.remove_text))
              }
        },
        dismissButton = {
          TextButton(onClick = { showDialog = false }) {
            Text(stringResource(R.string.cancel_text))
          }
        })
  }
  // confirmation button for leaving a trip
  if (leaveTrip) {
    AlertDialog(
        onDismissRequest = { leaveTrip = false },
        title = { Text(text = stringResource(R.string.leave_trip_text)) },
        text = { Text(stringResource(R.string.leave_trip_confirmation, trip.name)) },
        confirmButton = {
          TextButton(
              onClick = {
                val userId = Firebase.auth.uid.orEmpty()
                if (trip.participants.contains(userId)) {
                  if (trip.participants.size > 1) {
                    val updatedParticipants = trip.participants.filter { it != userId }
                    val updatedTrip = trip.copy(participants = updatedParticipants)
                    tripsViewModel.updateTrip(
                        updatedTrip,
                        onSuccess = {
                          Toast.makeText(
                                  context,
                                  context.getString(R.string.trip_left_text),
                                  Toast.LENGTH_SHORT)
                              .show()
                        },
                        onFailure = { error ->
                          Toast.makeText(
                                  context,
                                  context.getString(R.string.fail_leave_trip, error.message),
                                  Toast.LENGTH_SHORT)
                              .show()
                        })
                  } else {
                    // if no participants left, delete the trip
                    tripsViewModel.deleteTripById(trip.id)
                  }
                }
                leaveTrip = false
              }) {
                Text(stringResource(R.string.leave))
              }
        },
        dismissButton = {
          TextButton(onClick = { leaveTrip = false }) { Text(stringResource(R.string.cancel_text)) }
        })
  }
}
/**
 * Composable function that displays the list of participants in a trip.
 *
 * If there are more than four participants, the text "and X more" is displayed instead of showing
 * all participant avatars. If no participants are present, a message indicates this.
 *
 * @param trip The trip data containing the list of participants.
 * @param userViewModel The ViewModel containing the state and logic for user data.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DisplayParticipants(
    trip: Trip,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    arrangement: Arrangement.Vertical = Arrangement.Bottom
) {
  val numberOfParticipants = trip.participants.size - 1
  val numberToString = generateParticipantString(numberOfParticipants)
  val themeColor = MaterialTheme.colorScheme.onSurface
  Column(
      modifier = modifier,
      verticalArrangement = arrangement, // Align top to bottom
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
                  val user = userViewModel.contacts.value.find { it.id == participant }
                  if (user != null) {
                    // uses the same UserIcon function as in the participants form
                    UserIcon(user)
                  }
                }
            if (numberOfParticipants > 4) {
              Text(
                  text = stringResource(R.string.additional_participants, numberOfParticipants - 4),
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

/**
 * Formats a Timestamp to "MMM dd yyyy" date string.
 *
 * @return Formatted date string
 */
fun Timestamp.toDateString(): String {
  val sdf = java.text.SimpleDateFormat("MMM dd yyyy", java.util.Locale.getDefault())
  return sdf.format(this.toDate())
}

/**
 * Generates display text for number of participants.
 *
 * @param numberOfParticipants Number of participants to describe
 * @return String describing participant count
 */
fun generateParticipantString(numberOfParticipants: Int): String {
  return when (numberOfParticipants) {
    0 -> "No participants."
    1 -> "1 Other Participant:"
    else -> "$numberOfParticipants Other Participants:"
  }
}
/**
 * Opens the default calendar app on the device with pre-filled details for a new event.
 *
 * This method creates an `Intent` to insert a calendar event using the trip's details such as name,
 * description, start time, and end time.
 *
 * @param context The context used to launch the intent.
 * @param trip The trip data used to populate the calendar event details.
 */
internal fun openGoogleCalendar(context: Context, trip: Trip) {
  val intent =
      Intent(Intent.ACTION_INSERT)
          .setData(CalendarContract.Events.CONTENT_URI)
          .putExtra(CalendarContract.Events.TITLE, trip.name)
          .putExtra(CalendarContract.Events.DESCRIPTION, trip.description)
          .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, trip.startDate.toDate().time)
          .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, trip.endDate.toDate().time)
          .putExtra(CalendarContract.Events.EVENT_LOCATION, trip.location.name)
          .putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

  // Handle ActivityNotFoundException
  try {
    context.startActivity(intent)
  } catch (e: ActivityNotFoundException) {
    Toast.makeText(context, R.string.no_compatible_calendar, Toast.LENGTH_LONG).show()
  }
}
