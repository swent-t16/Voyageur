package com.android.voyageur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.PopupProperties
import com.android.voyageur.model.place.CustomPlace
import com.android.voyageur.model.place.PlacesViewModel

/**
 * A search widget for searching for places.
 *
 * @param placesViewModel The ViewModel used to search for places.
 */
@Composable
fun PlaceSearchWidget(
    placesViewModel: PlacesViewModel,
    onSelect: (CustomPlace) -> Unit,
    modifier: Modifier = Modifier
) {
  var query by remember { mutableStateOf(TextFieldValue("")) }
  var expanded by remember { mutableStateOf(false) }
  val searchedPlaces by placesViewModel.searchedPlaces.collectAsState()

  Column {
    OutlinedTextField(
        value = query,
        onValueChange = {
          placesViewModel.setQuery(it.text, null)
          expanded = it.text.isNotEmpty() && it.text != query.text
          query = it
        },
        modifier = modifier.fillMaxWidth().testTag("searchTextField"),
        placeholder = { Text("Search places...") })

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth().testTag("searchDropdown"),
        properties = PopupProperties(focusable = false)) {
          searchedPlaces.forEach { place ->
            DropdownMenuItem(
                text = { Text(place.place.displayName ?: "Unknown Place") },
                onClick = {
                  onSelect(place)
                  expanded = false
                },
               modifier = Modifier.testTag("item-${place.place.id}"))
          }
        }
  }
}
