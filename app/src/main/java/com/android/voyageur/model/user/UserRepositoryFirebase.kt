package com.android.voyageur.model.user

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class UserRepositoryFirebase(private val db: FirebaseFirestore) : UserRepository {
  private val collectionPath = "users"
  private val storage = FirebaseStorage.getInstance()

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

  override fun init(onSuccess: () -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> }
  }

  override fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(user.id)
        .set(user, SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(user.id)
        .set(user, SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

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

  fun getNewUserId(): String {
    return db.collection(collectionPath).document().id
  }

  companion object {
    fun create(): UserRepositoryFirebase {
      val db = FirebaseFirestore.getInstance()
      return UserRepositoryFirebase(db)
    }
  }
}
