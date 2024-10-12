package com.android.voyageur.model.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel(private val userRepository: UserRepositoryFirebase) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Load the currently authenticated user if available
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            loadUser(it.uid, it)
        }
    }

    fun loadUser(userId: String, firebaseUser: FirebaseUser? = null) {
        _isLoading.value = true
        userRepository.getUserById(userId,
            onSuccess = { retrievedUser ->
                _user.value = retrievedUser
                _isLoading.value = false
            },
            onFailure = {
                // If user does not exist, create a new user from FirebaseAuth data if available
                firebaseUser?.let {
                    val newUser = User(
                        id = it.uid,
                        name = it.displayName ?: "Unknown",
                        email = it.email ?: "No Email",
                        profilePicture = it.photoUrl?.toString() ?: "",
                        bio = ""
                    )
                    userRepository.createUser(newUser,
                        onSuccess = {
                            _user.value = newUser
                            _isLoading.value = false
                        },
                        onFailure = { exception ->
                            _user.value = null
                            _isLoading.value = false
                        }
                    )
                } ?: run {
                    _user.value = null
                    _isLoading.value = false
                }
            }
        )
    }

    fun updateUser(updatedUser: User) {
        _isLoading.value = true
        userRepository.updateUser(updatedUser,
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
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return UserViewModel(UserRepositoryFirebase.create()) as T
                }
            }
    }
}