package com.android.voyageur.model.trip

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TripRepositoryFirebase(private val db: FirebaseFirestore) : TripRepository {
    private val collectionPath = "trips"

    override fun getNewTripId(): String {
        return db.collection(collectionPath).document().id
    }

    override fun init(onSuccess: () -> Unit) {
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                Log.e("TripRepositoryFirebase", "Error initializing repository: ", exception)
            }
    }

    override fun getTrips(onSuccess: (List<Trip>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { result ->
                val trips = result.mapNotNull { document ->
                    try {
                        val trip = document.toObject(Trip::class.java)
                        trip.copy(id = document.id) // Ensure the ID is set
                    } catch (e: Exception) {
                        Log.e("TripRepositoryFirebase", "Error parsing trip: ", e)
                        null
                    }
                }
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
            .addOnFailureListener { exception: Exception ->
                Log.e("TripRepositoryFirebase", "Error creating trip: ", exception)
                onFailure(exception)
            }
    }

    override fun deleteTripById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception: Exception ->
                Log.e("TripRepositoryFirebase", "Error deleting trip: ", exception)
                onFailure(exception)
            }
    }

    override fun updateTrip(trip: Trip, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection(collectionPath)
            .document(trip.id)
            .set(trip, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception: Exception ->
                Log.e("TripRepositoryFirebase", "Error updating trip: ", exception)
                onFailure(exception)
            }
    }
}