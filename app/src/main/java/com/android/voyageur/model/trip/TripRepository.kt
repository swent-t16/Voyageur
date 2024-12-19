package com.android.voyageur.model.trip

import com.google.firebase.firestore.ListenerRegistration

/**
 * Interface for interacting with trip data, providing methods for managing trips in the system.
 * Methods include fetching trips, creating, updating, deleting trips, and listening for updates.
 */
interface TripRepository {

  /**
   * Generates a new unique ID for a trip.
   *
   * @return A string representing the new trip ID.
   */
  fun getNewTripId(): String

  /**
   * Initializes the repository, typically for setting up necessary resources or configurations.
   *
   * @param onSuccess Callback that is triggered when initialization is successful.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves a list of trips associated with a specific creator.
   *
   * @param creator The ID of the creator whose trips are to be fetched.
   * @param onSuccess Callback that is triggered with the list of trips if the operation is
   *   successful.
   * @param onFailure Callback that is triggered if the operation fails, with the exception details.
   */
  fun getTrips(
      creator: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit,
  )

  /**
   * Creates a new trip in the repository.
   *
   * @param trip The `Trip` object containing the details of the new trip.
   * @param onSuccess Callback that is triggered if the trip is successfully created.
   * @param onFailure Callback that is triggered if the operation fails, with the exception details.
   */
  fun createTrip(
      trip: Trip,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
  )

  /**
   * Updates an existing trip with the provided details.
   *
   * @param trip The `Trip` object containing the updated information.
   * @param onSuccess Callback that is triggered if the trip is successfully updated.
   * @param onFailure Callback that is triggered if the operation fails, with the exception details.
   */
  fun updateTrip(
      trip: Trip,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
  )

  /**
   * Deletes a trip by its ID.
   *
   * @param id The ID of the trip to be deleted.
   * @param onSuccess Callback that is triggered if the trip is successfully deleted.
   * @param onFailure Callback that is triggered if the operation fails, with the exception details.
   */
  fun deleteTripById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
  )

  /**
   * Retrieves a list of trips associated with a specific user, typically for a feed or list view.
   *
   * @param userId The ID of the user whose feed is to be fetched.
   * @param onSuccess Callback that is triggered with the list of trips if the operation is
   *   successful.
   * @param onFailure Callback that is triggered if the operation fails, with the exception details.
   */
  fun getFeed(
      userId: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit,
  )

  /**
   * Listens for updates to trips for a specific user. This can be used for real-time updates or
   * changes.
   *
   * @param userId The ID of the user whose trip updates are to be listened to.
   * @param onSuccess Callback that is triggered with the list of updated trips if the operation is
   *   successful.
   * @param onFailure Callback that is triggered if the operation fails, with the exception details.
   * @return A `ListenerRegistration` object that can be used to unregister the listener when no
   *   longer needed.
   */
  fun listenForTripUpdates(
      userId: String,
      onSuccess: (List<Trip>) -> Unit,
      onFailure: (Exception) -> Unit,
  ): ListenerRegistration

  /**
   * Retrieves a specific trip by its ID.
   *
   * @param tripId The ID of the trip to be fetched.
   * @param onSuccess Callback that is triggered with the `Trip` object if the operation is
   *   successful.
   * @param onFailure Callback that is triggered if the operation fails, with the exception details.
   */
  fun getTripById(
      tripId: String,
      onSuccess: (Trip) -> Unit,
      onFailure: (Exception) -> Unit,
  )
}
