package com.android.voyageur.model.trip

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
}
