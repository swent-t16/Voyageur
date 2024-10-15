package com.android.voyageur.model.user

interface UserRepository {
  fun init(onSuccess: () -> Unit)

  fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
