package com.android.voyageur.model.trip

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Implementation of [TripRepository] that interacts with Firebase Firestore to manage trips.
 * This class provides methods for creating, reading, updating, and deleting trips,
 * as well as fetching and listening for trip updates.
 */
class TripRepositoryFirebase(private val db: FirebaseFirestore) : TripRepository {
  private val collectionPath = "trips"
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Generates a new unique trip ID by creating a new document in the "trips" collection.
     *
     * @return The new trip ID as a string.
     */
  override fun getNewTripId(): String {
    return db.collection(collectionPath).document().id
  }

    /**
     * Initializes the repository by adding an authentication state listener to FirebaseAuth.
     * If a user is logged in, the onSuccess callback is invoked.
     *
     * @param onSuccess The callback to be invoked when initialization is successful.
     */
  override fun init(onSuccess: () -> Unit) {
    auth.addAuthStateListener { auth ->
      val user: FirebaseUser? = auth.currentUser
      if (user != null) {
        onSuccess()
      } else {
        Log.e("TripRepositoryFirebase", "No user found")
      }
    }
  }

    /**
     * Retrieves a list of trips that the specified creator is a participant of.
     *
     * @param creator The ID of the creator whose trips are to be fetched.
     * @param onSuccess The callback to be invoked with the list of trips if the operation is successful.
     * @param onFailure The callback to be invoked if the operation fails, with the exception details.
     */
  override fun getTrips(
      creator: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereArrayContains("participants", creator)
        .get()
        .addOnSuccessListener { result ->
          val trips = result.map { document -> document.toObject(Trip::class.java) }
          onSuccess(trips)
        }
        .addOnFailureListener { exception ->
          Log.e("TripRepositoryFirebase", "Error getting trips: ", exception)
          onFailure(exception)
        }
  }

    /**
     * Creates a new trip in the Firestore database.
     *
     * @param trip The `Trip` object containing the details of the trip to be created.
     * @param onSuccess The callback to be invoked when the trip is successfully created.
     * @param onFailure The callback to be invoked if the operation fails, with the exception details.
     */
  override fun createTrip(trip: Trip, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(trip.id)
        .set(trip)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("TripRepositoryFirebase", "Error creating trip: ", exception)
          onFailure(exception)
        }
  }

    /**
     * Deletes a trip by its ID from the Firestore database.
     *
     * @param id The ID of the trip to be deleted.
     * @param onSuccess The callback to be invoked when the trip is successfully deleted.
     * @param onFailure The callback to be invoked if the operation fails, with the exception details.
     */
  override fun deleteTripById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("TripRepositoryFirebase", "Error deleting trip: ", exception)
          onFailure(exception)
        }
  }

  /**
   * Fetches trips that are discoverable and not created by the user.
   *
   * @param userId The ID of the user.
   * @param onSuccess The callback to be invoked when the trips are fetched successfully.
   * @param onFailure The callback to be invoked when an error occurs.
   */
  override fun getFeed(
      userId: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("discoverable", true)
        .get()
        .addOnSuccessListener { result ->
          val trips =
              result
                  .map { document -> document.toObject(Trip::class.java) }
                  .filter { !it.participants.contains(userId) }
          onSuccess(trips)
        }
        .addOnFailureListener { exception ->
          Log.e("TripRepositoryFirebase", "Error getting feed: ", exception)
          onFailure(exception)
        }
  }

  /**
   * Listens for updates to trips that the user is a participant of.
   *
   * @param userId The ID of the user.
   * @param onSuccess The callback to be invoked when the trips are updated.
   * @param onFailure The callback to be invoked when an error occurs.
   * @return A ListenerRegistration object that can be used to stop listening for updates.
   * @see ListenerRegistration
   */
  override fun listenForTripUpdates(
      userId: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration {
    return db.collection(collectionPath)
        .whereArrayContains("participants", userId)
        .addSnapshotListener { value, error ->
          if (error != null) {
            Log.e("TripRepositoryFirebase", "Error listening for trip updates: ", error)
            onFailure(error)
          }

          if (value != null) {
            val trips = value.map { document -> document.toObject(Trip::class.java) }
            onSuccess(trips)
          }
        }
  }

    /**
     * Updates an existing trip in the Firestore database.
     *
     * @param trip The `Trip` object containing the updated details of the trip.
     * @param onSuccess The callback to be invoked when the trip is successfully updated.
     * @param onFailure The callback to be invoked if the operation fails, with the exception details.
     */
  override fun updateTrip(trip: Trip, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(trip.id)
        .set(trip)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("TripRepositoryFirebase", "Error updating trip: ", exception)
          onFailure(exception)
        }
  }

    /**
     * Retrieves a specific trip by its ID.
     *
     * @param tripId The ID of the trip to be fetched.
     * @param onSuccess The callback to be invoked with the `Trip` object if the operation is successful.
     * @param onFailure The callback to be invoked if the operation fails, with the exception details.
     */
  override fun getTripById(
      tripId: String,
      onSuccess: (Trip) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(tripId)
        .get()
        .addOnSuccessListener { document ->
          document.toObject(Trip::class.java)?.let(onSuccess)
              ?: onFailure(Exception("Trip not found"))
        }
        .addOnFailureListener(onFailure)
  }
}
