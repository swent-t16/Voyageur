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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.TextField
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
  val context = LocalContext.current
  val isConnected = status === ConnectionState.Available
  var searchQuery by remember { mutableStateOf("") }
  var showOnlyFavorites by remember { mutableStateOf(false) }
  val user by userViewModel.user.collectAsState()
  if (user == null) {
    // Reload screen if the user is null
    navigationActions.navigateTo(Screen.OVERVIEW)
    return
  }
  val trips =
      if (showOnlyFavorites) unfilteredTrips.filter { user!!.favoriteTrips.contains(it.id) }
      else unfilteredTrips

  LaunchedEffect(isLoadingUser, isLoadingTrip) { isLoading = isLoadingUser || isLoadingTrip }
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
  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = {
              if (isConnected) navigationActions.navigateTo(Screen.ADD_TRIP)
              else
                  Toast.makeText(
                          context, R.string.notification_no_internet_text, Toast.LENGTH_SHORT)
                      .show()
            },
            modifier = Modifier.testTag("createTripButton")) {
              Icon(
                  Icons.Outlined.Add,
                  stringResource(R.string.floating_button),
                  modifier = Modifier.testTag("addIcon"))
            }
      },
      modifier = Modifier.testTag("overviewScreen"),
      topBar = {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp) // Increased padding
            ) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    // TextField on the left
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                          Text(
                              text = stringResource(R.string.overview_searchbar_placeholder),
                              style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                              modifier = Modifier.fillMaxWidth())
                        },
                        modifier =
                            Modifier.weight(
                                    1f) // Forces TextField to take up all available horizontal
                                // space
                                .height(56.dp) // Increased height
                                .testTag("searchField"),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp) // Slightly increased corner radius
                        )

                    Spacer(modifier = Modifier.width(8.dp)) // Optional spacing

                    // IconButton explicitly aligned to the right
                    IconButton(
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        modifier = Modifier.testTag("favoriteFilterButton")) {
                          Icon(
                              imageVector =
                                  if (showOnlyFavorites) Icons.Filled.Favorite
                                  else Icons.Default.FavoriteBorder,
                              contentDescription =
                                  if (showOnlyFavorites) "Show all trips"
                                  else "Show favorite trips",
                              tint =
                                  if (showOnlyFavorites) MaterialTheme.colorScheme.onSurface
                                  else MaterialTheme.colorScheme.onSurface)
                        }
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
        if (isLoading) {
          CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
        } else {
          Column(
              modifier = Modifier.padding(pd).testTag("overviewColumn"),
          ) {
            if (trips.isEmpty()) {
              Box(
                  modifier = Modifier.padding(pd).fillMaxSize(),
                  contentAlignment = Alignment.Center) {
                    Text(
                        modifier = Modifier.testTag("emptyTripPrompt"),
                        text =
                            if (showOnlyFavorites) stringResource(R.string.no_favorite_trips)
                            else stringResource(R.string.empty_trip_prompt),
                    )
                  }
            } else {
              val filteredTrips =
                  if (searchQuery.isEmpty()) {
                    trips.sortedBy { it.startDate }
                  } else {
                    trips
                        .filter { it.name.contains(searchQuery, ignoreCase = true) }
                        .sortedBy { it.startDate }
                  }

              // Add this condition to check if filteredTrips is empty while searching
              if (searchQuery.isNotEmpty() && filteredTrips.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                  NoResultsFound(modifier = Modifier.testTag("noSearchResults"))
                }
              } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize().testTag("lazyColumn")) {
                      filteredTrips.forEach { trip ->
                        item {
                          TripItem(
                              tripsViewModel = tripsViewModel,
                              trip = trip,
                              navigationActions = navigationActions,
                              userViewModel = userViewModel,
                              user = user!!)
                          Spacer(modifier = Modifier.height(10.dp))
                        }
                      }
                    }
              }
            }
          }
        }
      })
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
                    }
              }
            }
      })
  // Confirmation Dialog
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
    1 -> "1 Participant:"
    else -> "$numberOfParticipants Participants:"
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
