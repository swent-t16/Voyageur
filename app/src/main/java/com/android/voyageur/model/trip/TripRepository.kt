package com.android.voyageur.model.trip

import com.google.firebase.firestore.ListenerRegistration

interface TripRepository {
  fun getNewTripId(): String

  fun init(onSuccess: () -> Unit)

  fun getTrips(
      creator: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit,
  )

  fun createTrip(
      trip: Trip,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
  )

  fun updateTrip(
      trip: Trip,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
  )

  fun deleteTripById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
  )

  fun getFeed(
      userId: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit,
  )

  fun listenForTripUpdates(
      userId: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit,
  ): ListenerRegistration

  fun getTripById(
      tripId: String,
      onSuccess: (Trip) -> Unit,
      onFailure: (Exception) -> Unit,
  )
}
