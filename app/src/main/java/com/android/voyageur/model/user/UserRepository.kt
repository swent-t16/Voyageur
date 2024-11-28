package com.android.voyageur.model.user

import android.net.Uri
import com.google.firebase.firestore.ListenerRegistration

interface UserRepository {
  fun init(onSuccess: () -> Unit)

  fun searchUsers(query: String, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun fetchUsersByIds(
      ids: List<String>,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getContacts(userId: String, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  fun uploadProfilePicture(
      uri: Uri,
      userId: String,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  )
    fun listenToUser(
        userId: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration?
}
