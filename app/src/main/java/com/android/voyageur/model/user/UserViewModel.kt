package com.android.voyageur.model.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
  internal val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  internal val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  internal val _searchedUsers = MutableStateFlow<List<User>>(emptyList())
  val searchedUsers: StateFlow<List<User>> = _searchedUsers

  internal val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  // Job to manage debounce coroutine
  private var debounceJob: Job? = null

  init {
    // Load the currently authenticated user if available
    val currentUser = FirebaseAuth.getInstance().currentUser
    currentUser?.let { loadUser(it.uid, it) }
  }

  fun loadUser(userId: String, firebaseUser: FirebaseUser? = null) {
    _isLoading.value = true
    userRepository.getUserById(
        userId,
        onSuccess = { retrievedUser ->
          _user.value = retrievedUser
          _isLoading.value = false
        },
        onFailure = {
          // If user does not exist, create a new user from FirebaseAuth data if available
          firebaseUser?.let {
            val newUser =
                User(
                    id = it.uid,
                    name = it.displayName ?: "Unknown",
                    email = it.email ?: "No Email",
                    profilePicture = it.photoUrl?.toString() ?: "",
                    bio = "",
                    username = it.email?.split("@")?.get(0) ?: "")
            userRepository.createUser(
                newUser,
                onSuccess = {
                  _user.value = newUser
                  _isLoading.value = false
                },
                onFailure = { exception ->
                  _user.value = null
                  _isLoading.value = false
                })
          }
              ?: run {
                _user.value = null
                _isLoading.value = false
              }
        })
  }

  fun addContact(userId: String) {
    val contacts = user.value?.contacts?.toMutableSet()
    val newUser = user.value!!.copy()
    contacts?.add(userId)
    newUser.contacts = contacts?.toList().orEmpty()
    if (user.value != null) updateUser(newUser)
  }

  fun updateUser(updatedUser: User) {
    _isLoading.value = true
    userRepository.updateUser(
        updatedUser,
        onSuccess = {
          _user.value = updatedUser
          _isLoading.value = false
        },
        onFailure = { _isLoading.value = false })
  }

  fun signOutUser() {
    FirebaseAuth.getInstance().signOut()
    _user.value = null
  }

  fun setQuery(query: String) {
    // Cancel any existing debounce job
    debounceJob?.cancel()
    _query.value = query
    // Launch a new debounce job
    debounceJob =
        viewModelScope.launch {
          delay(200) // debounce for 2s
          if (query.isNotEmpty()) {
            searchUsers(query)
          }
        }
  }

  fun searchUsers(query: String) {
    _isLoading.value = true
    userRepository.searchUsers(
        query,
        onSuccess = { users ->
          _searchedUsers.value = users
          _isLoading.value = false
        },
        onFailure = { _isLoading.value = false })
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(UserRepositoryFirebase.create()) as T
          }
        }
  }
}
