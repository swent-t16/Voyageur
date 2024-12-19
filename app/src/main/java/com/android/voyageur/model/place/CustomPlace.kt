package com.android.voyageur.model.place

import androidx.compose.ui.graphics.ImageBitmap
import com.android.voyageur.model.location.Location
import com.google.android.libraries.places.api.model.Place

/**
 * Represents a custom wrapper for a Google Places API `Place` object with additional functionality.
 *
 * The `CustomPlace` class encapsulates a `Place` object and its associated photos, providing
 * methods to work with the data, including conversion to a simpler `Location` model.
 *
 * @property place The `Place` object containing detailed information about a place.
 * @property photos A list of `ImageBitmap` objects representing photos of the place.
 */
data class CustomPlace(
    val place: Place,
    val photos: List<ImageBitmap>,
) {
    /**
     * Converts the `CustomPlace` object to a `Location` object.
     *
     * This method extracts relevant fields from the `Place` object and maps them to
     * the `Location` class, ensuring only the required data is preserved.
     *
     * @return A `Location` object containing the ID, name, address, latitude, and longitude
     *         of the place.
     */
  fun toLocation(): Location {
    return Location(
        id = this.place.id ?: "",
        name = this.place.displayName ?: "",
        address = this.place.formattedAddress ?: "",
        lat = this.place.latLng?.latitude ?: 0.0,
        lng = this.place.latLng?.longitude ?: 0.0)
  }
}
