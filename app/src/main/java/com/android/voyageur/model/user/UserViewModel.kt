package com.android.voyageur.model.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class UserViewModel(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

  internal val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  internal val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  internal val _searchedUsers = MutableStateFlow<List<User>>(emptyList())
  val searchedUsers: StateFlow<List<User>> = _searchedUsers

  internal val _selectedUser = MutableStateFlow<User?>(null)
  val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

  internal val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  // Job to manage debounce coroutine
  private var debounceJob: Job? = null

  // AuthStateListener to keep track of authentication changes
  private val authStateListener =
      FirebaseAuth.AuthStateListener { auth ->
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
          loadUser(firebaseUser.uid, firebaseUser)
        } else {
          _user.value = null
          _isLoading.value = false
        }
      }

  init {
    // Attach the AuthStateListener
    firebaseAuth.addAuthStateListener(authStateListener)
  }

  override fun onCleared() {
    super.onCleared()
    // Remove the listener when the ViewModel is destroyed
    firebaseAuth.removeAuthStateListener(authStateListener)
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

  fun removeContact(userId: String) {
    val contacts = user.value?.contacts?.toMutableSet() ?: return
    if (contacts.remove(userId)) {
      val updatedUser = user.value!!.copy(contacts = contacts.toList())
      updateUser(updatedUser)
    }
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
    firebaseAuth.signOut()
    // The AuthStateListener will handle setting _user to null
  }

  fun setQuery(query: String) {
    // Cancel any existing debounce job
    debounceJob?.cancel()
    _query.value = query
    // Launch a new debounce job
    debounceJob =
        viewModelScope.launch {
          kotlinx.coroutines.delay(200) // debounce for 200ms
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

  fun updateUserProfilePicture(uri: Uri, onComplete: (String) -> Unit) {
    _isLoading.value = true
    val userId = _user.value?.id ?: return

    userRepository.uploadProfilePicture(
        uri = uri,
        userId = userId,
        onSuccess = { downloadUrl ->
          _isLoading.value = false
          onComplete(downloadUrl)
        },
        onFailure = { exception -> _isLoading.value = false })
  }

  // select user - used in search screen
  fun selectUser(user: User) {
    _selectedUser.value = user
  }

  // deselect user - used in search screen
  fun deselectUser() {
    _selectedUser.value = null
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
