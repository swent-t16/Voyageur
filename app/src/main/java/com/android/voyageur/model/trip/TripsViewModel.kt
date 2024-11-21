package com.android.voyageur.model.trip

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.voyageur.model.activity.Activity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.UUID
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class TripsViewModel(
    private val tripsRepository: TripRepository,
) : ViewModel() {
  private val _trips = MutableStateFlow<List<Trip>>(emptyList())
  val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

  // useful for updating trip
  private val _selectedTrip = MutableStateFlow<Trip?>(null)
  open val selectedTrip: StateFlow<Trip?> = _selectedTrip.asStateFlow()

  // useful for displaying the activities for one day:
  private val _selectedDay = MutableStateFlow<LocalDate?>(null)
  open val selectedDay: StateFlow<LocalDate?> = _selectedDay.asStateFlow()

  private val _selectedActivity = MutableStateFlow<Activity?>(null)
  open val selectedActivity: StateFlow<Activity?> = _selectedActivity.asStateFlow()

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

  private val _tripType = MutableStateFlow(TripType.BUSINESS)
  val tripType: StateFlow<TripType> = _tripType.asStateFlow()

  fun setTripType(type: TripType) {
    _tripType.value = type
  }

  fun selectTrip(trip: Trip) {
    _selectedTrip.value = trip
  }

  fun selectDay(day: LocalDate) {
    _selectedDay.value = day
  }

  fun selectActivity(activity: Activity) {
    _selectedActivity.value = activity
  }

  fun getNewTripId(): String = tripsRepository.getNewTripId()

  fun getTrips(onSuccess: () -> Unit = {}) {
    tripsRepository.getTrips(
        creator = Firebase.auth.uid.orEmpty(),
        onSuccess = { trips ->
          /*
              This is a trick to force a recompose, because the reference wouldn't
              change and update the UI as the list references wouldn't change, nor the object ref.
              so the UI won't get updated without this.
          */
          _trips.value = ArrayList()
          _trips.value = trips
          onSuccess()
        },
        onFailure = {})
  }

  fun createTrip(trip: Trip, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    tripsRepository.createTrip(
        trip = trip, onSuccess = { getTrips(onSuccess) }, onFailure = { onFailure(it) })
  }

  fun deleteTripById(id: String, onSuccess: () -> Unit = {}) {
    tripsRepository.deleteTripById(id = id, onSuccess = { getTrips(onSuccess) }, onFailure = {})
  }

  fun updateTrip(trip: Trip, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    tripsRepository.updateTrip(
        trip = trip, onSuccess = { getTrips(onSuccess) }, onFailure = { onFailure(it) })
  }

  fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val fileRef = storageRef.child("images/${uri.lastPathSegment}" + UUID.randomUUID())
    fileRef
        .putFile(uri)
        .addOnSuccessListener {
          fileRef.downloadUrl
              .addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString()) // Return the download URL
              }
              .addOnFailureListener { exception -> onFailure(exception) }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }
}
