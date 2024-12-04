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
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    private val friendRequestRepository: FriendRequestRepository,
    private val addAuthStateListener: Boolean =
        true // New parameter to control the auth state listener
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

  // Variable to hold the listener registration
  private var userListenerRegistration: ListenerRegistration? = null

  // Listener to monitor authentication state changes
  private val authStateListener =
      FirebaseAuth.AuthStateListener { auth ->
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
          // User is signed in, start listening to user data
          loadUser(firebaseUser.uid, firebaseUser)
          // Fetch sent friend requests
          getSentFriendRequests()
        } else {
          // User is signed out, clear user data and remove listeners
          userListenerRegistration?.remove()
          userListenerRegistration = null
          _user.value = null
          _isLoading.value = false
        }
      }

  init {
    // Attach the authentication state listener to FirebaseAuth instance
    if (addAuthStateListener) {
      // Attach the authentication state listener to FirebaseAuth instance
      firebaseAuth.addAuthStateListener(authStateListener)
    }

    // Observe changes in the user and update contacts accordingly
    viewModelScope.launch {
      user.collectLatest { currentUser ->
        if (currentUser != null) {
          updateContacts(currentUser.contacts)
        } else {
          _contacts.value = emptyList()
        }
      }
    }
  }

  private fun updateContacts(contactIds: List<String>) {
    getUsersByIds(contactIds) { users -> _contacts.value = users }
  }

  override fun onCleared() {
    super.onCleared()
    if (addAuthStateListener) {
      // Remove the authentication listener when ViewModel is destroyed
      firebaseAuth.removeAuthStateListener(authStateListener)
    }
    // Remove Firestore listener
    userListenerRegistration?.remove()
  }

  /**
   * Loads user data from the repository. If the user does not exist, creates a new user profile
   * using Firebase authentication data.
   *
   * @param userId The ID of the user to load.
   * @param firebaseUser Optional Firebase user object for creating a new profile if needed.
   */
  fun loadUser(userId: String, firebaseUser: FirebaseUser? = null) {
    // Remove any existing listener to avoid duplicates
    userListenerRegistration?.remove()

    _isLoading.value = true

    userListenerRegistration =
        userRepository.listenToUser(
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
   * @param secondUserId The ID of the user to remove from contacts.
   */
  fun removeContact(
      secondUserId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    val currentUser = _user.value ?: return onFailure(Exception("Current user is not available"))

    getUsersByIds(listOf(secondUserId)) { users ->
      val secondUser =
          users.firstOrNull() ?: return@getUsersByIds onFailure(Exception("User not found"))

      // Update the current user's contact list
      val updatedCurrentUserContacts =
          currentUser.contacts.toMutableList().apply { remove(secondUserId) }
      val updatedCurrentUser = currentUser.copy(contacts = updatedCurrentUserContacts)

      // Update the second user's contact list
      val updatedSecondUserContacts =
          secondUser.contacts.toMutableList().apply { remove(currentUser.id) }
      val updatedSecondUser = secondUser.copy(contacts = updatedSecondUserContacts)

      // Perform Firestore updates
      updateUser(
          updatedCurrentUser,
          onSuccess = {
            updateUser(
                updatedSecondUser,
                onSuccess = {
                  // Clear lingering friend requests
                  clearFriendRequestState(secondUserId)
                  onSuccess()
                },
                onFailure = { error ->
                  Log.e("REMOVE_CONTACT", "Failed to update second user: ${error.message}")
                  onFailure(error)
                })
          },
          onFailure = { error ->
            Log.e("REMOVE_CONTACT", "Failed to update current user: ${error.message}")
            onFailure(error)
          })
    }
  }

  private fun clearFriendRequestState(userId: String) {
    val requestsToDelete = _friendRequests.value.filter { it.to == userId || it.from == userId }

    requestsToDelete.forEach { request ->
      friendRequestRepository.deleteRequest(
          reqId = request.id,
          onSuccess = {
            Log.d("FRIEND_REQUEST", "Deleted request: ${request.id}")
            _friendRequests.value = _friendRequests.value.filterNot { it.id == request.id }
          },
          onFailure = { exception ->
            Log.e("FRIEND_REQUEST", "Failed to delete request: ${request.id}, ${exception.message}")
          })
    }
  }

  /**
   * Updates user data in the repository.
   *
   * @param updatedUser The updated user data.
   */
  fun updateUser(updatedUser: User, onFailure: (Exception) -> Unit = {}) {
    userRepository.updateUser(
        updatedUser,
        onSuccess = { Log.d("USER_UPDATE", "Successfully updated user: ${updatedUser.id}") },
        onFailure = { error ->
          Log.e("USER_UPDATE", "Failed to update user: $error")
          onFailure(error)
        })
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
   * Updates user data in the repository.
   *
   * @param updatedUser The updated user data.
   * @param onSuccess Callback to execute after a successful update.
   * @param onFailure Callback to execute if the update fails.
   */
  fun updateUser(
      updatedUser: User,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    userRepository.updateUser(
        updatedUser,
        onSuccess = {
          Log.d("USER_UPDATE", "Successfully updated user: ${updatedUser.id}")
          onSuccess()
        },
        onFailure = { error ->
          Log.e("USER_UPDATE", "Failed to update user: $error")
          onFailure(error)
        })
  }

  /**
   * Accepts a friend request by adding the sender to the user's contacts and removing the friend
   * request.
   *
   * @param friendRequest The friend request to accept.
   */
  fun acceptFriendRequest(friendRequest: FriendRequest) {
    val currentUser = _user.value ?: return
    val currentUserId = currentUser.id

    // Add the sender to the current user's contacts
    val updatedContacts = currentUser.contacts.toMutableList().apply { add(friendRequest.from) }
    val updatedUser = currentUser.copy(contacts = updatedContacts)

    updateUser(
        updatedUser,
        onSuccess = {
          // Fetch and update the sender's data
          getUsersByIds(listOf(friendRequest.from)) { users ->
            val sender = users.firstOrNull() ?: return@getUsersByIds
            val senderUpdatedContacts = sender.contacts.toMutableList().apply { add(currentUserId) }
            val updatedSender = sender.copy(contacts = senderUpdatedContacts)

            updateUser(
                updatedSender,
                onSuccess = {
                  // Clear the friend request after successful updates
                  deleteFriendRequest(friendRequest.id)
                },
                onFailure = { error ->
                  Log.e("ACCEPT_REQUEST", "Failed to update sender: ${error.message}")
                })
          }
        },
        onFailure = { error ->
          Log.e("ACCEPT_REQUEST", "Failed to update current user: ${error.message}")
        })
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
  /**
   * Deletes friend request with the corresponding request ID
   *
   * @param reqId the request ID of the friend request to delete
   */
  fun deleteFriendRequest(reqId: String) {
    friendRequestRepository.deleteRequest(
        reqId = reqId,
        onSuccess = {
          Log.d("FRIEND_REQUEST", "Friend request $reqId successfully deleted")
          getFriendRequests { /* Optionally refresh friend requests */}
        },
        onFailure = { exception ->
          Log.e("FRIEND_REQUEST", "Failed to delete friend request: ${exception.message}")
        })
  }
}
