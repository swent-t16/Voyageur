package com.android.voyageur.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Composable function for the search screen.
 *
 * @param userViewModel ViewModel for user-related data.
 * @param placesViewModel ViewModel for place-related data.
 * @param navigationActions Navigation actions for bottom navigation.
 */
@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun SearchScreen(
    userViewModel: UserViewModel,
    placesViewModel: PlacesViewModel,
    navigationActions: NavigationActions,
    requirePermission: Boolean = true
) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  var selectedTab by remember { mutableStateOf(FilterType.USERS) }
  var isMapView by remember { mutableStateOf(false) }
  var userLocation by remember { mutableStateOf<LatLng?>(null) }
  val searchedUsers by userViewModel.searchedUsers.collectAsState()
  val searchedPlaces by placesViewModel.searchedPlaces.collectAsState()
  var locationCallback: LocationCallback? = null
  var fusedLocationClient: FusedLocationProviderClient? = null
  userViewModel.searchUsers(searchQuery.text)
  placesViewModel.searchPlaces(searchQuery.text)
  val context = LocalContext.current
  fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)
  locationCallback =
      object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
          for (lo in p0.locations) {
            // Trick to force refresh of map composable that doesn't detect changes
            userLocation = LatLng(lo.latitude, lo.longitude)
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
          Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
      }

  val permissions =
      arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

  LaunchedEffect(selectedTab) {
    if (selectedTab == FilterType.PLACES && requirePermission) {
      if (permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
      }) {
        // Get the location
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
            selectedItem = navigationActions.currentRoute())
      },
      floatingActionButton = {
        if (selectedTab == FilterType.PLACES) {
          FloatingActionButton(
              modifier = Modifier.testTag("toggleMapViewButton"),
              onClick = { isMapView = !isMapView }) {
                Icon(
                    imageVector = if (isMapView) Icons.Default.List else Icons.Default.Place,
                    contentDescription = if (isMapView) "Show List" else "Show Map")
              }
        }
      },
      floatingActionButtonPosition = FabPosition.Start,
      content = { pd ->
        val textFieldsColours =
            if (isSystemInDarkTheme()) {
              Color.DarkGray
            } else {
              Color.LightGray
            }
        Column(modifier = Modifier.padding(pd).fillMaxSize().testTag("searchScreenContent")) {
          Spacer(modifier = Modifier.height(24.dp))
          Text(
              text = "Search",
              style = MaterialTheme.typography.bodyLarge,
              fontSize = 24.sp,
              modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))

          // Search bar
          Row(
              modifier =
                  Modifier.padding(horizontal = 16.dp)
                      .fillMaxWidth()
                      .background(color = textFieldsColours, shape = MaterialTheme.shapes.medium)
                      .padding(8.dp)
                      .testTag("searchBar"),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = "Search Icon")
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = {
                      searchQuery = it
                      userViewModel.setQuery(searchQuery.text)
                      placesViewModel.setQuery(searchQuery.text)
                    },
                    modifier = Modifier.weight(1f).padding(8.dp).testTag("searchTextField"),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp))
              }

          // Tabs
          TabRow(modifier = Modifier.testTag("tabRow"), selectedTabIndex = selectedTab.ordinal) {
            FilterType.values().forEachIndexed { index, filterType ->
              Tab(
                  modifier = Modifier.testTag("filterButton_${filterType.name}"),
                  selected = selectedTab == filterType,
                  onClick = { selectedTab = filterType },
                  text = { Text(filterType.name) })
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
              text = "Search results",
              fontSize = 18.sp,
              modifier = Modifier.padding(horizontal = 16.dp))

          // Search results based on the selected tab
          if (selectedTab == FilterType.PLACES) {
            if (isMapView) {
              var cameraPositionState = rememberCameraPositionState {
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
              }
              if (userLocation != null || !requirePermission)
                  GoogleMap(
                      modifier =
                          Modifier.padding(16.dp)
                              .clip(RoundedCornerShape(16.dp))
                              .testTag("googleMap"),
                      cameraPositionState = cameraPositionState) {
                        userLocation?.let {
                          Marker(
                              state = MarkerState(position = it),
                              title = "You are here",
                              snippet = "This is your current location")
                        }
                        searchedPlaces.forEach { place ->
                          place.latLng?.let {
                            Marker(
                                state = MarkerState(position = it),
                                title = place.displayName ?: "Unknown Place",
                                snippet = place.address ?: "No address")
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
                      item { NoResultsFound() }
                    } else {
                      items(searchedPlaces) { place ->
                        PlaceSearchResultItem(place, Modifier.testTag("placeItem_${place.id}"))
                      }
                    }
                  }
            }
          } else {
            LazyColumn(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(16.dp)
                        .background(textFieldsColours, shape = MaterialTheme.shapes.large)
                        .testTag("searchResultsUsers")) {
                  if (searchedUsers.isEmpty()) {
                    item { NoResultsFound() }
                  } else {
                    items(searchedUsers) { user ->
                      UserSearchResultItem(
                          user,
                          userViewModel = userViewModel,
                          fieldColor = Color.LightGray,
                          modifier = Modifier.testTag("userItem_${user.id}"),
                      )
                    }
                  }
                }
          }
        }
      })
}

