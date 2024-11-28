package com.android.voyageur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.PopupProperties
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.place.PlacesViewModel

/**
 * A search widget for searching for places.
 *
 * @param placesViewModel The ViewModel used to search for places.
 */
@Composable
fun PlaceSearchWidget(
    placesViewModel: PlacesViewModel,
    onSelect: (Location) -> Unit,
    modifier: Modifier = Modifier,
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val searchedPlaces by placesViewModel.searchedPlaces.collectAsState()

  Column {
    OutlinedTextField(
        value = query,
        onValueChange = {
          onQueryChange(it)
          expanded = it.text.isNotEmpty() && it.text != query.text
        },
        modifier = modifier.fillMaxWidth().testTag("searchTextField"),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { expanded = false }),
        singleLine = true,
        placeholder = { Text("Search places...") },
        trailingIcon = {
          if (query.text.isNotEmpty()) {
            IconButton(onClick = { onQueryChange(TextFieldValue("")) }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Clear text")
            }
          }
        })

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth().testTag("searchDropdown"),
        properties = PopupProperties(focusable = false)) {
          searchedPlaces.forEach { place ->
            DropdownMenuItem(
                text = { Text(place.place.displayName ?: "Unknown Place") },
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
