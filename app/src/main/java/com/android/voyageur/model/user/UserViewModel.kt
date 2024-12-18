package com.android.voyageur.model.user

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.voyageur.R
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.notifications.FriendRequestRepositoryFirebase
import com.android.voyageur.model.notifications.TripInvite
import com.android.voyageur.ui.notifications.NotificationProvider
import com.android.voyageur.ui.notifications.StringProvider
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
 * ViewModel responsible for managing user data and interactions with Firebase authentication and
 * user repository. It provides functionality to load, update, and observe user data, and to handle
 * user-related actions such as signing out, adding/removing contacts, and searching users.
 *
 * This ViewModel is tightly integrated with Firebase services and manages real-time updates to user
 * data and friend requests using Firebase Firestore listeners.
 *
 * @param userRepository The repository used to manage user data.
 * @param firebaseAuth The FirebaseAuth instance used for authentication.
 * @param friendRequestRepository The repository used to manage friend requests.
 * @param stringProvider Optional provider for string resources.
 * @param notificationProvider Optional provider for notifications.
 * @param addAuthStateListener Flag indicating whether to add an auth state listener.
 * @constructor Creates an instance of [UserViewModel].
 */
open class UserViewModel
@SuppressLint("StaticFieldLeak")
constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val friendRequestRepository: FriendRequestRepository,
    private val stringProvider: StringProvider? = null,
    private val notificationProvider: NotificationProvider? = null,
    private val addAuthStateListener: Boolean = true
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

  /** Flow holding all participants. */
  internal val _allParticipants = MutableStateFlow<List<User>>(emptyList())
  val allParticipants: StateFlow<List<User>> = _allParticipants

  /** Flow holding the currently selected user in the search screen. */
  internal val _selectedUser = MutableStateFlow<User?>(null)
  val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

  /** Flow indicating if a loading process is active. */
  internal val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  /** Flow holding the users from which the current user received a friend request. */
  internal val _notificationUsers = MutableStateFlow<List<User>>(emptyList())
  val notificationUsers: StateFlow<List<User>> = _notificationUsers

  /** Flow holding the contacts of the current user. */
  val _contacts = MutableStateFlow<List<User>>(emptyList())
  val contacts: StateFlow<List<User>> = _contacts

  /** Flow holding the notification count. */
  val _notificationCount = MutableStateFlow<Long>(0)
  val notificationCount: StateFlow<Long> = _notificationCount

  /** Flow holding the friend requests received by the current user. */
  val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
  val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests

  /** Flow holding the friend requests sent by the current user. */
  internal val _sentFriendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
  val sentFriendRequests: StateFlow<List<FriendRequest>> = _sentFriendRequests

  // Add this variable to manage the listener
  private var sentFriendRequestsListener: ListenerRegistration? = null

  var shouldFetch = true

  // Job to manage debounce coroutine for search queries
  private var debounceJob: Job? = null

  // Variable to hold the listener registration
  var userListenerRegistration: ListenerRegistration? = null

  var friendRequestsListener: ListenerRegistration? = null

  // Listener to monitor authentication state changes
  private val authStateListener =
      FirebaseAuth.AuthStateListener { auth ->
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
          // User is signed in, start listening to user data
          loadUser(firebaseUser.uid, firebaseUser)
          // Listen to friend requests (incoming)
          listenToFriendRequests()
          // Listen to friend requests (sent)
          getSentFriendRequests()
        } else {
          // User is signed out, clear data and remove listeners
          userListenerRegistration?.remove()
          userListenerRegistration = null
          _user.value = null
          _isLoading.value = false
          sentFriendRequestsListener?.remove()
          sentFriendRequestsListener = null
          _sentFriendRequests.value = emptyList()

          // Remove friend requests listener
          friendRequestsListener?.remove()
          friendRequestsListener = null
          _friendRequests.value = emptyList()
          _notificationUsers.value = emptyList()
        }
      }

  init {
    // Attach the authentication state listener to FirebaseAuth instance
    if (addAuthStateListener) {
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

  /**
   * Updates the contacts of the current user.
   *
   * @param contactIds List of contact IDs to update.
   */
  fun updateContacts(contactIds: List<String>) {
    getUsersByIds(contactIds) { users -> _contacts.value = users }
  }

  public override fun onCleared() {
    super.onCleared()
    if (addAuthStateListener) {
      // Remove the authentication listener when ViewModel is destroyed
      firebaseAuth.removeAuthStateListener(authStateListener)
    }
    // Remove Firestore listener
    userListenerRegistration?.remove()
    sentFriendRequestsListener?.remove()
    sentFriendRequestsListener = null
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
                    (it.displayName ?: stringProvider?.getString(R.string.unknown))?.let { it1 ->
                      (it.email ?: stringProvider?.getString(R.string.no_email))?.let { it2 ->
                        (it.email?.split("@")?.get(0)
                                ?: stringProvider?.getString(R.string.unknown))
                            ?.let { it3 ->
                              User(
                                  id = it.uid,
                                  name = it1,
                                  email = it2,
                                  profilePicture = it.photoUrl?.toString() ?: "",
                                  bio = "",
                                  username = it3)
                            }
                      }
                    }
                if (newUser != null) {
                  userRepository.createUser(
                      newUser,
                      onSuccess = {
                        _user.value = newUser
                        _isLoading.value = false
                      },
                      onFailure = { _isLoading.value = false })
                }
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
   * @param friendRequestId The ID of the friend request to delete after adding the contact.
   */
  fun addContact(userId: String, friendRequestId: String) {
    val currentUser = user.value ?: return
    val contacts = currentUser.contacts.toMutableSet()
    contacts.add(userId)
    val newUser = currentUser.copy(contacts = contacts.toList().orEmpty())
    updateUser(newUser)
    // Deletes Friend Request since the user has been added as a contact
    deleteFriendRequest(friendRequestId)
  }

  /**
   * Removes a contact from the current user's contact list.
   *
   * @param secondUserId The ID of the user to remove from contacts.
   * @param onSuccess Callback to execute after successful removal.
   * @param onFailure Callback to execute if the removal fails.
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

  /**
   * Clears the friend request state for a specific user.
   *
   * @param userId The ID of the user to clear friend requests for.
   */
  fun clearFriendRequestState(userId: String) {
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
   * @param onFailure Callback to execute if the update fails.
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
                  // Mark the request as accepted to trigger the sender's notification
                  val acceptedRequest = friendRequest.copy(accepted = true)
                  friendRequestRepository.createRequest(
                      acceptedRequest,
                      onSuccess = {
                        // Remove the old request from this device's local state immediately
                        _friendRequests.value =
                            _friendRequests.value.filterNot { it.id == friendRequest.id }

                        // After a short delay, delete the accepted request from Firestore
                        // This gives the sender time to see the acceptance state and show a
                        // notification
                        viewModelScope.launch {
                          kotlinx.coroutines.delay(1000)
                          friendRequestRepository.deleteRequest(
                              reqId = acceptedRequest.id,
                              onSuccess = {},
                              onFailure = { e ->
                                Log.e(
                                    "ACCEPT_REQUEST",
                                    "Failed to delete friend request: ${e.message}")
                              })
                        }
                      },
                      onFailure = { error ->
                        Log.e(
                            "ACCEPT_REQUEST", "Failed to create accepted request: ${error.message}")
                      })
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
    fun provideFactory(
        stringProvider: StringProvider,
        notificationProvider: NotificationProvider
    ): ViewModelProvider.Factory {
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return UserViewModel(
              userRepository = UserRepositoryFirebase.create(),
              firebaseAuth = FirebaseAuth.getInstance(),
              friendRequestRepository = FriendRequestRepositoryFirebase.create(),
              addAuthStateListener = true,
              stringProvider = stringProvider,
              notificationProvider = notificationProvider)
              as T
        }
      }
    }
  }

  /**
   * Retrieves the notification count for the current user.
   *
   * @param onSuccess Callback to execute after successful retrieval.
   */
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

  /**
   * Retrieves the friend requests for the current user.
   *
   * @param onSuccess Callback to execute after successful retrieval.
   */
  fun getFriendRequests(onSuccess: (List<FriendRequest>) -> Unit) {
    friendRequestRepository.getFriendRequests(
        Firebase.auth.uid.orEmpty(),
        { requests ->
          _friendRequests.value = requests
          getUsersByIds(requests.map { it.from }) { users -> _notificationUsers.value = users }
          onSuccess(requests)
        },
        { Log.e("USER_VIEW_MODEL", it.message.orEmpty()) })
  }

  /**
   * Retrieves the ID of the sent friend request to a specific user.
   *
   * @param toUserId The ID of the user to whom the friend request was sent.
   * @return The ID of the sent friend request, or null if not found.
   */
  fun getSentRequestId(toUserId: String): String? {
    return sentFriendRequests.value.firstOrNull { it.to == toUserId }?.id
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
    _sentFriendRequests.value = _sentFriendRequests.value.filterNot { it.id == reqId }
    friendRequestRepository.deleteRequest(
        reqId = reqId,
        onSuccess = { Log.d("FRIEND_REQUEST", "Friend request $reqId successfully deleted") },
        onFailure = { exception ->
          Log.e("FRIEND_REQUEST", "Failed to delete friend request: ${exception.message}")
        })
  }
  /**
   * Sets up a listener for incoming friend requests so that the UI updates in real-time. Now also
   * triggers a notification if a new friend request arrives.
   */
  fun listenToFriendRequests() {
    val userId = Firebase.auth.uid.orEmpty()
    if (userId.isEmpty()) return

    friendRequestsListener?.remove()

    friendRequestsListener =
        friendRequestRepository.listenToFriendRequests(
            userId = userId,
            onSuccess = { requests ->
              val oldRequests = _friendRequests.value
              _friendRequests.value = requests
              _notificationCount.value = requests.size.toLong()

              getUsersByIds(requests.map { it.from }) { users ->
                _notificationUsers.value = users

                // Check if there's a new request (list got bigger)
                if (requests.size > oldRequests.size) {
                  val oldRequestIds = oldRequests.map { it.id }.toSet()
                  val newRequests = requests.filterNot { oldRequestIds.contains(it.id) }

                  if (newRequests.isNotEmpty()) {
                    val newRequest = newRequests.first()
                    val senderUser = users.find { it.id == newRequest.from }
                    val senderName = senderUser?.name ?: stringProvider?.getString(R.string.unknown)

                    if (senderName != null) {
                      notificationProvider?.showNewFriendRequestNotification(senderName)
                    }
                  }
                }
              }
            },
            onFailure = { exception ->
              Log.e("USER_VIEW_MODEL", "Failed to listen to friend requests: ${exception.message}")
            })
  }

  /**
   * Sets up a listener for sent friend requests so that the UI updates in real-time. This method
   * also triggers a notification if a sent friend request is accepted.
   */
  fun getSentFriendRequests() {
    val userId = Firebase.auth.uid.orEmpty()
    if (userId.isEmpty()) return

    val oldSentRequests = _sentFriendRequests.value
    sentFriendRequestsListener?.remove()
    sentFriendRequestsListener =
        friendRequestRepository.listenToSentFriendRequests(
            userId = userId,
            onSuccess = { requests ->
              val oldRequestsMap = oldSentRequests.associateBy { it.id }
              _sentFriendRequests.value = requests

              // Find requests that have just transitioned to accepted = true
              val newlyAccepted =
                  requests.filter { newReq ->
                    newReq.accepted && oldRequestsMap[newReq.id]?.accepted == false
                  }

              if (newlyAccepted.isNotEmpty()) {
                newlyAccepted.forEach { acceptedReq ->
                  // Use the optimized `getUserById` instead of `getUsersByIds`
                  userRepository.getUserById(
                      id = acceptedReq.to,
                      onSuccess = { acceptorUser ->
                        val acceptorName =
                            acceptorUser.name ?: stringProvider?.getString(R.string.unknown)

                        // Use NotificationProvider to show acceptance notification
                        if (acceptorName != null) {
                          notificationProvider?.showFriendRequestAcceptedNotification(acceptorName)
                        }

                        // After showing the notification, delete the request
                        friendRequestRepository.deleteRequest(
                            reqId = acceptedReq.id,
                            onSuccess = {},
                            onFailure = { exception ->
                              Log.e(
                                  "FRIEND_REQUEST",
                                  "Failed to delete request: ${exception.message}")
                            })
                      },
                      onFailure = { exception ->
                        Log.e("FRIEND_REQUEST", "Failed to fetch user: ${exception.message}")
                      })
                }
              }
            },
            onFailure = { exception -> Log.e("USER_VIEW_MODEL", exception.message.orEmpty()) })
  }

  fun resolveTripInviteUsers(
      invites: List<TripInvite>,
      onResolved: (List<Pair<TripInvite, User>>) -> Unit
  ) {
    val userIds = invites.map { it.from }.distinct()
    getUsersByIds(userIds) { users ->
      val inviteUserPairs =
          invites.mapNotNull { invite ->
            val user = users.find { it.id == invite.from }
            if (user != null) Pair(invite, user) else null
          }
      onResolved(inviteUserPairs)
    }
  }
}
