package com.android.voyageur.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.place.CustomPlace
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.components.NoResultsFound
import com.android.voyageur.ui.components.SearchBar
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.ExperimentalCoroutinesApi

const val DISCOVER_PAGE_INDEX = 2

/**
 * Composable function for the search screen.
 *
 * @param userViewModel ViewModel for user-related data.
 * @param placesViewModel ViewModel for place-related data.
 * @param navigationActions Navigation actions for bottom navigation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("MissingPermission")
@Composable
fun SearchScreen(
    userViewModel: UserViewModel,
    placesViewModel: PlacesViewModel,
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    requirePermission: Boolean = true
) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  var isMapView by remember { mutableStateOf(false) }
  var userLocation by remember { mutableStateOf<LatLng?>(null) }
  val searchedUsers by userViewModel.searchedUsers.collectAsState()
  val searchedPlaces by placesViewModel.searchedPlaces.collectAsState()
  var locationCallback: LocationCallback? = null
  var fusedLocationClient: FusedLocationProviderClient? = null
  userViewModel.searchUsers(searchQuery.text)

  val context = LocalContext.current
  var denied by remember { mutableStateOf(false) }
  var showLocationDialog by remember { mutableStateOf(false) }
  val isLoading by userViewModel.isLoading.collectAsState()
  fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)
  val status by connectivityState()

  val isConnected = status === ConnectionState.Available
  if (isConnected) {
    placesViewModel.searchPlaces(searchQuery.text, userLocation)
  } else if (navigationActions.getNavigationState().currentTabForSearch ==
      FilterType.PLACES.ordinal) {
    Toast.makeText(context, "No internet connection, places search is disabled", Toast.LENGTH_SHORT)
        .show()
  }
  fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
  }

  locationCallback =
      object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
          for (lo in p0.locations) {
            if (userLocation == null) userLocation = LatLng(lo.latitude, lo.longitude)
          }
        }
      }

  fun startLocationUpdates() {
    locationCallback?.let {
      val locationRequest =
          LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
          }
      fusedLocationClient?.requestLocationUpdates(locationRequest, it, Looper.getMainLooper())
    }
  }

  val launcherMultiplePermissions =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        if (areGranted) {
          startLocationUpdates()
          Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
          denied = true
          Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
      }

  val permissions =
      arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

  if (!isLocationEnabled(LocalContext.current) && !denied) {
    showLocationDialog = true
  }

  if (showLocationDialog && !denied) {
    AlertDialog(
        onDismissRequest = { showLocationDialog = false },
        title = { Text(text = "Enable Location Services") },
        text = {
          Text(
              text =
                  "Location services are required for this feature. Please enable them in your device settings.")
        },
        confirmButton = {
          TextButton(
              onClick = {
                showLocationDialog = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
              }) {
                Text(text = "Open Settings")
              }
        },
        dismissButton = {
          TextButton(
              onClick = {
                showLocationDialog = false
                denied = true
              }) {
                Text(text = "Cancel")
              }
        })
  }
  LaunchedEffect(navigationActions.getNavigationState().currentTabForSearch) {
    if (navigationActions.getNavigationState().currentTabForSearch == FilterType.PLACES.ordinal &&
        requirePermission) {
      if (permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
      }) {
        startLocationUpdates()
      } else {
        launcherMultiplePermissions.launch(permissions)
      }
    }
  }

  Scaffold(
      modifier = Modifier.testTag("searchScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel)
      },
      floatingActionButton = {
        if (navigationActions.getNavigationState().currentTabForSearch ==
            FilterType.PLACES.ordinal) {
          FloatingActionButton(
              modifier = Modifier.testTag("toggleMapViewButton"),
              onClick = { isMapView = !isMapView }) {
                Icon(
                    imageVector = if (isMapView) Icons.Default.List else Icons.Default.Place,
                    contentDescription = if (isMapView) "Show List" else "Show Map")
              }
        }
      },
      floatingActionButtonPosition = FabPosition.Start) { pd ->
        val textFieldsColours = MaterialTheme.colorScheme.surfaceVariant

        Column(modifier = Modifier.padding(pd).fillMaxSize().testTag("searchScreenContent")) {
          Spacer(modifier = Modifier.height(24.dp))

          // Search bar
          Row(
              modifier = Modifier.testTag("searchBar"),
              verticalAlignment = Alignment.CenterVertically) {
                SearchBar(
                    placeholderId =
                        when (navigationActions.getNavigationState().currentTabForSearch) {
                          FilterType.PLACES.ordinal -> R.string.search_places
                          FilterType.USERS.ordinal -> R.string.search_users
                          2 -> R.string.search_trips
                          else -> R.string.empty
                        },
                    onQueryChange = { query ->
                      searchQuery = TextFieldValue(query)
                      userViewModel.setQuery(query)
                      placesViewModel.setQuery(query, userLocation)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp).testTag("searchTextField"))
              }
          // Tabs
          TabRow(
              modifier = Modifier.testTag("tabRow"),
              selectedTabIndex = navigationActions.getNavigationState().currentTabForSearch) {
                FilterType.values().forEachIndexed { index, filterType ->
                  Tab(
                      modifier = Modifier.testTag("filterButton_${filterType.name}"),
                      selected =
                          navigationActions.getNavigationState().currentTabForSearch ==
                              filterType.ordinal,
                      onClick = {
                        navigationActions.getNavigationState().currentTabForSearch =
                            filterType.ordinal
                      },
                      text = { Text(filterType.name) })
                }
                Tab(
                    modifier = Modifier.testTag("discoverTab"),
                    selected =
                        navigationActions.getNavigationState().currentTabForSearch ==
                            DISCOVER_PAGE_INDEX,
                    onClick = {
                      navigationActions.getNavigationState().currentTabForSearch =
                          DISCOVER_PAGE_INDEX
                    },
                    text = { Text("DISCOVER") })
              }

          Spacer(modifier = Modifier.height(16.dp))
          if (isLoading && !isConnected) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                  CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                }
          }
          Text(
              text = "Search results",
              fontSize = 18.sp,
              modifier = Modifier.padding(horizontal = 16.dp))

          // Search results based on the selected tab
          if (navigationActions.getNavigationState().currentTabForSearch ==
              FilterType.PLACES.ordinal) {
            if (isMapView) {
              val cameraPositionState = rememberCameraPositionState {
                position =
                    com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                        userLocation ?: LatLng(37.7749, -122.4194), // Default to SF
                        12f)
              }
              LaunchedEffect(userLocation) {
                cameraPositionState.position =
                    com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                        userLocation ?: LatLng(37.7749, -122.4194), // Default to SF
                        12f)
                if (userLocation != null)
                    fusedLocationClient?.removeLocationUpdates(locationCallback)
              }
              if (userLocation != null || !requirePermission || denied)
                  GoogleMap(
                      properties = MapProperties(isMyLocationEnabled = userLocation != null),
                      modifier =
                          Modifier.padding(16.dp)
                              .clip(RoundedCornerShape(16.dp))
                              .testTag("googleMap"),
                      cameraPositionState = cameraPositionState) {
                        searchedPlaces.forEach { customPlace ->
                          customPlace.place.latLng?.let {
                            Marker(
                                state = MarkerState(position = it),
                                title = customPlace.place.displayName ?: "Unknown Place",
                                snippet = customPlace.place.address ?: "No address")
                          }
                        }
                      }
              else
                  Column(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.SpaceEvenly,
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                      }
            } else {
              LazyColumn(
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(16.dp)
                          .background(textFieldsColours, shape = MaterialTheme.shapes.large)
                          .testTag("searchResultsPlaces")) {
                    if (searchedPlaces.isEmpty()) {
                      item { NoResultsFound(modifier = Modifier.testTag("noResults")) }
                    } else {
                      items(searchedPlaces) { place ->
                        PlaceSearchResultItem(
                            place,
                            Modifier.clickable {
                              placesViewModel.selectPlace(place)
                              navigationActions.navigateTo(Screen.PLACE_DETAILS)
                            })
                      }
                    }
                  }
            }
          } else if (navigationActions.getNavigationState().currentTabForSearch ==
              FilterType.USERS.ordinal) {
            LazyColumn(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(16.dp)
                        .background(textFieldsColours, shape = MaterialTheme.shapes.large)
                        .testTag("searchResultsUsers")) {
                  if (searchedUsers.isEmpty()) {
                    item { NoResultsFound(modifier = Modifier.testTag("noResults")) }
                  } else {
                    items(searchedUsers) { user ->
                      UserSearchResultItem(
                          user,
                          userViewModel = userViewModel,
                          fieldColor = MaterialTheme.colorScheme.surfaceVariant,
                          modifier = Modifier.testTag("userItem_${user.id}"),
                          navigationActions = navigationActions)
                    }
                  }
                }
          } else {
            DiscoverContent(searchQuery.text, tripsViewModel, userViewModel)
          }
        }
      }
}

/**
 * Composable function to display a user search result item.
 *
 * @param user The user to display.
 * @param modifier Modifier for the composable.
 * @param userViewModel ViewModel for user-related data.
 * @param fieldColor Background color for the user item.
 */
