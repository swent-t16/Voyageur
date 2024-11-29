package com.android.voyageur.model.trip

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

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

    /**
     * Fetches a list of trips that the user is participating in.
     * @param creator The ID of the user to fetch the trips for.
     * @param onSuccess Callback invoked with the list of trips.
     * @param onFailure Callback invoked with an exception if the fetch fails.
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
     * Creates a new trip in Firestore.
     * @param trip The trip to create.
     * @param onSuccess Callback invoked when the trip is successfully created.
     * @param onFailure Callback invoked with an exception if the creation fails.
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
     * Deletes a trip from Firestore.
     * @param id The ID of the trip to delete.
     * @param onSuccess Callback invoked when the trip is successfully deleted.
     * @param onFailure Callback invoked with an exception if the deletion fails.
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
     * Fetches a list of trips that are discoverable and do not contain the user in their participants.
     * This method is used to populate the feed of trips that the user can join.
     * @param userId The ID of the user to fetch the feed for.
     * @param onSuccess Callback invoked with the list of trips.
     * @param onFailure Callback invoked with an exception if the fetch fails.
     */
    override fun getFeedForUser(
        userId: String,
        onSuccess: (List<Trip>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(collectionPath)
            .whereEqualTo("discoverable", true)
            .get()
            .addOnSuccessListener { result ->
                val trips = result.map { document -> document.toObject(Trip::class.java) }.filter {
                    it.participants.contains(userId).not()
                }
                onSuccess(trips)
            }
            .addOnFailureListener { exception ->
                Log.e("TripRepositoryFirebase", "Error getting feed for user: ", exception)
                onFailure(exception)
            }
    }

    /**
     * Updates an existing trip in Firestore.
     * @param trip The trip to update.
     * @param onSuccess Callback invoked when the trip is successfully updated.
     * @param onFailure Callback invoked with an exception if the update fails.
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
}
