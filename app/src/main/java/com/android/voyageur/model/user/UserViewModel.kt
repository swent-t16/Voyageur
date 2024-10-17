package com.android.voyageur.model.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserRepositoryFirebase
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  private val _searchedUsers = MutableStateFlow<List<User>>(emptyList())
  val searchedUsers: StateFlow<List<User>> = _searchedUsers

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  // Job to manage debounce coroutine
  private var debounceJob: Job? = null

  init {
    // Load the currently authenticated user if available
    val currentUser = FirebaseAuth.getInstance().currentUser
    currentUser?.let { loadUser(it.uid, it) }
  }

    init {
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            loadUser(currentUser.uid, currentUser)
        }
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
                firebaseUser?.let { createUserFromFirebase(it) } ?: run {
                    _user.value = null
                    _isLoading.value = false
                }
            }
        )
    }

    private fun createUserFromFirebase(firebaseUser: FirebaseUser) {
        val newUser = User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: DEFAULT_NAME,
            email = firebaseUser.email ?: DEFAULT_EMAIL,
            profilePicture = firebaseUser.photoUrl?.toString() ?: "",
            bio = ""
        )
        userRepository.createUser(
            newUser,
            onSuccess = {
                _user.value = newUser
                _isLoading.value = false
            },
            onFailure = {
                _user.value = null
                _isLoading.value = false
            }
        )
    }

    fun updateUser(updatedUser: User) {
        _isLoading.value = true
        userRepository.updateUser(
            updatedUser,
            onSuccess = {
                _user.value = updatedUser
                _isLoading.value = false
            },
            onFailure = {
                _isLoading.value = false
            }
        )
    }

    fun signOutUser() {
        FirebaseAuth.getInstance().signOut()
        _user.value = null
    }

    companion object {
        private const val DEFAULT_NAME = "Unknown"
        private const val DEFAULT_EMAIL = "No Email"

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