@Composable
fun UserSearchResultItem(
    user: User,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    fieldColor: Color,
    navigationActions: NavigationActions
) {
  val connectionStatus by connectivityState()
  val isConnected = connectionStatus == ConnectionState.Available
  val currentUser by userViewModel.user.collectAsState()
  val sentFriendRequests by userViewModel.sentFriendRequests.collectAsState()
  val receivedFriendRequests by userViewModel.friendRequests.collectAsState()

  val isCurrentUser = currentUser?.id == user.id
  val isContactAdded = currentUser?.contacts?.contains(user.id) ?: false
  val isRequestPending = sentFriendRequests.any { it.to == user.id }
  val isRequestReceived = receivedFriendRequests.any { it.from == user.id }

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp, horizontal = 16.dp)
              .clickable {
                userViewModel.selectUser(user)
                navigationActions.navigateTo(Screen.SEARCH_USER_PROFILE)
              }
              .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
              .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberAsyncImagePainter(model = user.profilePicture),
            contentDescription = "${user.name}'s profile picture",
            modifier =
                Modifier.size(60.dp).clip(CircleShape).background(fieldColor, shape = CircleShape))

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
          Text(
              text = user.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)

          Spacer(modifier = Modifier.height(4.dp))

          Text(
              text = "@${user.username}",
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }

        if (!isCurrentUser) {
          Row {
            if (isRequestReceived) {
              Button(
                  onClick = {
                    val friendRequest = receivedFriendRequests.find { it.from == user.id }
                    if (friendRequest != null) {
                      userViewModel.acceptFriendRequest(friendRequest)
                    }
                  },
                  enabled = isConnected,
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.tertiary),
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.width(100.dp).height(40.dp)) {
                    Text(
                        text = stringResource(R.string.accept),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center)
                  }
            } else {
              Button(
                  onClick = {
                    when {
                      isContactAdded -> userViewModel.removeContact(user.id)
                      isRequestPending -> {
                        val requestId = userViewModel.getSentRequestId(user.id)
                        if (requestId != null) {
                          userViewModel.deleteFriendRequest(requestId)
                        }
                      }
                      else -> userViewModel.sendContactRequest(user.id)
                    }
                  },
                  enabled = isConnected,
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor =
                              when {
                                isContactAdded -> MaterialTheme.colorScheme.error
                                isRequestPending -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                              }),
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.width(100.dp).height(40.dp)) {
                    Text(
                        text =
                            when {
                              isContactAdded -> stringResource(R.string.remove)
                              isRequestPending -> stringResource(R.string.cancel)
                              else -> stringResource(R.string.add)
                            },
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center)
                  }
            }
          }
        }
      }
}

/**
 * Composable function to display a place search result item.
 *
 * @param customPlace The place to display.
 * @param modifier Modifier for the composable.
 */
@Composable
fun PlaceSearchResultItem(customPlace: CustomPlace, modifier: Modifier = Modifier) {
  val place = customPlace.place
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp, horizontal = 16.dp)
              .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
              .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = place.displayName ?: "Unknown Place",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          Spacer(modifier = Modifier.height(4.dp))

          // Address
          Text(
              text = "· ${place.address ?: "No address"} ·",
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
}

/** Enum class representing the filter types for the search screen. */
enum class FilterType {
  USERS,
  PLACES
}
