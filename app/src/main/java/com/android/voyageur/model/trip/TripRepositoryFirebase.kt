package com.android.voyageur.model.trip

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TripRepositoryFirebase(private val db: FirebaseFirestore) : TripRepository {
  private val collectionPath = "trips"
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()

  override fun getNewTripId(): String {
    return db.collection(collectionPath).document().id
  }

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
