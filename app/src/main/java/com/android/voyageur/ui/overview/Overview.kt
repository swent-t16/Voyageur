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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapVert
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
import kotlinx.coroutines.launch

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
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalLayoutApi::class)
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
  var sortedDecreasing by remember { mutableStateOf(true) }
  var showOnlyFavorites by remember { mutableStateOf(false) }
  val user by userViewModel.user.collectAsState()
  var visibleTripIds by remember { mutableStateOf(unfilteredTrips.map { it.id }.toSet()) }

  if (user == null) {
    return
  }

  LaunchedEffect(user) {
    val updatedFavoriteTrips =
        user!!.favoriteTrips.filter { tripId -> unfilteredTrips.any { trip -> trip.id == tripId } }
    if (updatedFavoriteTrips.size != user!!.favoriteTrips.size) {
      val updatedUser = user!!.copy(favoriteTrips = updatedFavoriteTrips)
      userViewModel.updateUser(updatedUser)
    }
  }

  LaunchedEffect(unfilteredTrips) { visibleTripIds = unfilteredTrips.map { it.id }.toSet() }

  val visibleTrips = unfilteredTrips.filter { visibleTripIds.contains(it.id) }

  val trips =
      if (showOnlyFavorites) {
        visibleTrips.filter { user!!.favoriteTrips.contains(it.id) }
      } else {
        visibleTrips
      }

  LaunchedEffect(isLoadingUser, isLoadingTrip) { isLoading = isLoadingUser || isLoadingTrip }

  LoadParticipantsEffect(trips, userViewModel)

  Scaffold(
      floatingActionButton = { AddTripFAB(isConnected, navigationActions) },
      modifier = Modifier.testTag("overviewScreen"),
      topBar = {
        Column {
          FlowRow(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
              verticalArrangement = Arrangement.Center,
              horizontalArrangement = Arrangement.SpaceBetween) {
                SearchBar(
                    placeholderId = R.string.overview_searchbar_placeholder,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.testTag("searchField").weight(1f).width(50.dp))

                IconButton(
                    onClick = { sortedDecreasing = !sortedDecreasing },
                    modifier = Modifier.testTag("reverseTripsOrderButton")) {
                      Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Sort order")
                    }

                IconButton(
                    onClick = { showOnlyFavorites = !showOnlyFavorites },
                    modifier = Modifier.testTag("favoriteFilterButton")) {
                      Icon(
                          imageVector =
                              if (showOnlyFavorites) Icons.Filled.Favorite
                              else Icons.Default.FavoriteBorder,
                          contentDescription =
                              if (showOnlyFavorites) stringResource(R.string.show_all_trips)
                              else stringResource(R.string.show_favorite_trips),
                          tint = MaterialTheme.colorScheme.onSurface)
                    }

                IconButton(
                    onClick = { navigationActions.navigateTo(Screen.ARCHIVED_TRIPS) },
                    modifier = Modifier.testTag("archiveButton")) {
                      Icon(
                          imageVector = Icons.Default.Archive,
                          contentDescription = stringResource(R.string.archived_trips),
                          tint = MaterialTheme.colorScheme.onSurface)
                    }
              }
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel = userViewModel,
            tripsViewModel = tripsViewModel)
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
            descending = sortedDecreasing,
            user = user!!,
            onTripVisibilityChange = { tripId -> visibleTripIds = visibleTripIds - tripId })
      })
}

