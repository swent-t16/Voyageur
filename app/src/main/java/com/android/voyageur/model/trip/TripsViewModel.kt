package com.android.voyageur.model.trip

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.assistant.generatePrompt
import com.android.voyageur.model.assistant.generativeModel
import com.android.voyageur.model.notifications.TripInvite
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.notifications.TripInviteRepositoryFirebase
import com.android.voyageur.model.user.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing trip-related data and operations in the app.
 *
 * This ViewModel interacts with the [TripRepository] to fetch, create, update, and delete trips. It
 * also handles the management of trip-related states such as the selected trip, selected day,
 * activities, and UI state. Additionally, this ViewModel is responsible for handling file uploads
 * to Firebase Storage and integrating with the AI assistant to generate trip activities.
 *
 * @property tripsRepository The repository used to perform CRUD operations on trips.
 * @property tripInviteRepository The repository used to manage trip invites.
 * @property addAuthStateListener Whether to add an authentication state listener.
 * @property firebaseAuth The Firebase authentication instance.
 */
open class TripsViewModel(
    private val tripsRepository: TripRepository,
    protected val tripInviteRepository: TripInviteRepository,
    private val addAuthStateListener: Boolean = false,
    public val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

  /** StateFlow holding the list of trips. */
  private val _trips = MutableStateFlow<List<Trip>>(emptyList())
  val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

  /** StateFlow holding the selected trip. */
  private val _selectedTrip = MutableStateFlow<Trip?>(null)
  open val selectedTrip: StateFlow<Trip?> = _selectedTrip.asStateFlow()

  /** StateFlow holding the count of trip notifications. */
  private val _tripNotificationCount = MutableStateFlow(0L)
  val tripNotificationCount: StateFlow<Long> = _tripNotificationCount

  /** StateFlow holding the selected day. */
  private val _selectedDay = MutableStateFlow<LocalDate?>(null)
  open val selectedDay: StateFlow<LocalDate?> = _selectedDay.asStateFlow()

  /** StateFlow holding the UI state for the AI assistant. */
  private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
  open val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  /** StateFlow holding the selected activity. */
  private val _selectedActivity = MutableStateFlow<Activity?>(null)
  open val selectedActivity: StateFlow<Activity?> = _selectedActivity.asStateFlow()

  /** StateFlow indicating whether data is loading. */
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  /** StateFlow holding the feed of trips. */
  private val _feed = MutableStateFlow<List<Trip>>(emptyList())
  val feed: StateFlow<List<Trip>> = _feed.asStateFlow()

  /** StateFlow holding the list of trip invites. */
  private val _tripInvites = MutableStateFlow<List<TripInvite>>(emptyList())
  val tripInvites: StateFlow<List<TripInvite>> = _tripInvites.asStateFlow()

  /** StateFlow holding the list of users being invited. */
  private val _invitingUsers = MutableStateFlow<List<User>>(emptyList())
  val invitingUsers: StateFlow<List<User>> = _invitingUsers.asStateFlow()

  /** Listener registration for trip updates. */
  private var _tripListenerRegistration: ListenerRegistration? = null

  /** Authentication state listener for Firebase. */
  val authStateListener =
      FirebaseAuth.AuthStateListener { auth ->
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
          _tripListenerRegistration =
              tripsRepository.listenForTripUpdates(
                  firebaseAuth.uid.orEmpty(),
                  onSuccess = {
                    _trips.value = it
                    if (selectedTrip.value != null)
                        it.find { trip -> trip.id == selectedTrip.value?.id }
                            ?.let { it1 -> selectTrip(it1) }
                  },
                  onFailure = { Log.e("TripsViewModel", "Failed to listen for trip updates", it) })
        } else {
          _tripListenerRegistration?.remove()
          _tripListenerRegistration = null
        }
      }

  init {
    tripsRepository.init {
      fetchTripInvites() // Fetch trip invites
      _isLoading.value = true
      tripsRepository.getTrips(
          Firebase.auth.uid.orEmpty(),
          onSuccess = {
            _trips.value = it
            _isLoading.value = false
          },
          onFailure = { _isLoading.value = false })
    }
    if (addAuthStateListener) {
      firebaseAuth.addAuthStateListener(authStateListener)
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T =
              TripsViewModel(
                  TripRepositoryFirebase(Firebase.firestore),
                  TripInviteRepositoryFirebase(Firebase.firestore),
                  addAuthStateListener = true)
                  as T
        }
  }

  /** StateFlow holding the type of the trip. */
  private val _tripType = MutableStateFlow(TripType.BUSINESS)
  val tripType: StateFlow<TripType> = _tripType.asStateFlow()

  /**
   * Sets the type of the trip.
   *
   * @param type The type of the trip.
   */
  fun setTripType(type: TripType) {
    _tripType.value = type
  }

  fun set_tripInvites(tripInvites: List<TripInvite>) {
    _tripInvites.value = tripInvites
  }

  /**
   * Selects a trip.
   *
   * @param trip The trip to select.
   */
  fun selectTrip(trip: Trip) {
    _selectedTrip.value = trip
  }

  /**
   * Selects a day.
   *
   * @param day The day to select.
   */
  open fun selectDay(day: LocalDate) {
    _selectedDay.value = day
  }

  /**
   * Selects an activity.
   *
   * @param activity The activity to select.
   */
  fun selectActivity(activity: Activity) {
    _selectedActivity.value = activity
  }

  /**
   * Gets a new trip ID.
   *
   * @return A new trip ID.
   */
  fun getNewTripId(): String = tripsRepository.getNewTripId()

  /**
   * Fetches trips and updates the state.
   *
   * @param onSuccess Callback to be invoked on success.
   */
  fun getTrips(onSuccess: () -> Unit = {}) {
    _isLoading.value = true
    tripsRepository.getTrips(
        creator = firebaseAuth.uid.orEmpty(),
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

  /**
   * Gets the count of trip notifications.
   *
   * @param onSuccess Callback to be invoked on success.
   */
  fun getNotificationsCount(onSuccess: (Long) -> Unit) {
    val userId = Firebase.auth.uid.orEmpty()
    if (userId.isEmpty()) return

    tripInviteRepository.getTripInvitesCount(
        userId = userId,
        onSuccess = { count ->
          // Assuming _tripNotificationCount is a MutableStateFlow<Long>
          if (_tripNotificationCount.value != count) {
            _tripNotificationCount.value = count
          }
          onSuccess(count)
        },
        onFailure = { exception ->
          Log.e("TripsViewModel", "Failed to fetch trip notifications count: ${exception.message}")
        })
  }

  /** Fetches trip invites and updates the state. */
  fun fetchTripInvites() {
    val userId = Firebase.auth.uid.orEmpty()
    if (userId.isEmpty()) return

    tripInviteRepository.listenToTripInvites(
        userId = userId,
        onSuccess = { invites -> _tripInvites.value = invites },
        onFailure = { e -> Log.e("TripsViewModel", "Failed to fetch trip invites: ${e.message}") })
  }

  /**
   * Creates a new trip.
   *
   * @param trip The trip to create.
   * @param onSuccess Callback to be invoked on success.
   * @param onFailure Callback to be invoked on failure.
   */
  fun createTrip(trip: Trip, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    tripsRepository.createTrip(
        trip = trip, onSuccess = { getTrips(onSuccess) }, onFailure = { onFailure(it) })
  }

  /**
   * Deletes a trip by its ID.
   *
   * @param id The ID of the trip to delete.
   * @param onSuccess Callback to be invoked on success.
   */
  fun deleteTripById(id: String, onSuccess: () -> Unit = {}) {
    tripsRepository.deleteTripById(
        id = id,
        onSuccess = { getTrips(onSuccess) },
        onFailure = { exception -> Log.e("TripsViewModel", "Failed to delete trip", exception) })
  }

  /**
   * Updates a trip.
   *
   * @param trip The trip to update.
   * @param onSuccess Callback to be invoked on success.
   * @param onFailure Callback to be invoked on failure.
   */
  fun updateTrip(trip: Trip, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    tripsRepository.updateTrip(
        trip = trip, onSuccess = { getTrips(onSuccess) }, onFailure = { onFailure(it) })
  }

  /**
   * Uploads an image to Firebase Storage.
   *
   * @param uri The URI of the image to upload.
   * @param onSuccess Callback to be invoked on success with the download URL.
   * @param onFailure Callback to be invoked on failure.
   */
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

  /**
   * Gets the activities for the selected trip.
   *
   * @return A list of activities for the selected trip.
   */
  open fun getActivitiesForSelectedTrip(): List<Activity> {
    return selectedTrip.value?.activities ?: emptyList()
  }

  /**
   * Adds an activity to the selected trip.
   *
   * @param activity The activity to add.
   */
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

  /**
   * Removes an activity from the selected trip.
   *
   * @param activity The activity to remove.
   */
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

  /**
   * Gets the photos for the selected trip.
   *
   * @return A list of photo URLs for the selected trip.
   */
  open fun getPhotosForSelectedTrip(): List<String> {
    return selectedTrip.value?.photos ?: emptyList()
  }

  /**
   * Adds a photo to the selected trip.
   *
   * @param photo The URL of the photo to add.
   */
  open fun addPhotoToTrip(photo: String) {
    if (selectedTrip.value == null) return
    val trip = selectedTrip.value!!
    val updatedTrip = trip.copy(photos = trip.photos + photo)
    updateTrip(
        updatedTrip,
        onSuccess = {
          /*
              This is a trick to force a recompose, because the reference wouldn't
              change and update the UI.
          */
          selectTrip(updatedTrip)
        },
        onFailure = { error ->
          Log.e("PhotosScreen", "Error adding photo: ${error.message}", error)
        })
  }

  /**
   * Removes a photo from the selected trip.
   *
   * @param photo The URL of the photo to remove.
   */
  open fun removePhotoFromTrip(photo: String) {
    if (selectedTrip.value == null) return
    val trip = selectedTrip.value!!
    val updatedTrip = trip.copy(photos = trip.photos - photo)
    updateTrip(
        updatedTrip,
        onSuccess = {
          /*
              This is a trick to force a recompose, because the reference wouldn't
              change and update the UI.
          */
          selectTrip(updatedTrip)
        },
        onFailure = { error ->
          Log.e("PhotoItem", "Error deleting photo: ${error.message}", error)
        })
  }

  /**
   * Gets the feed of trips for a user.
   *
   * @param userId The user ID.
   */
  fun getFeed(userId: String) {
    _isLoading.value = true
    tripsRepository.getFeed(
        userId,
        onSuccess = { trips ->
          _feed.value = trips
          _isLoading.value = false
        },
        onFailure = {
          _isLoading.value = false
          Log.e("TripsViewModel", "Failed to get feed", it)
        })
  }

  // ****************************************************************************************************
  // AI assistant
  // ****************************************************************************************************

  /**
   * Sends a prompt to the AI assistant to generate activities for a trip. The result changes the UI
   * state.
   *
   * @param trip The trip.
   * @param userPrompt The prompt that the user provides in the app.
   * @param interests The interests to focus on.
   * @param provideFinalActivities Whether to provide final activities with date and time or just
   *   draft activities.
   */
  open fun sendActivitiesPrompt(
      trip: Trip,
      userPrompt: String,
      interests: List<String> = emptyList(),
      provideFinalActivities: Boolean,
  ) {
    _uiState.value = UiState.Loading

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val response =
            generativeModel.generateContent(
                generatePrompt(
                    trip,
                    userPrompt,
                    interests,
                    provideFinalActivities,
                    getActivitiesForSelectedTrip().map { it.title }))
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

  /**
   * Accepts a trip invite.
   *
   * @param tripInvite The trip invite to accept.
   */
  fun acceptTripInvite(tripInvite: TripInvite) {
    val userId = firebaseAuth.uid.toString()
    if (userId.isEmpty()) return

    viewModelScope.launch {
      tripsRepository.getTripById(
          tripInvite.tripId,
          onSuccess = { trip ->
            val updatedTrip = trip.copy(participants = trip.participants + userId)
            tripsRepository.updateTrip(
                updatedTrip,
                onSuccess = {
                  tripInviteRepository.deleteTripInvite(
                      tripInvite.id,
                      onSuccess = {},
                      onFailure = { e -> Log.e("TripsViewModel", "Failed to delete invite: $e") })
                },
                onFailure = { e -> Log.e("TripsViewModel", "Failed to update trip: $e") })
          },
          onFailure = { e -> Log.e("TripsViewModel", "Failed to get trip: $e") })
    }
  }

  /**
   * Declines a trip invite.
   *
   * @param inviteId The ID of the invite to decline.
   */
  fun declineTripInvite(inviteId: String) {
    tripInviteRepository.deleteTripInvite(
        inviteId,
        onSuccess = {},
        onFailure = { e -> Log.e("TripsViewModel", "Failed to delete invite: $e") })
  }
  /**
   * Sends a trip invite to another user.
   *
   * @param toUserId The ID of the user to invite
   * @param tripId The ID of the trip to invite them to
   * @param onSuccess Callback to be invoked on success
   * @param onFailure Callback to be invoked on failure with the exception
   */
  fun sendTripInvite(
      toUserId: String,
      tripId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    val fromUserId = firebaseAuth.uid.orEmpty()
    if (fromUserId.isEmpty()) return

    val inviteId = tripInviteRepository.getNewId()
    val tripInvite =
        TripInvite(
            id = inviteId,
            tripId = tripId,
            from = fromUserId,
            to = toUserId,
            accepted = false // Default value for new invites
            )

    tripInviteRepository.createTripInvite(
        req = tripInvite, onSuccess = onSuccess, onFailure = onFailure)
  }
    fun getTripById(tripId: String, onResult: (Trip?) -> Unit) {
        tripsRepository.getTripById(
            tripId,
            onSuccess = { trip -> onResult(trip) },
            onFailure = { exception ->
                Log.e("TripsViewModel", "Failed to fetch trip by ID: $exception")
                onResult(null)
            }
        )
    }

}
