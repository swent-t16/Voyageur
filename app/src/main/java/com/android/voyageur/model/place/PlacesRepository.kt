package com.android.voyageur.model.place

import com.google.android.gms.maps.model.LatLng

/**
 * Interface for managing interactions with a places data source.
 *
 * The `PlacesRepository` provides methods to search for places based on a query or location
 * and to fetch advanced details about a specific place. It serves as an abstraction layer
 * for working with place data from various sources, such as Google Places API.
 */
interface PlacesRepository {

    /**
     * Searches for places matching the given query and optional location.
     *
     * This method performs a search for places that match the specified `query` string.
     * Optionally, the search can be centered around a specific location using `LatLng`.
     *
     * @param query The search query string (e.g., "restaurant", "museum").
     * @param location The optional `LatLng` object specifying the search center. If null,
     *                 the search is not location-based.
     * @param onSuccess A callback invoked with a list of `CustomPlace` objects if the search
     *                  is successful.
     * @param onFailure A callback invoked with an exception if the search fails.
     */
  fun searchPlaces(
      query: String,
      location: LatLng?,
      onSuccess: (List<CustomPlace>) -> Unit,
      onFailure: (Exception) -> Unit
  )

    /**
     * Fetches detailed information about a specific place using its ID.
     *
     * This method retrieves advanced details for a place, including attributes and photos,
     * based on the provided `placeId`.
     *
     * @param placeId The unique identifier of the place.
     * @param onSuccess A callback invoked with a `CustomPlace` object containing detailed
     *                  information about the place if the fetch is successful.
     * @param onFailure A callback invoked with an exception if fetching details fails.
     */
  fun fetchAdvancedDetails(
      placeId: String,
      onSuccess: (CustomPlace) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