/**
 * Composable function to display a user search result item.
 *
 * @param user The user to display.
 * @param modifier Modifier for the composable.
 * @param userViewModel ViewModel for user-related data.
 * @param fieldColor Background color for the user item.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserSearchResultItem(
    user: User,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    fieldColor: Color
) {
  val isContactAdded =
      userViewModel.user.collectAsState().value?.contacts?.contains(user.id) ?: false

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp, horizontal = 16.dp)
              .background(Color.White, shape = RoundedCornerShape(8.dp))
              .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        FlowRow {
          Image(
              painter = rememberAsyncImagePainter(model = user.profilePicture),
              contentDescription = "${user.name}'s profile picture",
              modifier =
                  Modifier.size(60.dp)
                      .clip(CircleShape)
                      .background(fieldColor, shape = CircleShape)
                      .testTag("userProfilePicture_${user.id}"))

          Spacer(modifier = Modifier.width(16.dp))

          Column {
            Text(
                text = user.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.testTag("userName_${user.id}"))

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "@${user.username}",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.testTag("userUsername_${user.id}"))
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = { userViewModel.addContact(user.id) },
            enabled = !isContactAdded,
            shape = RoundedCornerShape(20.dp),
            colors =
                ButtonDefaults.buttonColors(
                    if (isContactAdded) Color.DarkGray else Color(0xFF6200EA)),
            modifier = Modifier.width(120.dp).height(40.dp).testTag("addContactButton")) {
              Text(
                  text = if (isContactAdded) "Added" else "Add",
                  color = if (isContactAdded) Color.DarkGray else Color.White,
                  fontSize = 14.sp,
                  maxLines = 1,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth())
            }
      }
}

/**
 * Composable function to display a place search result item.
 *
 * @param place The place to display.
 * @param modifier Modifier for the composable.
 */
@Composable
fun PlaceSearchResultItem(place: Place, modifier: Modifier = Modifier) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp, horizontal = 16.dp)
              .background(Color.White, shape = RoundedCornerShape(8.dp))
              .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = place.displayName ?: "Unknown Place",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black)

          Spacer(modifier = Modifier.height(4.dp))
          // Rating and review count
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (place.rating != null) {
              Text(text = "${place.rating} ", fontSize = 14.sp, color = Color.Black)
              Text(
                  text = "★".repeat(place.rating.toInt()),
                  fontSize = 14.sp,
                  color = Color(0xFFFFA000) // Orange color for stars
                  )
              Text(text = " (${place.userRatingsTotal})", fontSize = 14.sp, color = Color.Gray)
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Price level, type, and address
          Text(
              text = "${"$".repeat(place.priceLevel ?: 1)} · ${place.address ?: "No address"}",
              fontSize = 14.sp,
              color = Color.Gray)

          Spacer(modifier = Modifier.height(4.dp))
        }
      }
}

@Composable
fun NoResultsFound() {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 16.dp, horizontal = 16.dp)
              .background(
                  Color(0xFFF8F8F8), shape = RoundedCornerShape(12.dp)) // Light gray background
              .padding(24.dp)
              .testTag("noResults"), // Additional padding for spacing
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Icon for visual appeal
              Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "No results found",
                  tint = Color.Gray,
                  modifier = Modifier.size(48.dp))

              Spacer(modifier = Modifier.height(16.dp))

              // Main message text
              Text(
                  text = "No results found",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black)

              Spacer(modifier = Modifier.height(8.dp))

              // Additional guidance text
              Text(
                  text = "Try adjusting your search or check for typos.",
                  fontSize = 14.sp,
                  color = Color.Gray,
                  textAlign = TextAlign.Center)
            }
      }
}

/** Enum class representing the filter types for the search screen. */
enum class FilterType {
  USERS,
  PLACES
}
