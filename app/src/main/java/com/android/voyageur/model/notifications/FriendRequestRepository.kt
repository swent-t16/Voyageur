package com.android.voyageur.model.notifications

import com.google.firebase.firestore.ListenerRegistration

/**
 * Defines the operations for managing friend requests and notifications in the Voyageur app.
 *
 * This repository interface abstracts the functionality to interact with the backend or database
 * for managing friend requests, such as retrieving, creating, and deleting requests. It also
 * includes real-time listening capabilities for friend request updates.
 */
interface FriendRequestRepository {

  /**
   * Initializes the repository for use, setting up necessary configurations or connections.
   *
   * @param onSuccess A callback invoked upon successful initialization.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves all friend requests sent to a specific user.
   *
   * @param userId The ID of the user whose friend requests should be retrieved.
   * @param onSuccess A callback invoked with the list of friend requests on success.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  fun getFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves the count of notifications for a specific user.
   *
   * @param userId The ID of the user whose notification count is to be retrieved.
   * @param onSuccess A callback invoked with the count of notifications on success.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  fun getNotificationCount(
      userId: String,
      onSuccess: (Long) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Creates a new friend request.
   *
   * @param req The friend request to be created.
   * @param onSuccess A callback invoked upon successful creation of the friend request.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  fun createRequest(req: FriendRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all friend requests sent by a specific user.
   *
   * @param userId The ID of the user who sent the friend requests.
   * @param onSuccess A callback invoked with the list of sent friend requests on success.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  fun getSentFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a friend request by its ID.
   *
   * @param reqId The ID of the friend request to be deleted.
   * @param onSuccess A callback invoked upon successful deletion of the friend request.
   * @param onFailure A callback invoked with an exception if the operation fails.
   */
  fun deleteRequest(reqId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Generates a new unique ID for a friend request.
   *
   * @return A unique string ID for the new friend request.
   */
  fun getNewId(): String

  /**
   * Listens to updates for friend requests sent by a specific user in real-time.
   *
   * @param userId The ID of the user whose sent friend requests should be monitored.
   * @param onSuccess A callback invoked with the list of sent friend requests on updates.
   * @param onFailure A callback invoked with an exception if the operation fails.
   * @return A [ListenerRegistration] that can be used to stop listening for updates.
   */
  fun listenToSentFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration

  /**
   * Listens to updates for friend requests received by a specific user in real-time.
   *
   * @param userId The ID of the user whose received friend requests should be monitored.
   * @param onSuccess A callback invoked with the list of friend requests on updates.
   * @param onFailure A callback invoked with an exception if the operation fails.
   * @return A [ListenerRegistration] that can be used to stop listening for updates.
   */
  fun listenToFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration
}
