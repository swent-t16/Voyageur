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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.place.CustomPlace
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
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

/**
 * Composable function for the search screen.
 *
 * @param userViewModel ViewModel for user-related data.
 * @param placesViewModel ViewModel for place-related data.
 * @param navigationActions Navigation actions for bottom navigation.
 */
@SuppressLint("MissingPermission")
@Composable
fun SearchScreen(
    userViewModel: UserViewModel,
    placesViewModel: PlacesViewModel,
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
  placesViewModel.searchPlaces(searchQuery.text, userLocation)
  val context = LocalContext.current
  var denied by remember { mutableStateOf(false) }
  var showLocationDialog by remember { mutableStateOf(false) }

  fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)

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
    if (navigationActions.getNavigationState().currentTabForSearch == FilterType.PLACES &&
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
        if (navigationActions.getNavigationState().currentTabForSearch == FilterType.PLACES) {
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
                      placesViewModel.setQuery(searchQuery.text, userLocation)
                    },
                    modifier = Modifier.weight(1f).padding(8.dp).testTag("searchTextField"),
                    textStyle =
                        LocalTextStyle.current.copy(
                            fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done))
              }

          // Tabs
          TabRow(
              modifier = Modifier.testTag("tabRow"),
              selectedTabIndex =
                  navigationActions.getNavigationState().currentTabForSearch.ordinal) {
                FilterType.values().forEachIndexed { index, filterType ->
                  Tab(
                      modifier = Modifier.testTag("filterButton_${filterType.name}"),
                      selected =
                          navigationActions.getNavigationState().currentTabForSearch == filterType,
                      onClick = {
                        navigationActions.getNavigationState().currentTabForSearch = filterType
                      },
                      text = { Text(filterType.name) })
                }
              }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
              text = "Search results",
              fontSize = 18.sp,
              modifier = Modifier.padding(horizontal = 16.dp))

          // Search results based on the selected tab
          if (navigationActions.getNavigationState().currentTabForSearch == FilterType.PLACES) {
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
                      item { NoResultsFound() }
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
                          fieldColor = MaterialTheme.colorScheme.surfaceVariant,
                          modifier = Modifier.testTag("userItem_${user.id}"),
                          navigationActions = navigationActions)
                    }
                  }
                }
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
  val isContactAdded =
      userViewModel.user.collectAsState().value?.contacts?.contains(user.id) ?: false

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp, horizontal = 16.dp)
              .clickable {
                // Navigate to the user profile screen with userId
                userViewModel.selectUser(user)
                navigationActions.navigateTo(Screen.SEARCH_USER_PROFILE)
              } // Make the Row clickable
              .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
              .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberAsyncImagePainter(model = user.profilePicture),
            contentDescription = "${user.name}'s profile picture",
            modifier =
                Modifier.size(60.dp)
                    .clip(CircleShape)
                    .background(fieldColor, shape = CircleShape)
                    .testTag("userProfilePicture_${user.id}"))

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier =
                Modifier.weight(1f) // Use weight to allocate remaining space for this column
                    .padding(end = 8.dp) // Add padding to avoid overlap with the button
            ) {
              Text(
                  text = user.name,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.testTag("userName_${user.id}"))

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                  text = "@${user.username}",
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.testTag("userUsername_${user.id}"))
            }

        Button(
            onClick = { userViewModel.addContact(user.id) },
            enabled = !isContactAdded,
            shape = RoundedCornerShape(20.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        if (isContactAdded) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier =
                Modifier.width(100.dp) // Fixed width for the button
                    .height(40.dp)
                    .testTag("addContactButton")) {
              Text(
                  text = if (isContactAdded) "Added" else "Add",
                  color =
                      if (isContactAdded) MaterialTheme.colorScheme.onSurfaceVariant
                      else MaterialTheme.colorScheme.onPrimary,
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

@Composable
fun NoResultsFound() {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 16.dp, horizontal = 16.dp)
              .background(
                  MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
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
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(48.dp))

              Spacer(modifier = Modifier.height(16.dp))

              // Main message text
              Text(
                  text = "No results found",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface)

              Spacer(modifier = Modifier.height(8.dp))

              // Additional guidance text
              Text(
                  text = "Try adjusting your search or check for typos.",
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center)
            }
      }
}

/** Enum class representing the filter types for the search screen. */
enum class FilterType {
  USERS,
  PLACES
}
