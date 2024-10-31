package com.android.voyageur.model.place

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class PlacesViewModel(private val placesRepository: PlacesRepository) : ViewModel() {
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  private val _searchedPlaces = MutableStateFlow<List<Place>>(emptyList())
  val searchedPlaces: StateFlow<List<Place>> = _searchedPlaces

  // Job to manage debounce coroutine
  private var debounceJob: Job? = null

  fun setQuery(query: String) {
    debounceJob?.cancel()
    _query.value = query
    debounceJob =
        viewModelScope.launch {
          delay(200) // debounce for 2s
          if (query.isNotEmpty()) {
            searchPlaces(query)
          }
        }
  }

  fun searchPlaces(query: String) {
    placesRepository.searchPlaces(
        query,
        onSuccess = { places -> _searchedPlaces.value = places },
        onFailure = { exception -> Log.e("PlacesViewModel", "Failed to search places", exception) })
  }

  companion object {
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
