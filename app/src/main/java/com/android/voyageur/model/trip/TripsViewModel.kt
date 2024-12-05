package com.android.voyageur.model.trip

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.assistant.generatePrompt
import com.android.voyageur.model.assistant.generativeModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

  // used for the AI assistant
  private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
  open val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val _selectedActivity = MutableStateFlow<Activity?>(null)
  open val selectedActivity: StateFlow<Activity?> = _selectedActivity.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  init {
    tripsRepository.init {
      _isLoading.value = true
      tripsRepository.getTrips(
          Firebase?.auth?.uid.orEmpty(),
          onSuccess = {
            _trips.value = it
            _isLoading.value = false
          },
          onFailure = { _isLoading.value = false })
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

  open fun selectDay(day: LocalDate) {
    _selectedDay.value = day
  }

  fun selectActivity(activity: Activity) {
    _selectedActivity.value = activity
  }

  fun getNewTripId(): String = tripsRepository.getNewTripId()

  fun getTrips(onSuccess: () -> Unit = {}) {
    _isLoading.value = true
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
          _isLoading.value = false
          onSuccess()
        },
        onFailure = {
          _isLoading.value = false
          Log.e("TripsViewModel", "Failed to get trips", it)
        })
  }

  fun createTrip(trip: Trip, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    tripsRepository.createTrip(
        trip = trip, onSuccess = { getTrips(onSuccess) }, onFailure = { onFailure(it) })
  }

  fun deleteTripById(id: String, onSuccess: () -> Unit = {}) {
    tripsRepository.deleteTripById(
        id = id,
        onSuccess = { getTrips(onSuccess) },
        onFailure = { exception -> Log.e("TripsViewModel", "Failed to delete trip", exception) })
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

  open fun getActivitiesForSelectedTrip(): List<Activity> {
    return selectedTrip.value?.activities ?: emptyList()
  }

  open fun addActivityToTrip(activity: Activity) {
    if (selectedTrip.value != null) {
      val trip = selectedTrip.value!!
      val updatedTrip = trip.copy(activities = trip.activities + activity)
      updateTrip(
          updatedTrip,
          onSuccess = {
            /*
                This is a trick to force a recompose, because the reference wouldn't
                change and update the UI.
            */
            selectTrip(Trip())
            selectTrip(updatedTrip)
          })
    }
  }

  open fun removeActivityFromTrip(activity: Activity) {
    if (selectedTrip.value != null) {
      val trip = selectedTrip.value!!
      val updatedTrip = trip.copy(activities = trip.activities - activity)
      updateTrip(
          updatedTrip,
          onSuccess = {
            /*
                This is a trick to force a recompose, because the reference wouldn't
                change and update the UI.
            */
            selectTrip(Trip())
            selectTrip(updatedTrip)
          })
    }
  }

  open fun getPhotosForSelectedTrip(): List<String> {
    return selectedTrip.value?.photos ?: emptyList()
  }

  open fun addPhotoToTrip(photo: String) {
    if (selectedTrip.value != null) {
      val trip = selectedTrip.value!!
      val updatedTrip = trip.copy(photos = trip.photos + photo)
      updateTrip(
          updatedTrip,
          onSuccess = {
            /*
                This is a trick to force a recompose, because the reference wouldn't
                change and update the UI.
            */
            selectTrip(Trip())
            selectTrip(updatedTrip)
          },
          onFailure = { error ->
            Log.e("PhotosScreen", "Error adding photo: ${error.message}", error)
          })
    }
  }

  open fun removePhotoFromTrip(photo: String) {
    if (selectedTrip.value != null) {
      val trip = selectedTrip.value!!
      val updatedTrip = trip.copy(photos = trip.photos - photo)
      updateTrip(
          updatedTrip,
          onSuccess = {
            /*
                This is a trick to force a recompose, because the reference wouldn't
                change and update the UI.
            */
            selectTrip(Trip())
            selectTrip(updatedTrip)
          },
          onFailure = { error ->
            Log.e("PhotoItem", "Error deleting photo: ${error.message}", error)
          })
    }
  }

  // ****************************************************************************************************
  // AI assistant
  // ****************************************************************************************************

  /**
   * Sends a prompt to the AI assistant to generate activities for a trip. The result changes the UI
   * state.
   *
   * @param trip the trip
   * @param userPrompt the prompt that the user provides in the app
   * @param provideFinalActivities whether to provide final activities with date and time or just
   *   draft activities.
   */
  open fun sendActivitiesPrompt(trip: Trip, userPrompt: String, provideFinalActivities: Boolean) {
    _uiState.value = UiState.Loading

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val response =
            generativeModel.generateContent(
                generatePrompt(trip, userPrompt, provideFinalActivities))
        response.text?.let { outputContent -> _uiState.value = UiState.Success(outputContent) }
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.localizedMessage ?: "unknown error")
      }
    }
  }

  /** Sets the initial UI state to [UiState.Initial]. */
  open fun setInitialUiState() {
    _uiState.value = UiState.Initial
  }
}
