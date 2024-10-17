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

  override fun getTrips(
      creator: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("creator", creator)
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
