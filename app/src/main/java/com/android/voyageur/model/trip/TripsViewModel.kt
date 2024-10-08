package com.android.voyageur.model.trip

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TripsViewModel(private val tripsRepository: TripRepository) : ViewModel() {
  private val _trips = MutableStateFlow<List<Trip>>(emptyList())
  var trips: StateFlow<List<Trip>> = _trips

  // useful for updating trip
  private val _selectedTrip =
      // initializing with empty trip
      MutableStateFlow(
          Trip(
              "",
              "",
              emptyArray(),
              "",
              "",
              emptyArray(),
              Timestamp.now(),
              Timestamp.now(),
              emptyArray(),
              TripType.TOURISM))
  var selectedTrip: StateFlow<Trip> = _selectedTrip

  init {
    tripsRepository.init {
      tripsRepository.getTrips(onSuccess = { _trips.value = it }, onFailure = {})
    }
  }

  fun selectTrip(trip: Trip) {
    _selectedTrip.value = trip
  }

  fun getNewTripId(): String = tripsRepository.getNewTripId()

  fun getTrips() {
    tripsRepository.getTrips(onSuccess = { trips -> _trips.value = trips }, onFailure = {})
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
