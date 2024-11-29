package com.android.voyageur.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.PopupProperties
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.utils.findMainActivityOrNull
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

/**
 * A search widget for searching for places.
 *
 * @param placesViewModel The ViewModel used to search for places.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PlaceSearchWidget(
    placesViewModel: PlacesViewModel,
    onSelect: (Location) -> Unit,
    modifier: Modifier = Modifier,
    query: TextFieldValue,
    onQueryChange: (TextFieldValue, LatLng?) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val searchedPlaces by placesViewModel.searchedPlaces.collectAsState()
  val scope = rememberCoroutineScope()
  var userLocation by remember { mutableStateOf<LatLng?>(null) }
  val context = LocalContext.current

  // Permissions state
  val locationPermissionState =
      rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

  LaunchedEffect(locationPermissionState.status) {
    if (locationPermissionState.status.isGranted) {
      userLocation = requestUserLocation(context)
      Log.e(
          "PlaceSearchWidget",
          "Fetched user location: ${userLocation?.latitude}, ${userLocation?.longitude}")
    } else {
      if (context.findMainActivityOrNull() != null)
          locationPermissionState.launchPermissionRequest()
    }
  }

  Column {
    OutlinedTextField(
        value = query,
        onValueChange = {
          onQueryChange(it, userLocation)
          expanded = it.text.isNotEmpty() && it.text != query.text
        },
        modifier = modifier.fillMaxWidth().testTag("searchTextField"),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { expanded = false }),
        singleLine = true,
        placeholder = { Text("Search places...") },
        trailingIcon = {
          if (query.text.isNotEmpty()) {
            IconButton(onClick = { onQueryChange(TextFieldValue(""), userLocation) }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Clear text")
            }
          }
        })
    if (!locationPermissionState.status.isGranted) {
      Text("Location permission is required to fetch your location.")
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth().testTag("searchDropdown"),
        properties = PopupProperties(focusable = false)) {
          searchedPlaces.forEach { place ->
            DropdownMenuItem(
                text = {
                  Column {
                    Row { Text(place.place.displayName ?: "Unknown Place") }
                    place.place.formattedAddress?.let { Row { Text(it) } }
                  }
                },
                onClick = {
                  val location = place.toLocation()
                  onSelect(location)
                  expanded = false
                },
                modifier = Modifier.testTag("item-${place.place.id}"))
          }
        }
  }
}

/**
 * Requests the user's location. This function is a suspend function and should be called from a
 * coroutine.
 *
 * @param context The context to use to fetch the location.
 * @return The user's location if available, or null if not available.
 */
@SuppressLint("MissingPermission")
suspend fun requestUserLocation(context: Context): LatLng? {

  val fusedLocationClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(context)

  return try {
    val location = fusedLocationClient.lastLocation.await()
    location?.let { LatLng(it.latitude, it.longitude) }
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}
