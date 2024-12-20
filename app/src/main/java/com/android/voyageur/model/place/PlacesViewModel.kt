package com.android.voyageur.model.place

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel class for managing place search queries and results.
 *
 * @property placesRepository The repository used to search for places.
 */
open class PlacesViewModel(private val placesRepository: PlacesRepository) : ViewModel() {
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  private val _searchedPlaces = MutableStateFlow<List<CustomPlace>>(emptyList())
  val searchedPlaces: StateFlow<List<CustomPlace>> = _searchedPlaces

  private val _selectedPlace = MutableStateFlow<CustomPlace?>(null)
  val selectedPlace: StateFlow<CustomPlace?> = _selectedPlace

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  // Job to manage debounce coroutine
  private var debounceJob: Job? = null

  /**
   * Sets the search query and triggers a search after a debounce period.
   *
   * @param query The search query.
   * @param location The location to search around.
   */
  fun setQuery(query: String, location: LatLng?) {
    debounceJob?.cancel()
    _query.value = query
    debounceJob =
        viewModelScope.launch {
          delay(200) // debounce for 2s
          if (query.isNotEmpty()) {
            searchPlaces(query, location)
          }
        }
  }

  /**
   * Searches for places based on the query and location.
   *
   * @param query The search query.
   * @param location The location to search around.
   */
  fun searchPlaces(query: String, location: LatLng?) {
    placesRepository.searchPlaces(
        query,
        location,
        onSuccess = { places -> _searchedPlaces.value = places },
        onFailure = { exception -> Log.e("PlacesViewModel", "Failed to search places", exception) })
  }

  /**
   * Selects a place to display its details.
   *
   * @param place The place to select.
   */
  fun selectPlace(place: CustomPlace) {
    _selectedPlace.value = place
    _isLoading.value = true
    selectedPlace.value?.let {
      place.place.id?.let { it1 ->
        placesRepository.fetchAdvancedDetails(
            it1,
            onSuccess = { place ->
              _selectedPlace.value = place
              _isLoading.value = false
            },
            onFailure = { exception ->
              Log.e("PlacesViewModel", "Failed to fetch place details", exception)
              _isLoading.value = false
            })
      }
    }
  }

  /** Deselects the currently selected place. */
  fun deselectPlace() {
    _selectedPlace.value = null
  }

  /**
   * Sets the list of searched places.
   *
   * @param places The list of places to set.
   */
  fun setSearchedPlaces(places: List<CustomPlace>) {
    _searchedPlaces.value = places
  }

  companion object {
    /**
     * Provides a factory to create an instance of PlacesViewModel.
     *
     * @param placesClient The PlacesClient used to make API requests.
     * @return A ViewModelProvider.Factory to create PlacesViewModel instances.
     */
    fun provideFactory(placesClient: PlacesClient): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val placesRepository = GooglePlacesRepository(placesClient)
            return PlacesViewModel(placesRepository) as T
          }
        }
  }
}