/**
 * Composable function that displays a single trip item as a card.
 *
 * This function creates a card view for a trip that displays the trip's image, name, date range,
 * and participants. It also includes functionality for favoriting, archiving, leaving, and deleting
 * the trip through a dropdown menu.
 *
 * @param tripsViewModel The ViewModel containing the state and logic for handling trips.
 * @param trip The Trip object containing the trip's data to be displayed.
 * @param navigationActions Actions to handle navigation between screens.
 * @param userViewModel The ViewModel containing the state and logic for user data.
 * @param user The current User object.
 * @param onTripVisibilityChange Callback function triggered when trip visibility changes.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun TripItem(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    user: User,
    onTripVisibilityChange: (String) -> Unit
) {
  val dateRange = trip.startDate.toDateString() + " - " + trip.endDate.toDateString()
  val themeColor = MaterialTheme.colorScheme.onSurface
  var isExpanded by remember { mutableStateOf(false) }
  var showDialog by remember { mutableStateOf(false) }
  var leaveTrip by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available

  val requestPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
          isGranted: Boolean ->
        if (isGranted) {
          openGoogleCalendar(context, trip)
        } else {
          Toast.makeText(context, R.string.denied_calendar_permission, Toast.LENGTH_SHORT).show()
        }
      }

  Card(
      onClick = {
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

                // Heart icon
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
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.5f),
                                    radius = size.minDimension / 2 + 4.dp.toPx(),
                                    center = center)
                              })
                    }
              }

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
                      if (!trip.archived) {
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
                                openGoogleCalendar(context, trip)
                              } else {
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
                        DropdownMenuItem(
                            onClick = {
                              isExpanded = false
                              onTripVisibilityChange(trip.id)
                              tripsViewModel.archiveTrip(
                                  trip,
                                  onSuccess = {
                                    Toast.makeText(
                                            context, R.string.trip_archived, Toast.LENGTH_SHORT)
                                        .show()
                                  },
                                  onFailure = { error ->
                                    Toast.makeText(
                                            context,
                                            "Failed to archive trip: ${error.message}",
                                            Toast.LENGTH_SHORT)
                                        .show()
                                  })
                            },
                            text = { Text(stringResource(R.string.archive_trip)) },
                            modifier = Modifier.testTag("archiveMenuItem_${trip.name}"))
                      } else {
                        DropdownMenuItem(
                            onClick = {
                              isExpanded = false
                              onTripVisibilityChange(trip.id)
                              tripsViewModel.unarchiveTrip(
                                  trip,
                                  onSuccess = {
                                    Toast.makeText(
                                            context, R.string.trip_unarchived, Toast.LENGTH_SHORT)
                                        .show()
                                  },
                                  onFailure = { error ->
                                    Toast.makeText(
                                            context,
                                            "Failed to unarchive trip: ${error.message}",
                                            Toast.LENGTH_SHORT)
                                        .show()
                                  })
                            },
                            text = { Text(stringResource(R.string.unarchive_trip)) },
                            modifier = Modifier.testTag("unarchiveMenuItem_${trip.name}"))
                      }
                    }
              }
            }
      })

  // Delete confirmation dialog
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

  // Leave trip confirmation dialog
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
                    // If no participants left, delete the trip
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
 * Composable function that renders the main content of the overview screen.
 *
 * This function handles the display of either a loading indicator, an empty state message, or the
 * list of trips depending on the current state and data available.
 *
 * @param isLoading Boolean indicating if data is currently being loaded.
 * @param trips List of Trip objects to be displayed.
 * @param searchQuery Current search query string for filtering trips.
 * @param showOnlyFavorites Boolean indicating if only favorite trips should be shown.
 * @param padding PaddingValues to be applied to the content.
 * @param tripsViewModel The ViewModel containing the state and logic for handling trips.
 * @param navigationActions Actions to handle navigation between screens.
 * @param userViewModel The ViewModel containing the state and logic for user data.
 * @param descending Boolean indicating if trips should be sorted in descending order.
 * @param user The current User object.
 * @param onTripVisibilityChange Callback function triggered when trip visibility changes.
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
    descending: Boolean = true,
    user: User,
    onTripVisibilityChange: (String) -> Unit
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
          descending = descending,
          user = user,
          onTripVisibilityChange = onTripVisibilityChange)
    }
  }
}

/**
 * Composable function that displays a scrollable list of trips.
 *
 * This function handles the filtering and sorting of trips based on the search query and sort
 * order, and displays them in a LazyColumn. If no trips match the search query, it shows a "no
 * results" message.
 *
 * @param trips List of Trip objects to be displayed.
 * @param searchQuery Current search query string for filtering trips.
 * @param tripsViewModel The ViewModel containing the state and logic for handling trips.
 * @param navigationActions Actions to handle navigation between screens.
 * @param userViewModel The ViewModel containing the state and logic for user data.
 * @param descending Boolean indicating if trips should be sorted in descending order.
 * @param user The current User object.
 * @param onTripVisibilityChange Callback function triggered when trip visibility changes.
 */
@Composable
private fun TripsList(
    trips: List<Trip>,
    searchQuery: String,
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    descending: Boolean = true,
    user: User,
    onTripVisibilityChange: (String) -> Unit
) {
  var filteredTrips = filterTrips(trips, searchQuery)
  if (!descending) {
    filteredTrips = filteredTrips.reversed()
  }

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
              user = user,
              onTripVisibilityChange = onTripVisibilityChange)
        }
      }
}

/**
 * Side effect composable that loads participant data for the displayed trips.
 *
 * This function fetches user data for all participants in the provided trips and updates the
 * contacts in the UserViewModel.
 *
 * @param trips List of Trip objects whose participants need to be loaded.
 * @param userViewModel The ViewModel containing the state and logic for user data.
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
 * Composable function that creates a Floating Action Button for adding new trips.
 *
 * This function displays an add button that navigates to the add trip screen when clicked, but only
 * if there is an internet connection available.
 *
 * @param isConnected Boolean indicating if the device has an internet connection.
 * @param navigationActions Actions to handle navigation between screens.
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
            else stringResource(R.string.no_favorite_trips))
  }
}

/**
 * Function that filters a list of trips based on a search query.
 *
 * If the search query is empty, returns the trips sorted by start date. If there is a search query,
 * returns trips whose names contain the query (case-insensitive).
 *
 * @param trips List of Trip objects to be filtered.
 * @param searchQuery String to filter trips by name.
 * @return Filtered and sorted list of trips.
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
  val numberOfParticipants = trip.participants.size
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
            trip.participants.take(4).forEach { participant ->
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
