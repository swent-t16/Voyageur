package com.android.voyageur.model.notifications

import com.google.firebase.firestore.ListenerRegistration

/**
 * Defines the repository interface for managing trip invitations.
 *
 * The `TripInviteRepository` interface provides methods to perform CRUD operations for trip
 * invites, listen for real-time updates, and manage unique identifiers for invites.
 */
interface TripInviteRepository {

  /**
   * Initializes the repository for use.
   *
   * This method sets up the repository, ensuring that it is ready for performing operations.
   *
   * @param onSuccess A callback invoked when initialization is successful.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves all trip invitations sent to a specific user.
   *
   * @param userId The unique identifier of the user.
   * @param onSuccess A callback invoked with a list of `TripInvite` objects when the retrieval is
   *   successful.
   * @param onFailure A callback invoked with an exception if the retrieval fails.
   */
  fun getTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves the count of trip invitations sent to a specific user.
   *
   * @param userId The unique identifier of the user.
   * @param onSuccess A callback invoked with the count of trip invites when the retrieval is
   *   successful.
   * @param onFailure A callback invoked with an exception if the retrieval fails.
   */
  fun getTripInvitesCount(userId: String, onSuccess: (Long) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Creates a new trip invitation.
   *
   * @param req The `TripInvite` object representing the invitation to be created.
   * @param onSuccess A callback invoked when the creation is successful.
   * @param onFailure A callback invoked with an exception if the creation fails.
   */
  fun createTripInvite(req: TripInvite, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a specific trip invitation.
   *
   * @param invite The unique identifier of the trip invite to delete.
   * @param onSuccess A callback invoked when the deletion is successful.
   * @param onFailure A callback invoked with an exception if the deletion fails.
   */
  fun deleteTripInvite(invite: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Generates a new unique identifier for a trip invitation.
   *
   * @return A new unique identifier as a `String`.
   */
  fun getNewId(): String

  /**
   * Listens for real-time updates to the sent trip invitations of a specific user.
   *
   * @param userId The unique identifier of the user.
   * @param onSuccess A callback invoked with a list of updated `TripInvite` objects when changes
   *   occur.
   * @param onFailure A callback invoked with an exception if listening fails.
   * @return A `ListenerRegistration` object to manage the real-time listener.
   */
  fun listenToSentTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration

  /**
   * Listens for real-time updates to the received trip invitations of a specific user.
   *
   * @param userId The unique identifier of the user.
   * @param onSuccess A callback invoked with a list of updated `TripInvite` objects when changes
   *   occur.
   * @param onFailure A callback invoked with an exception if listening fails.
   * @return A `ListenerRegistration` object to manage the real-time listener.
   */
  fun listenToTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration

  /**
   * Retrieves all trip invitations for a specific trip.
   *
   * @param tripId The unique identifier of the trip.
   * @param onSuccess A callback invoked with a list of `TripInvite` objects when the retrieval is
   *   successful.
   * @param onFailure A callback invoked with an exception if the retrieval fails.
   */
  fun getInvitesForTrip(
      tripId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
