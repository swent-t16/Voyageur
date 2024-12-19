package com.android.voyageur.model.user

import android.net.Uri
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

/**
 * Firebase implementation of the [UserRepository] interface. Handles operations related to user
 * data in Firebase Firestore and Firebase Storage.
 */
class UserRepositoryFirebase(private val db: FirebaseFirestore) : UserRepository {
  private val collectionPath = "users"
  private val storage = FirebaseStorage.getInstance()

  /**
   * Fetches a user from Firebase Firestore by their unique ID.
   *
   * @param id The unique ID of the user.
   * @param onSuccess Callback invoked with the fetched [User] object.
   * @param onFailure Callback invoked when an error occurs.
   */
  override fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          try {
            val user = document.toObject(User::class.java)
            if (user != null) {
              onSuccess(user)
            } else {
              onFailure(Exception("User not found"))
            }
          } catch (e: Exception) {
            onFailure(e)
          }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Initializes the user repository by fetching the users collection.
   *
   * @param onSuccess Callback invoked upon successful initialization.
   */
  override fun init(onSuccess: () -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> }
  }

  /**
   * Creates a new user in Firebase Firestore.
   *
   * @param user The [User] object to be created.
   * @param onSuccess Callback invoked upon successful user creation.
   * @param onFailure Callback invoked when an error occurs during user creation.
   */
  override fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(user.id)
        .set(user, SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Updates an existing user in Firebase Firestore.
   *
   * @param user The updated [User] object.
   * @param onSuccess Callback invoked upon successful update.
   * @param onFailure Callback invoked when an error occurs during update.
   */
  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(user.id)
        .set(user, SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Deletes a user from Firebase Firestore by their unique ID.
   *
   * @param id The unique ID of the user to delete.
   * @param onSuccess Callback invoked upon successful deletion.
   * @param onFailure Callback invoked when an error occurs during deletion.
   */
  override fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Fetches all the users in the give list
   *
   * @param ids the list of userIDs to fetch
   * @param onSuccess callback for the response
   * @param onFailure callback for error handling
   */
  override fun fetchUsersByIds(
      ids: List<String>,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (ids.isEmpty()) onSuccess(listOf())
    else
        db.collection(collectionPath)
            .whereIn(FieldPath.documentId(), ids)
            .get()
            .addOnSuccessListener { documents ->
              // Convert the query result to a list of User objects
              val users = documents.toObjects(User::class.java)
              onSuccess(users)
            }
            .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Fetches all the contacts of the user and returns them into a list
   *
   * @param userId The id of the user to fetch the contacts of
   * @param onSuccess callback for the response
   * @param onFailure callback for error handling
   */
  override fun getContacts(
      userId: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
          val user = document.toObject(User::class.java)
          if (user != null) {
            fetchUsersByIds(user.contacts, onSuccess, onFailure)
          } else {
            onFailure(Exception("User not found"))
          }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Searches for users based on a query string.
   *
   * @param query The search query (e.g., name or username).
   * @param onSuccess Callback invoked with the list of matching users.
   * @param onFailure Callback invoked when an error occurs during the search.
   */
  override fun searchUsers(
      query: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .orderBy("name")
        .get()
        .addOnSuccessListener { documents ->
          val users =
              documents.toObjects(User::class.java).filter {
                it.name.contains(query, ignoreCase = true)
              }
          onSuccess(users)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Uploads a profile picture to Firebase Storage.
   *
   * @param uri The URI of the profile picture to upload.
   * @param userId The ID of the user for whom the profile picture is uploaded.
   * @param onSuccess Callback invoked with the download URL of the uploaded profile picture.
   * @param onFailure Callback invoked when an error occurs during the upload.
   */
  override fun uploadProfilePicture(
      uri: Uri,
      userId: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
    val uploadTask = storageRef.putFile(uri)

    uploadTask
        .addOnSuccessListener {
          storageRef.downloadUrl
              .addOnSuccessListener { downloadUri -> onSuccess(downloadUri.toString()) }
              .addOnFailureListener { exception -> onFailure(exception) }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Generates a new unique user ID.
   *
   * @return A newly generated unique user ID as a string.
   */
  fun getNewUserId(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Listens to real-time changes to a user's data in Firestore.
   *
   * @param userId The ID of the user to listen to.
   * @param onSuccess Callback invoked with the updated user data when changes occur.
   * @param onFailure Callback invoked when an error occurs during the snapshot listener.
   * @return A [ListenerRegistration] object to manage the listener.
   * @see ListenerRegistration
   */
  override fun listenToUser(
      userId: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration? {
    return db.collection("users").document(userId).addSnapshotListener { snapshot, exception ->
      if (exception != null) {
        onFailure(exception)
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        val user = snapshot.toObject(User::class.java)
        if (user != null) {
          onSuccess(user)
        } else {
          onFailure(Exception("User data is null"))
        }
      } else {
        onFailure(Exception("Snapshot is null or does not exist"))
      }
    }
  }

  companion object {
    /**
     * Factory method to create an instance of [UserRepositoryFirebase].
     *
     * @return A new instance of [UserRepositoryFirebase].
     */
    fun create(): UserRepositoryFirebase {
      val db = FirebaseFirestore.getInstance()
      return UserRepositoryFirebase(db)
    }
  }
}
