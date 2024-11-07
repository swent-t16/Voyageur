package com.android.voyageur.model.place

import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class GooglePlacesRepository(private val placesClient: PlacesClient) : PlacesRepository {
  override fun searchPlaces(
      query: String,
      onSuccess: (List<Place>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val token = AutocompleteSessionToken.newInstance()

    // For now, we only want to search for restaurants, bars, and cafes
    val filter = listOf("restaurant", "bar", "cafe")

    val request =
        FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .setTypesFilter(filter)
            .build()

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
      onSuccess: (List<Place>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val places = mutableListOf<Place>()
    var failedRequests = 0

    // Define which fields to retrieve for each place
    val placeFields =
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
    placeIds.forEach { placeId ->
      val request = FetchPlaceRequest.builder(placeId, placeFields).build()
      placesClient
          .fetchPlace(request)
          .addOnSuccessListener { response ->
            places.add(response.place)
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
