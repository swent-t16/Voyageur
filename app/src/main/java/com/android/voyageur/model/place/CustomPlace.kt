package com.android.voyageur.model.place

import androidx.compose.ui.graphics.ImageBitmap
import com.android.voyageur.model.location.Location
import com.google.android.libraries.places.api.model.Place

data class CustomPlace(
    val place: Place,
    val photos: List<ImageBitmap>,
) {
  /** function to convert a CustomPlace object to a Location object with only the fields we need */
  fun toLocation(): Location {
    return Location(
        id = this.place.id ?: "",
        name = this.place.displayName ?: "",
        address = this.place.formattedAddress ?: "",
        lat = this.place.latLng?.latitude ?: 0.0,
        lng = this.place.latLng?.longitude ?: 0.0)
  }
}
