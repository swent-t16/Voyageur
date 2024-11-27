package com.android.voyageur.model.user

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.notifications.FriendRequestRepositoryFirebase
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing user data and interactions with the Firebase authentication
 * and user repository. It provides functionality to load, update, and observe user data, and to
 * handle user-related actions such as signing out, adding/removing contacts, and searching users.
 *
 * @property userRepository The repository used to manage user data.
 * @property firebaseAuth The FirebaseAuth instance used for authentication.
 */
open class UserViewModel(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val friendRequestRepository: FriendRequestRepository
) : ViewModel() {

  /** Flow holding the current user data. */
  internal val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user

  /** Flow holding the current search query. */
  internal val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query

  /** Flow holding the list of searched users based on the current query. */
  internal val _searchedUsers = MutableStateFlow<List<User>>(emptyList())
  val searchedUsers: StateFlow<List<User>> = _searchedUsers

  internal val _allParticipants = MutableStateFlow<List<User>>(emptyList())
  val allParticipants: StateFlow<List<User>> = _allParticipants

  /** Flow holding the currently selected user in the search screen. */
  internal val _selectedUser = MutableStateFlow<User?>(null)
  val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

  /** Flow indicating if a loading process is active. */
  internal val _isLoading = MutableStateFlow(false)

  /** Flow indicating the users from which the current user received a friend request. * */
  internal val _notificationUsers = MutableStateFlow<List<User>>(emptyList())
  val notificationUsers: StateFlow<List<User>> = _notificationUsers

  val _contacts = MutableStateFlow<List<User>>(emptyList())
  val contacts: StateFlow<List<User>> = _contacts
  val _notificationCount = MutableStateFlow<Long>(0)
  val notificationCount: StateFlow<Long> = _notificationCount
  val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
  val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests

  internal val _sentFriendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
  val sentFriendRequests: StateFlow<List<FriendRequest>> = _sentFriendRequests

  val isLoading: StateFlow<Boolean> = _isLoading
  var shouldFetch = true

  // Job to manage debounce coroutine for search queries
  private var debounceJob: Job? = null

  // Listener to monitor authentication state changes
  private val authStateListener =
      FirebaseAuth.AuthStateListener { auth ->
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
          loadUser(firebaseUser.uid, firebaseUser)
          // Fetch sent friend requests
          getSentFriendRequests()
        } else {
          _user.value = null
          _isLoading.value = false
        }
      }

  init {
    // Attach the authentication state listener to FirebaseAuth instance
    firebaseAuth.addAuthStateListener(authStateListener)
  }

  override fun onCleared() {
    super.onCleared()
    // Remove the authentication listener when ViewModel is destroyed
    firebaseAuth.removeAuthStateListener(authStateListener)
  }

  /**
   * Loads user data from the repository. If the user does not exist, creates a new user profile
   * using Firebase authentication data.
   *
   * @param userId The ID of the user to load.
   * @param firebaseUser Optional Firebase user object for creating a new profile if needed.
   */
  fun loadUser(userId: String, firebaseUser: FirebaseUser? = null) {
    _isLoading.value = true
    userRepository.getUserById(
        userId,
        onSuccess = { retrievedUser ->
          _user.value = retrievedUser
          _isLoading.value = false
        },
        onFailure = {
          firebaseUser?.let {
            // Create a new user profile if not found in the repository
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
                onFailure = { _isLoading.value = false })
          }
              ?: run {
                _user.value = null
                _isLoading.value = false
              }
        })
  }

  /**
   * Sends a friend request to the specified user.
   *
   * @param userId The ID of the user to add as a contact.
   */
  fun sendContactRequest(userId: String) {
    friendRequestRepository.createRequest(
        req =
            FriendRequest(
                id = friendRequestRepository.getNewId(),
                from = Firebase.auth.uid.orEmpty(),
                to = userId),
        onSuccess = { getSentFriendRequests() },
        onFailure = {})
  }
  /**
   * Adds a contact to the current user's contact list.
   *
   * @param userId The ID of the user to add as a contact.
   */
  fun addContact(userId: String, friendRequestId: String) {
    val contacts = user.value?.contacts?.toMutableSet()
    val newUser = user.value!!.copy()
    contacts?.add(userId)
    newUser.contacts = contacts?.toList().orEmpty()
    if (user.value != null) {
      updateUser(newUser)
      // Deletes Friend Request since the user has been added as a contact
      deleteFriendRequest(friendRequestId)
    }
  }

  /**
   * Removes a contact from the current user's contact list.
   *
   * @param userId The ID of the user to remove from contacts.
   */
  fun removeContact(userId: String) {
    val contacts = user.value?.contacts?.toMutableSet() ?: return
    if (contacts.remove(userId)) {
      val updatedUser = user.value!!.copy(contacts = contacts.toList())
      updateUser(updatedUser)
      // Reload the user to update the state
      loadUser(updatedUser.id)
    }
  }

  /**
   * Updates user data in the repository.
   *
   * @param updatedUser The updated user data.
   */
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

  /**
   * Signs out the current user from Firebase. The AuthStateListener will automatically handle
   * updating the ViewModel state to reflect the sign-out.
   */
  fun signOutUser() {
    firebaseAuth.signOut()
  }

  /**
   * Sets a search query and initiates a debounce for searching users to reduce redundant searches.
   *
   * @param query The search query string.
   */
  fun setQuery(query: String) {
    debounceJob?.cancel()
    _query.value = query
    debounceJob =
        viewModelScope.launch {
          kotlinx.coroutines.delay(200)
          if (query.isNotEmpty()) {
            searchUsers(query)
          }
        }
  }

  /**
   * Searches for users matching the provided query.
   *
   * @param query The search query string.
   */
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

  /**
   * Updates the user's profile picture by uploading a new image to the repository.
   *
   * @param uri The URI of the new profile picture.
   * @param onComplete A callback function that receives the download URL of the new profile
   *   picture.
   */
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
        onFailure = { _isLoading.value = false })
  }

  /**
   * Selects a user to be viewed in the search screen.
   *
   * @param user The selected `User` object.
   */
  fun selectUser(user: User) {
    _selectedUser.value = user
  }

  /** Deselects the currently selected user in the search screen. */
  fun deselectUser() {
    _selectedUser.value = null
  }

  companion object {
    /** Factory for creating instances of UserViewModel, supplying the required UserRepository. */
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(
                UserRepositoryFirebase.create(),
                friendRequestRepository = FriendRequestRepositoryFirebase.create())
                as T
          }
        }
  }

  fun getNotificationsCount(onSuccess: (Long) -> Unit) {
    friendRequestRepository.getNotificationCount(
        Firebase.auth.uid.orEmpty(),
        {
          if (_notificationCount.value != it) {
            _notificationCount.value = it
          }
          onSuccess(it)
        },
        { Log.e("USER_VIEW_MODEL", it.message.orEmpty()) })
  }

  fun getFriendRequests(onSuccess: (List<FriendRequest>) -> Unit) {
    friendRequestRepository.getFriendRequests(
        Firebase.auth.uid.orEmpty(),
        {
          if (!_friendRequests.value.map { x -> x.id }.containsAll(it.map { x -> x.id }) ||
              it.size != _friendRequests.value.size) {
            Log.d("HELLO", "HELLO COX" + "${_notificationCount.value}")
            getUsersByIds(it.map { x -> x.from }) { users ->
              _friendRequests.value = it
              _notificationUsers.value = users
            }
          }
          onSuccess(it)
        },
        { Log.e("USER_VIEW_MODEL", it.message.orEmpty()) })
  }

  fun getSentRequestId(toUserId: String): String? {
    return sentFriendRequests.value.firstOrNull { it.to == toUserId }?.id
  }

  fun getSentFriendRequests(onSuccess: (List<FriendRequest>) -> Unit = {}) {
    val userId = Firebase.auth.uid.orEmpty()
    friendRequestRepository.getSentFriendRequests(
        userId,
        onSuccess = { requests ->
          _sentFriendRequests.value = requests
          onSuccess(requests)
        },
        onFailure = { exception -> Log.e("USER_VIEW_MODEL", exception.message.orEmpty()) })
  }

  /**
   * Fetches all the users in the give list
   *
   * @param userIds the list of userIDs to fetch
   * @param onSuccess callback for the response
   */
  fun getUsersByIds(userIds: List<String>, onSuccess: (List<User>) -> Unit) {
    _isLoading.value = true
    userRepository.fetchUsersByIds(
        userIds,
        { users ->
          _isLoading.value = false
          onSuccess(users)
        },
        { error ->
          _isLoading.value = false
          Log.e("USER_VIEW_MODEL", error.message.orEmpty())
        })
  }
  /**
   * Fetches all the contacts of the current user and returns them into a list
   *
   * @param onSuccess callback for the response
   */
  fun getMyContacts(onSuccess: (List<User>) -> Unit) {
    if (Firebase.auth.uid == null) return
    userRepository.getContacts(
        Firebase.auth.uid ?: "", onSuccess, { Log.e("USER_VIEW_MODEL", it.message.orEmpty()) })
  }

  fun deleteFriendRequest(reqId: String) {
    friendRequestRepository.deleteRequest(
        reqId = reqId,
        onSuccess = {
          // Request the new friend requests, which will update the state flows
          getFriendRequests {}
          getSentFriendRequests()
        },
        onFailure = { exception ->
          Log.e("USER_VIEW_MODEL", "Failed to delete friend request: ${exception.message}")
        })
  }
}
