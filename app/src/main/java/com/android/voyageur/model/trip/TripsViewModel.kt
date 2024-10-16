package com.android.voyageur.model.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TripsViewModel(
    private val tripsRepository: TripRepository,
) : ViewModel() {
  private val _trips = MutableStateFlow<List<Trip>>(emptyList())
  val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

  // useful for updating trip
  private val _selectedTrip =
      // initializing with empty trip
      MutableStateFlow(
          Trip(
              "",
              "",
              emptyList(),
              "",
              "",
              emptyList(),
              Timestamp.now(),
              Timestamp.now(),
              emptyList(),
              TripType.TOURISM,
          ),
      )
  var selectedTrip: StateFlow<Trip> = _selectedTrip

  init {
    tripsRepository.init {
      tripsRepository.getTrips(
          Firebase?.auth?.uid.orEmpty(), onSuccess = { _trips.value = it }, onFailure = {})
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T =
              TripsViewModel(TripRepositoryFirebase(Firebase.firestore)) as T
        }
  }

  fun selectTrip(trip: Trip) {
    _selectedTrip.value = trip
  }

  fun getNewTripId(): String = tripsRepository.getNewTripId()

  fun getTrips() {
    tripsRepository.getTrips(
        creator = Firebase.auth.uid.orEmpty(),
        onSuccess = { trips -> _trips.value = trips },
        onFailure = {})
  }

  fun createTrip(trip: Trip) {
    tripsRepository.createTrip(trip = trip, onSuccess = { getTrips() }, onFailure = {})
  }

  fun deleteTripById(id: String) {
    tripsRepository.deleteTripById(id = id, onSuccess = { getTrips() }, onFailure = {})
  }

  fun updateTrip(trip: Trip) {
    tripsRepository.updateTrip(trip = trip, onSuccess = { getTrips() }, onFailure = {})
  }
}
