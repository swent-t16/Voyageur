package com.android.voyageur.ui.overview

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
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
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun OverviewScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
) {
  val trips by tripsViewModel.trips.collectAsState()
  var isLoading = false
  var searchVisible by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  val isLoadingUser by userViewModel.isLoading.collectAsState()
  val isLoadingTrip by tripsViewModel.isLoading.collectAsState()
  val status by connectivityState()
  val context = LocalContext.current
  val isConnected = status === ConnectionState.Available

  Log.e("RECOMPOSE", "OverviewScreen recomposed")
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
              else Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            },
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
              Box(
                  modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                  contentAlignment = Alignment.CenterStart) {
                    if (!searchVisible) {
                      Text(
                          text = "Your Trips",
                          style =
                              MaterialTheme.typography.headlineMedium.copy(
                                  fontWeight = FontWeight.Bold),
                          modifier = Modifier.testTag("topBarTitle"))
                    } else {
                      TextField(
                          value = searchQuery,
                          onValueChange = { searchQuery = it },
                          placeholder = { Text("Search trips...") },
                          modifier = Modifier.fillMaxWidth().testTag("searchField"),
                          singleLine = true,
                          shape = RoundedCornerShape(10.dp),
                          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                          keyboardActions = KeyboardActions(onSearch = {}))
                    }
                  }
            },
            actions = {
              Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                      searchVisible = !searchVisible
                      if (!searchVisible) searchQuery = ""
                    },
                    modifier = Modifier.testTag("searchButton")) {
                      Icon(
                          imageVector =
                              if (searchVisible) Icons.Default.Close else Icons.Default.Search,
                          contentDescription =
                              if (searchVisible) "Close search" else "Search trips")
                    }
              }
            },
            modifier = Modifier.height(120.dp).testTag("topAppBar"),
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary))
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
                        text = "You have no trips yet.",
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
                            userViewModel = userViewModel)
                        Spacer(modifier = Modifier.height(10.dp))
                      }
                    }
                  }
            }
          }
        }
      })
}

@Composable
fun TripItem(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {
  val dateRange = trip.startDate.toDateString() + " - " + trip.endDate.toDateString()
  val themeColor = MaterialTheme.colorScheme.onSurface
  var isExpanded by remember { mutableStateOf(false) }
  var showDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current
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
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("expandIcon_${trip.name}")) {
                      Icon(
                          imageVector = Icons.Default.MoreVert,
                          contentDescription = if (isExpanded) "Collapse" else "Expand")
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
                          text = { Text("Delete") },
                          modifier = Modifier.testTag("deleteMenuItem_${trip.name}"))
                    }
              }
            }
      })
  // Confirmation Dialog
  if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text(text = "Remove Trip") },
        text = { Text("Are you sure you want to remove \"${trip.name}\" from your trips?") },
        confirmButton = {
          TextButton(
              onClick = {
                tripsViewModel.deleteTripById(trip.id)
                Toast.makeText(context, "Trip successfully deleted", Toast.LENGTH_SHORT).show()
                showDialog = false
              }) {
                Text("Remove")
              }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } })
  }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DisplayParticipants(trip: Trip, userViewModel: UserViewModel) {
  val numberOfParticipants = trip.participants.size - 1
  val numberToString = generateParticipantString(numberOfParticipants)
  val themeColor = MaterialTheme.colorScheme.onSurface
  Column(
      modifier = Modifier.fillMaxHeight().padding(top = 8.dp),
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
                  val user = userViewModel.contacts.value.find { it.id == participant }
                  if (user != null) {
                    // uses the same UserIcon function as in the participants form
                    UserIcon(user)
                  }
                }
            if (numberOfParticipants > 4) {
              Text(
                  text = "and ${numberOfParticipants - 4} more",
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
  val sdf = java.text.SimpleDateFormat("MMM dd yyyy", java.util.Locale.getDefault())
  return sdf.format(this.toDate())
}

// Helper function to generate the correct string
fun generateParticipantString(numberOfParticipants: Int): String {
  return when (numberOfParticipants) {
    0 -> "No participants."
    1 -> "1 Participant:"
    else -> "$numberOfParticipants Participants:"
  }
}
