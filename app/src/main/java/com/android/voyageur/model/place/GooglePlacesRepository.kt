package com.android.voyageur.model.place

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.math.cos

class GooglePlacesRepository(private val placesClient: PlacesClient) : PlacesRepository {
  private val placeFields =
      listOf(
          Place.Field.ID,
          Place.Field.DISPLAY_NAME,
          Place.Field.ADDRESS,
          Place.Field.PHOTO_METADATAS,
          Place.Field.INTERNATIONAL_PHONE_NUMBER,
          Place.Field.WEBSITE_URI,
          Place.Field.OPENING_HOURS,
          Place.Field.RATING,
          Place.Field.USER_RATINGS_TOTAL,
          Place.Field.LAT_LNG,
          Place.Field.PRICE_LEVEL)

  /**
   * This function is used to convert a circular radius around a location (LatLng) to a rectangular
   * bounds, as the maps sdk doesn't support circular bounds
   *
   * @param center the location used as the user location
   * @param radius the radius area for the circle search
   * @return the converted bounds around the center point
   */
  private fun circularBounds(center: LatLng, radius: Double): RectangularBounds {
    val earthRadius = 6371000.0 // Earth's radius in meters

    val latDelta = radius / earthRadius * (180 / Math.PI) // Convert to degrees
    val lngDelta =
        radius / (earthRadius * cos(Math.toRadians(center.latitude))) *
            (180 / Math.PI) // Adjust for latitude

    val southwest = LatLng(center.latitude - latDelta, center.longitude - lngDelta)
    val northeast = LatLng(center.latitude + latDelta, center.longitude + lngDelta)

    return RectangularBounds.newInstance(southwest, northeast)
  }

  override fun searchPlaces(
      query: String,
      location: LatLng?,
      onSuccess: (List<CustomPlace>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val token = AutocompleteSessionToken.newInstance()

    // For now, we only want to search for restaurants, bars, and cafes
    val filter = listOf("restaurant", "bar", "cafe")
    val bounds = location?.let { circularBounds(it, 10000.0) }
    var builder =
        FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .setTypesFilter(filter)
    if (bounds != null) builder = builder.setLocationBias(bounds)
    val request = builder.build()
    placesClient
        .findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
          val placeIds = response.autocompletePredictions.map { it.placeId }
          fetchPlaceDetails(placeIds, onSuccess, onFailure)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  private fun fetchPlaceDetails(
      placeIds: List<String>,
      onSuccess: (List<CustomPlace>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val places = mutableListOf<CustomPlace>()
    var failedRequests = 0

    // Define which fields to retrieve for each place
    placeIds.forEach { placeId ->
      val request = FetchPlaceRequest.builder(placeId, placeFields).build()
      placesClient
          .fetchPlace(request)
          .addOnSuccessListener { response ->
            val place = response.place
            val bitmaps = mutableListOf<ImageBitmap>()

            val photosMetadata =
                (place.photoMetadatas?.map { it } ?: emptyList<PhotoMetadata>()).take(3)
            for (photoMetadata in photosMetadata) {
              val photoRequest = FetchPhotoRequest.builder(photoMetadata).build()
              placesClient
                  .fetchPhoto(photoRequest)
                  .addOnSuccessListener { fetchPhotoResponse ->
                    val bitmap = fetchPhotoResponse.bitmap.asImageBitmap()
                    bitmaps.add(bitmap)
                  }
                  .addOnFailureListener { exception ->
                    Log.e("PlacesRepository", "Failed to fetch photo", exception)
                  }
            }
            places.add(CustomPlace(place, bitmaps))

            if (places.size + failedRequests == placeIds.size) {
              onSuccess(places)
            }
          }
          .addOnFailureListener { exception ->
            failedRequests++
            if (places.size + failedRequests == placeIds.size) {
              onFailure(exception)
            }
          }
    }
  }
}
