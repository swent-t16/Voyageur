package com.android.voyageur.model.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserRepositoryFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    internal val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    internal val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(UserRepositoryFirebase.create()) as T
            }
        }
    }
}
