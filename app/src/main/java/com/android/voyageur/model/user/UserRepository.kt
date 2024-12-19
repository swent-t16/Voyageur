package com.android.voyageur.model.user

import android.net.Uri
import com.google.firebase.firestore.ListenerRegistration


/**
 * Interface for interacting with the User data in the repository.
 * Provides various methods to perform CRUD operations on User data.
 */
interface UserRepository {
    /**
     * Initializes the User repository, typically used to set up any necessary connections
     * or listeners.
     *
     * @param onSuccess Callback invoked when initialization is successful.
     */
  fun init(onSuccess: () -> Unit)

    /**
     * Searches for users based on a query string.
     *
     * @param query The search query (e.g., name or username).
     * @param onSuccess Callback invoked with the list of matching users.
     * @param onFailure Callback invoked when an error occurs.
     */
  fun searchUsers(query: String, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Fetches a user by their unique identifier.
     *
     * @param id The ID of the user to fetch.
     * @param onSuccess Callback invoked with the fetched User.
     * @param onFailure Callback invoked when an error occurs.
     */
  fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Creates a new user in the repository.
     *
     * @param user The User object to create.
     * @param onSuccess Callback invoked when the user is successfully created.
     * @param onFailure Callback invoked when an error occurs during creation.
     */
  fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Updates an existing user in the repository.
     *
     * @param user The updated User object.
     * @param onSuccess Callback invoked when the user is successfully updated.
     * @param onFailure Callback invoked when an error occurs during update.
     */
  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Deletes a user from the repository by their unique identifier.
     *
     * @param id The ID of the user to delete.
     * @param onSuccess Callback invoked when the user is successfully deleted.
     * @param onFailure Callback invoked when an error occurs during deletion.
     */
  fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Fetches users by a list of user IDs.
     *
     * @param ids The list of user IDs to fetch.
     * @param onSuccess Callback invoked with the list of users corresponding to the provided IDs.
     * @param onFailure Callback invoked when an error occurs during fetching.
     */
  fun fetchUsersByIds(
      ids: List<String>,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

    /**
     * Fetches the contacts of a specific user.
     *
     * @param userId The ID of the user whose contacts are to be fetched.
     * @param onSuccess Callback invoked with the list of contacts for the user.
     * @param onFailure Callback invoked when an error occurs during fetching the contacts.
     */
  fun getContacts(userId: String, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Uploads a profile picture for the user.
     *
     * @param uri The URI of the image to upload.
     * @param userId The ID of the user for whom the profile picture is being uploaded.
     * @param onSuccess Callback invoked with the URL of the uploaded profile picture.
     * @param onFailure Callback invoked when an error occurs during the upload.
     */
  fun uploadProfilePicture(
      uri: Uri,
      userId: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  )

    /**
     * Listens to updates for a specific user.
     *
     * @param userId The ID of the user to listen for updates.
     * @param onSuccess Callback invoked with the updated user data when changes occur.
     * @param onFailure Callback invoked when an error occurs while listening for updates.
     * @return A [ListenerRegistration] object that can be used to stop listening for updates.
     * @see ListenerRegistration
     */
  fun listenToUser(
      userId: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration?
}
