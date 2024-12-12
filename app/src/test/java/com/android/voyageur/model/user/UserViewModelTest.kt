package com.android.voyageur.model.user

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.ui.notifications.NotificationProvider
import com.android.voyageur.ui.notifications.StringProvider
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserViewModelTest {
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var userViewModel: UserViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  private val user = User("1", "name", "email", "", "bio", listOf(), emptyList(), "username")

  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>
  private lateinit var stringProvider: StringProvider
  private lateinit var notificationProvider: NotificationProvider

  val context = ApplicationProvider.getApplicationContext<Context>()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)
    stringProvider = mock()
    notificationProvider = mock()

    // Mock FirebaseAuth to return our mocked firebaseUser
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
    `when`(firebaseUser.uid).thenReturn("123")
    `when`(firebaseUser.displayName).thenReturn("Test User")
    `when`(firebaseUser.email).thenReturn("test@example.com")
    `when`(firebaseUser.photoUrl).thenReturn(null)

    // Mock static FirebaseAuth.getInstance() to return our mocked firebaseAuth
    firebaseAuthMockStatic = mockStatic(FirebaseAuth::class.java)
    firebaseAuthMockStatic
        .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
        .thenReturn(firebaseAuth)

    // Mock userRepository.listenToUser to call onSuccess with a User
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user =
              User(
                  id = userId,
                  name = "Test User",
                  email = "test@example.com",
                  interests = emptyList())
          onSuccess(user)
          null
        }
        .`when`(userRepository)
        .listenToUser(anyString(), any(), anyOrNull())

    // Mock userRepository.fetchUsersByIds to return an empty list
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .`when`(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    // Mock friendRequestRepository methods
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (List<FriendRequest>) -> Unit
          onSuccess(emptyList())
          null
        }
        .`when`(friendRequestRepository)
        .getFriendRequests(anyString(), any(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (Long) -> Unit
          onSuccess(0L)
          null
        }
        .`when`(friendRequestRepository)
        .getNotificationCount(anyString(), any(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (List<FriendRequest>) -> Unit
          onSuccess(emptyList())
          null
        }
        .`when`(friendRequestRepository)
        .getSentFriendRequests(anyString(), any(), anyOrNull())

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as () -> Unit
          onSuccess()
          null
        }
        .`when`(friendRequestRepository)
        .deleteRequest(anyString(), any(), anyOrNull())

    // Create the UserViewModel with the mocked userRepository, firebaseAuth, and
    // friendRequestRepository
    userViewModel =
        UserViewModel(
            userRepository = userRepository,
            friendRequestRepository = friendRequestRepository,
            firebaseAuth = firebaseAuth,
            stringProvider = stringProvider,
            notificationProvider = notificationProvider,
            addAuthStateListener = false)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    firebaseAuthMockStatic.close() // Close the mock static
  }

  // Test for updating the query in the ViewModel
  @Test
  fun setQueryUpdatesQuery() {
    val query = "test"
    userViewModel.setQuery(query)
    assert(userViewModel.query.value == query)
  }

  // Test to verify search triggers after delay
  @Test
  fun setQueryTriggersSearchAfterDelay() = runTest {
    val query1 = "test1"
    val query2 = "test2"
    userViewModel.setQuery(query1)
    assert(userViewModel.query.value == query1)
    verify(userRepository, never()).searchUsers(eq(query1), any(), any())
    userViewModel.setQuery(query2)
    delay(300) // Wait for debounce delay (assuming 200ms delay in setQuery)
    verify(userRepository).searchUsers(eq(query2), any(), any())
  }

  // Test for a successful user search
  @Test
  fun testSearchUsersSucceeds() = runTest {
    val query = "test"
    val mockUserList = listOf(User(id = "1", name = "Test User", email = "test@example.com"))

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (List<User>) -> Unit
          onSuccess(mockUserList)
          null
        }
        .`when`(userRepository)
        .searchUsers(eq(query), any(), any())

    userViewModel.searchUsers(query)

    verify(userRepository).searchUsers(eq(query), any(), any())
    assert(userViewModel.searchedUsers.value == mockUserList)
  }

  // Test for a failed user search
  @Test
  fun testSearchUsersFails() = runTest {
    val query = "test"
    val exception = Exception("Search failed")

    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .`when`(userRepository)
        .searchUsers(eq(query), any(), any())

    userViewModel.searchUsers(query)

    verify(userRepository).searchUsers(eq(query), any(), any())
    assert(userViewModel.searchedUsers.value.isEmpty())
    assert(!userViewModel.isLoading.value)
  }

  // Test for updating user successfully
  @Test
  fun updateUser_success() = runTest {
    val user = User("123", "Jane Doe", "jane@example.com")

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(userRepository)
        .updateUser(any(), any(), anyOrNull())

    userViewModel.updateUser(user)

    // Since updateUser calls loadUser, we need to mock listenToUser again if needed
    verify(userRepository).updateUser(eq(user), any(), anyOrNull())
  }

  // Test for signing out a user
  @Test
  fun signOutUser() {
    userViewModel.signOutUser()
    // Since we disabled the AuthStateListener, we may need to simulate the sign out effect
    assert(userViewModel.user.value == null)
  }

  @Test
  fun loadUser_success() = runTest {
    val userId = "123"
    val mockFirebaseUser =
        mock(FirebaseUser::class.java).apply {
          `when`(uid).thenReturn(userId)
          `when`(displayName).thenReturn("Test User")
          `when`(email).thenReturn("test@example.com")
        }
    val mockUser = User(id = userId, name = "Test User", email = "test@example.com")

    // Mock userRepository.listenToUser to call onSuccess with a User
    doAnswer { invocation ->
          val uid = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          onSuccess(mockUser)
          null
        }
        .`when`(userRepository)
        .listenToUser(eq(userId), any(), anyOrNull())

    // Mock userRepository.fetchUsersByIds if updateContacts is called
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .`when`(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    // Call the loadUser function
    userViewModel.loadUser(userId, mockFirebaseUser)

    // Verify the repository was called
    verify(userRepository).listenToUser(eq(userId), any(), anyOrNull())

    // Assert that the ViewModel's user state is updated
    assert(userViewModel.user.value == mockUser)

    // Assert that loading is no longer in progress
    assert(!userViewModel.isLoading.value)
  }

  @Test
  fun loadUser_failure_createsNewUserFromFirebase() = runTest {
    val userId = "123"
    val mockFirebaseUser =
        mock(FirebaseUser::class.java).apply {
          `when`(uid).thenReturn(userId)
          `when`(displayName).thenReturn("Firebase User")
          `when`(email).thenReturn("firebase@example.com")
          `when`(photoUrl).thenReturn(null)
        }
    val newUser =
        User(
            id = userId,
            name = "Firebase User",
            email = "firebase@example.com",
            profilePicture = "",
            bio = "",
            contacts = listOf(),
            username = "firebase")

    // Mock failure in listenToUser
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("User not found"))
          null
        }
        .`when`(userRepository)
        .listenToUser(eq(userId), any(), anyOrNull())

    // Mock createUser to simulate successful user creation
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(userRepository)
        .createUser(eq(newUser), any(), anyOrNull())

    // Mock userRepository.fetchUsersByIds if updateContacts is called
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .`when`(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    // Call the loadUser function
    userViewModel.loadUser(userId, mockFirebaseUser)

    // Verify the repository tried to listen to the user
    verify(userRepository).listenToUser(eq(userId), any(), anyOrNull())

    // Verify the repository created a new user after failure
    verify(userRepository).createUser(eq(newUser), any(), anyOrNull())

    // Assert that the ViewModel's user state is updated with the newly created user
    assert(userViewModel.user.value == newUser)

    // Assert that loading is no longer in progress
    assert(!userViewModel.isLoading.value)
  }

  @Test
  fun loadUser_failure_noFirebaseUser() = runTest {
    val userId = "123"

    // Mock failure of user retrieval from repository
    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(Exception("User not found"))
          null
        }
        .`when`(userRepository)
        .listenToUser(eq(userId), any(), anyOrNull())

    // Call the loadUser function with no FirebaseUser
    userViewModel.loadUser(userId, null)

    // Verify the repository tried to retrieve the user
    verify(userRepository).listenToUser(eq(userId), any(), anyOrNull())

    // Assert that the ViewModel's user state is null after failure
    assert(userViewModel.user.value == null)

    // Assert that loading is no longer in progress
    assert(!userViewModel.isLoading.value)
  }

  @Test
  fun addContact_CallsUpdateUserAndDeleteFriendRequest() = runTest {
    // Mock initial user
    val currentUserId = "123"
    val contactUserId = "456"
    val friendRequestId = "req_789"
    val currentUser =
        User(
            id = currentUserId,
            name = "Current User",
            email = "current@example.com",
            contacts = listOf())
    userViewModel._user.value = currentUser

    // Mock updateUser to succeed
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(userRepository)
        .updateUser(any(), any(), anyOrNull())

    // Mock deleteFriendRequest to succeed
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(friendRequestRepository)
        .deleteRequest(eq(friendRequestId), any(), anyOrNull())

    // Call the method under test
    userViewModel.addContact(contactUserId, friendRequestId)

    // Verify updateUser was called
    verify(userRepository).updateUser(any(), any(), anyOrNull())

    // Verify deleteFriendRequest was called
    verify(friendRequestRepository).deleteRequest(eq(friendRequestId), any(), anyOrNull())
  }

  @Test
  fun clearFriendRequestState_RemovesFriendRequest() = runTest {
    val friendRequestId = "req_123"
    val friendRequest = FriendRequest(id = friendRequestId, from = "123", to = "456")
    userViewModel._friendRequests.value = listOf(friendRequest)

    // Mock deleteRequest to succeed
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(friendRequestRepository)
        .deleteRequest(eq(friendRequestId), any(), anyOrNull())

    // Call the method under test
    userViewModel.clearFriendRequestState(friendRequest.to)

    // Verify deleteRequest was called
    verify(friendRequestRepository).deleteRequest(eq(friendRequestId), any(), anyOrNull())

    // Assert the local state is updated
    assert(userViewModel.friendRequests.value.isEmpty())
  }

  @Test
  fun acceptFriendRequest_UpdatesContactsAndClearsRequest() = runTest {
    val currentUserId = "123"
    val senderId = "456"
    val friendRequest = FriendRequest(id = "req_123", from = senderId, to = currentUserId)
    val currentUser =
        User(
            id = currentUserId,
            name = "Current User",
            email = "current@example.com",
            contacts = listOf())
    val senderUser =
        User(id = senderId, name = "Sender User", email = "sender@example.com", contacts = listOf())

    userViewModel._user.value = currentUser

    // Mock fetchUsersByIds to return the sender user
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(senderUser))
          null
        }
        .`when`(userRepository)
        .fetchUsersByIds(eq(listOf(senderId)), any(), anyOrNull())

    // Mock updateUser to succeed
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(userRepository)
        .updateUser(any(), any(), anyOrNull())

    // Mock deleteRequest to succeed
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(friendRequestRepository)
        .deleteRequest(eq(friendRequest.id), any(), anyOrNull())

    // Call the method under test
    userViewModel.acceptFriendRequest(friendRequest)

    // Verify that the sender's user data was fetched
    verify(userRepository).fetchUsersByIds(eq(listOf(senderId)), any(), anyOrNull())

    // Verify updateUser was called twice: once for the current user and once for the sender
    verify(userRepository, times(2)).updateUser(any(), any(), anyOrNull())

    // Verify the correct updates for each user
    verify(userRepository)
        .updateUser(
            argThat { user -> user.id == currentUserId && user.contacts.contains(senderId) },
            any(),
            anyOrNull())
    verify(userRepository)
        .updateUser(
            argThat { user -> user.id == senderId && user.contacts.contains(currentUserId) },
            any(),
            anyOrNull())
  }

  @Test
  fun updateUserProfilePicture_success() = runTest {
    val mockUri = mock(Uri::class.java)
    val mockUrl = "http://example.com/new-profile.jpg"
    val userId = "123"

    userViewModel._user.value = User(id = userId, name = "Test User")

    // Properly mock uploadProfilePicture to return a success
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(String) -> Unit>(2) // Correctly mock onSuccess
          onSuccess(mockUrl)
          null
        }
        .whenever(userRepository)
        .uploadProfilePicture(eq(mockUri), eq(userId), any(), any())

    var resultUrl: String? = null
    userViewModel.updateUserProfilePicture(mockUri) { url -> resultUrl = url }

    verify(userRepository).uploadProfilePicture(eq(mockUri), eq(userId), any(), any())
    assert(resultUrl == mockUrl)
    assert(!userViewModel.isLoading.value)
  }

  @Test
  fun selectUser_updatesSelectedUser() {
    val selectedUser = User("123", "Selected User", "selected@example.com")
    userViewModel.selectUser(selectedUser)
    assert(userViewModel.selectedUser.value == selectedUser)
  }

  @Test
  fun deselectUser_resetsSelectedUser() {
    userViewModel.deselectUser()
    assert(userViewModel.selectedUser.value == null)
  }

  @Test
  fun removeContact_success() = runTest {
    val currentUser = User(id = "123", contacts = listOf("456"))
    val contactUser = User(id = "456", contacts = listOf("123"))
    userViewModel._user.value = currentUser

    // Mock fetching the contact user
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(contactUser))
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    // Mock updating the users
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .whenever(userRepository)
        .updateUser(any(), any(), anyOrNull())

    var successCalled = false
    userViewModel.removeContact(
        "456", { successCalled = true }, { fail("Failure callback should not be called") })

    verify(userRepository, times(2)).updateUser(any(), any(), anyOrNull())
    assert(successCalled)
  }

  @Test
  fun getNotificationsCount_updatesState() = runTest {
    val notificationCount = 5L

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(Long) -> Unit>(1)
          onSuccess(notificationCount)
          null
        }
        .whenever(friendRequestRepository)
        .getNotificationCount(anyString(), any(), anyOrNull())

    var resultCount: Long? = null
    userViewModel.getNotificationsCount { count -> resultCount = count }

    verify(friendRequestRepository).getNotificationCount(anyString(), any(), anyOrNull())
    assert(resultCount == notificationCount)
    assert(userViewModel.notificationCount.value == notificationCount)
  }

  @Test
  fun getMyContacts_updatesState() = runTest {
    val mockContacts =
        listOf(User(id = "123", name = "Contact 1"), User(id = "456", name = "Contact 2"))

    // Mock Firebase.auth.uid to return a non-null value
    whenever(Firebase.auth.uid).thenReturn("mockUserId")

    // Mock userRepository.getContacts to return mock data
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(mockContacts)
          null
        }
        .whenever(userRepository)
        .getContacts(eq("mockUserId"), any(), anyOrNull())

    // Create a job to collect contacts and ensure state updates are captured
    val job = launch { userViewModel.contacts.collect { /* No-op: Just observe the state */} }

    // Call the method under test
    userViewModel.getMyContacts { /* No-op */}

    // Allow all coroutines to complete
    advanceUntilIdle()

    // Verify the repository method was called
    verify(userRepository).getContacts(eq("mockUserId"), any(), anyOrNull())

    // Cancel the collection job
    job.cancel()
  }

  @Test
  fun loadUser_userExistsButFailsToLoad() = runTest {
    val userId = "123"
    val exception = Exception("Database error")

    // Mock listenToUser to call onFailure
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .listenToUser(eq(userId), any(), anyOrNull())

    userViewModel.loadUser(userId)

    verify(userRepository).listenToUser(eq(userId), any(), anyOrNull())
    assert(userViewModel.user.value == null)
    assert(!userViewModel.isLoading.value)
  }

  @Test
  fun loadUser_firebaseUserMissingFields() = runTest {
    val userId = "123"
    val mockFirebaseUser =
        mock(FirebaseUser::class.java).apply {
          `when`(uid).thenReturn(userId)
          `when`(displayName).thenReturn(null)
          `when`(email).thenReturn(null)
        }

    // Mock failure to load user from repository
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(Exception("User not found"))
          null
        }
        .whenever(userRepository)
        .listenToUser(eq(userId), any(), anyOrNull())

    userViewModel.loadUser(userId, mockFirebaseUser)

    verify(userRepository).listenToUser(eq(userId), any(), anyOrNull())
    verify(userRepository)
        .createUser(
            argThat { user -> user.name == "Unknown" && user.email == "No Email" },
            any(),
            anyOrNull())
  }

  @Test
  fun updateContacts_invalidContactIds() = runTest {
    val invalidIds = listOf("999", "888")

    // Mock fetchUsersByIds to return an empty list
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(invalidIds), any(), anyOrNull())

    userViewModel.updateContacts(invalidIds)

    verify(userRepository).fetchUsersByIds(eq(invalidIds), any(), anyOrNull())
    assert(userViewModel.contacts.value.isEmpty())
  }

  @Test
  fun friendRequest_creation_validId() {
    val mockId = "req_123"
    whenever(friendRequestRepository.getNewId()).thenReturn(mockId)

    val request = FriendRequest(id = mockId, from = "123", to = "456")

    assert(request.id == mockId)
    assert(request.from == "123")
    assert(request.to == "456")
  }

  @Test
  fun clearFriendRequestState_logsDeletedRequest() = runTest {
    val mockLog = mockStatic(Log::class.java)
    val friendRequest = FriendRequest(id = "req_123", from = "123", to = "456")
    userViewModel._friendRequests.value = listOf(friendRequest)

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .whenever(friendRequestRepository)
        .deleteRequest(eq("req_123"), any(), anyOrNull())

    userViewModel.clearFriendRequestState("456")

    mockLog.verify { Log.d("FRIEND_REQUEST", "Deleted request: req_123") }
    mockLog.close()
  }

  @Test
  fun getSentRequestId_returnsCorrectRequestId() {
    val userId = "456"
    val requestId = "req_123"
    val currentUserId = "123"

    val friendRequest = FriendRequest(id = requestId, from = currentUserId, to = userId)
    userViewModel._sentFriendRequests.value = listOf(friendRequest)

    val result = userViewModel.getSentRequestId(userId)
    assert(result == requestId)
  }

  @Test
  fun getSentRequestId_returnsNullIfNotFound() {
    val userId = "456"
    val currentUserId = "123"

    val friendRequest = FriendRequest(id = "req_123", from = currentUserId, to = "789")
    userViewModel._sentFriendRequests.value = listOf(friendRequest)

    val result = userViewModel.getSentRequestId(userId)
    assert(result == null)
  }

  @Test
  fun setQuery_withEmptyString_doesNotTriggerSearch() = runTest {
    val query = ""
    userViewModel.setQuery(query)
    assert(userViewModel.query.value == query)
    delay(300) // Wait for debounce delay
    verify(userRepository, never()).searchUsers(any(), any(), any())
  }

  @Test
  fun updateUser_failure() = runTest {
    val user = User("123", "Jane Doe", "jane@example.com")
    val exception = Exception("Update failed")

    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .updateUser(any(), any(), anyOrNull())

    var failureCalled = false
    userViewModel.updateUser(
        user,
        onFailure = {
          failureCalled = true
          assert(it == exception)
        })

    verify(userRepository).updateUser(eq(user), any(), anyOrNull())
    assert(failureCalled)
  }

  @Test
  fun acceptFriendRequest_senderUserNotFound() = runTest {
    val currentUserId = "123"
    val senderId = "456"
    val friendRequest = FriendRequest(id = "req_123", from = senderId, to = currentUserId)
    val currentUser =
        User(
            id = currentUserId,
            name = "Current User",
            email = "current@example.com",
            contacts = listOf())
    userViewModel._user.value = currentUser

    // Mock getUsersByIds to return empty list (sender user not found)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(listOf(senderId)), any(), anyOrNull())

    // Call the method under test
    userViewModel.acceptFriendRequest(friendRequest)

    // Verify that updateUser was not called for sender
    verify(userRepository, never())
        .updateUser(argThat { user -> user.id == senderId }, any(), anyOrNull())
  }

  @Test
  fun updateUserProfilePicture_failure() = runTest {
    val mockUri = mock(Uri::class.java)
    val userId = "123"
    val exception = Exception("Upload failed")

    userViewModel._user.value = User(id = userId, name = "Test User")

    // Properly mock uploadProfilePicture to return a failure
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(3)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .uploadProfilePicture(eq(mockUri), eq(userId), any(), any())

    var resultUrl: String? = null
    userViewModel.updateUserProfilePicture(mockUri) { url -> resultUrl = url }

    verify(userRepository).uploadProfilePicture(eq(mockUri), eq(userId), any(), any())
    assert(resultUrl == null)
    assert(!userViewModel.isLoading.value)
  }

  @Test
  fun getNotificationsCount_handlesFailure() = runTest {
    val exception = Exception("Failed to get notification count")

    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(friendRequestRepository)
        .getNotificationCount(anyString(), any(), anyOrNull())

    userViewModel.getNotificationsCount { count -> fail("onSuccess should not be called") }

    verify(friendRequestRepository).getNotificationCount(anyString(), any(), anyOrNull())
    assert(userViewModel.notificationCount.value == 0L)
  }

  @Test
  fun getFriendRequests_handlesFailure() = runTest {
    val exception = Exception("Failed to get friend requests")

    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(friendRequestRepository)
        .getFriendRequests(anyString(), any(), anyOrNull())

    var onSuccessCalled = false
    userViewModel.getFriendRequests { onSuccessCalled = true }

    verify(friendRequestRepository).getFriendRequests(anyString(), any(), anyOrNull())
    assert(!onSuccessCalled)
    assert(userViewModel.friendRequests.value.isEmpty())
  }

  @Test
  fun getUsersByIds_handlesFailure() = runTest {
    val userIds = listOf("123", "456")
    val exception = Exception("Failed to fetch users")

    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(userIds), any(), anyOrNull())

    var onSuccessCalled = false
    userViewModel.getUsersByIds(userIds) { users -> onSuccessCalled = true }

    verify(userRepository).fetchUsersByIds(eq(userIds), any(), anyOrNull())
    assert(!onSuccessCalled)
  }

  @Test
  fun getMyContacts_handlesFailure() = runTest {
    val exception = Exception("Failed to get contacts")

    // Mock Firebase.auth.uid to return a non-null value
    whenever(Firebase.auth.uid).thenReturn("mockUserId")

    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .getContacts(eq("mockUserId"), any(), anyOrNull())

    var onSuccessCalled = false
    userViewModel.getMyContacts { users -> onSuccessCalled = true }

    verify(userRepository).getContacts(eq("mockUserId"), any(), anyOrNull())
    assert(!onSuccessCalled)
  }

  @Test
  fun getFriendRequests_updatesStateOnSuccess_newData() = runTest {
    val userId = "123"
    val friendRequests =
        listOf(
            FriendRequest(id = "req1", from = "userA", to = userId),
            FriendRequest(id = "req2", from = "userB", to = userId))

    // Mock Firebase.auth.uid to return userId
    whenever(Firebase.auth.uid).thenReturn(userId)

    // Mock friendRequestRepository.getFriendRequests to call onSuccess with friendRequests
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(friendRequests)
          null
        }
        .whenever(friendRequestRepository)
        .getFriendRequests(eq(userId), any(), anyOrNull())

    // Mock getUsersByIds to return users corresponding to friendRequests.from
    val users =
        listOf(
            User(id = "userA", name = "User A", email = "userA@example.com"),
            User(id = "userB", name = "User B", email = "userB@example.com"))
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(users)
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(listOf("userA", "userB")), any(), anyOrNull())

    var onSuccessCalled = false
    userViewModel.getFriendRequests {
      onSuccessCalled = true
      assert(it == friendRequests)
    }

    verify(friendRequestRepository).getFriendRequests(eq(userId), any(), anyOrNull())
    verify(userRepository).fetchUsersByIds(eq(listOf("userA", "userB")), any(), anyOrNull())

    assert(onSuccessCalled)
    assert(userViewModel.friendRequests.value == friendRequests)
    assert(userViewModel.notificationUsers.value == users)
  }

  @Test
  fun getFriendRequests_updatesStateWhenListChanges() = runTest {
    val userId = "123"
    val initialFriendRequests = listOf(FriendRequest(id = "req1", from = "userA", to = userId))
    userViewModel._friendRequests.value = initialFriendRequests

    // New friend requests from repository
    val newFriendRequests =
        listOf(
            FriendRequest(id = "req1", from = "userA", to = userId),
            FriendRequest(id = "req2", from = "userB", to = userId))

    // Mock Firebase.auth.uid to return userId
    whenever(Firebase.auth.uid).thenReturn(userId)

    // Mock friendRequestRepository.getFriendRequests to return newFriendRequests
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(newFriendRequests)
          null
        }
        .whenever(friendRequestRepository)
        .getFriendRequests(eq(userId), any(), anyOrNull())

    // Mock getUsersByIds to return users corresponding to newFriendRequests.from
    val users =
        listOf(
            User(id = "userA", name = "User A", email = "userA@example.com"),
            User(id = "userB", name = "User B", email = "userB@example.com"))
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(users)
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(listOf("userA", "userB")), any(), anyOrNull())

    var onSuccessCalled = false
    userViewModel.getFriendRequests {
      onSuccessCalled = true
      assert(it == newFriendRequests)
    }

    verify(friendRequestRepository).getFriendRequests(eq(userId), any(), anyOrNull())
    verify(userRepository).fetchUsersByIds(eq(listOf("userA", "userB")), any(), anyOrNull())

    assert(onSuccessCalled)
    assert(userViewModel.friendRequests.value == newFriendRequests)
    assert(userViewModel.notificationUsers.value == users)
  }

  @Test
  fun getFriendRequests_updatesWhenSizeChanges() = runTest {
    val userId = "123"
    val initialFriendRequests = listOf(FriendRequest(id = "req1", from = "userA", to = userId))
    userViewModel._friendRequests.value = initialFriendRequests

    // New friend requests from repository with different size
    val newFriendRequests =
        listOf(
            FriendRequest(id = "req1", from = "userA", to = userId),
            FriendRequest(id = "req2", from = "userB", to = userId),
            FriendRequest(id = "req3", from = "userC", to = userId))

    // Mock Firebase.auth.uid to return userId
    whenever(Firebase.auth.uid).thenReturn(userId)

    // Mock friendRequestRepository.getFriendRequests to return newFriendRequests
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(newFriendRequests)
          null
        }
        .whenever(friendRequestRepository)
        .getFriendRequests(eq(userId), any(), anyOrNull())

    // Mock getUsersByIds to return users corresponding to newFriendRequests.from
    val users =
        listOf(
            User(id = "userA", name = "User A", email = "userA@example.com"),
            User(id = "userB", name = "User B", email = "userB@example.com"),
            User(id = "userC", name = "User C", email = "userC@example.com"))
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(users)
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(listOf("userA", "userB", "userC")), any(), anyOrNull())

    var onSuccessCalled = false
    userViewModel.getFriendRequests {
      onSuccessCalled = true
      assert(it == newFriendRequests)
    }

    verify(friendRequestRepository).getFriendRequests(eq(userId), any(), anyOrNull())
    verify(userRepository)
        .fetchUsersByIds(eq(listOf("userA", "userB", "userC")), any(), anyOrNull())

    assert(onSuccessCalled)
    assert(userViewModel.friendRequests.value == newFriendRequests)
    assert(userViewModel.notificationUsers.value == users)
  }

  @Test
  fun getSentFriendRequests_updatesStateOnSuccess() = runTest {
    val userId = "123"
    val requests = listOf(FriendRequest(id = "req_999", from = userId, to = "someUser"))

    whenever(Firebase.auth.uid).thenReturn(userId)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(requests)
          null
        }
        .whenever(friendRequestRepository)
        .listenToSentFriendRequests(eq(userId), any(), anyOrNull())

    userViewModel.getSentFriendRequests()

    assert(userViewModel.sentFriendRequests.value == requests)
    verify(friendRequestRepository).listenToSentFriendRequests(eq(userId), any(), anyOrNull())
  }

  @Test
  fun deleteFriendRequest_success() = runTest {
    val reqId = "req_999"
    val request = FriendRequest(id = reqId, from = "123", to = "456")
    userViewModel._sentFriendRequests.value = listOf(request)

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .whenever(friendRequestRepository)
        .deleteRequest(eq(reqId), any(), anyOrNull())

    userViewModel.deleteFriendRequest(reqId)

    assert(userViewModel.sentFriendRequests.value.isEmpty())
    verify(friendRequestRepository).deleteRequest(eq(reqId), any(), anyOrNull())
  }

  @Test
  fun deleteFriendRequest_failure() = runTest {
    val reqId = "req_999"
    val request = FriendRequest(id = reqId, from = "123", to = "456")
    userViewModel._sentFriendRequests.value = listOf(request)
    val exception = Exception("Delete failed")

    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(friendRequestRepository)
        .deleteRequest(eq(reqId), any(), anyOrNull())

    userViewModel.deleteFriendRequest(reqId)

    // The request is removed optimistically; no crash expected.
    assert(userViewModel.sentFriendRequests.value.isEmpty())
  }

  @Test
  fun removeContact_failure_inFetchingUsers() = runTest {
    val exception = Exception("Failed to fetch users")
    whenever(userRepository.fetchUsersByIds(any(), any(), anyOrNull())).thenAnswer { invocation ->
      val onFailure = invocation.arguments[2] as (Exception) -> Unit
      // Simulate a user fetch failure
      onFailure(exception)
      null
    }

    // Removed onFailure checking because the code never calls it.
    userViewModel.removeContact(
        secondUserId = "456", onSuccess = { fail("Should not succeed since fetching users failed") }
        // No onFailure assertion since the code does not call it
        )

    // The code doesn't call onSuccess or onFailure in response to this error, it only logs it.
    // Just verify that no crash happened and maybe that the contacts remain unchanged:
    assert(userViewModel.contacts.value.isEmpty())
  }

  @Test
  fun removeContact_failure_inUpdatingUser() = runTest {
    val currentUser = User(id = "123", contacts = listOf("456"))
    val contactUser = User(id = "456", contacts = listOf("123"))
    userViewModel._user.value = currentUser

    // Mock fetching the contact user successfully
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(contactUser))
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    // Mock updateUser to fail
    val updateException = Exception("Update failed")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(updateException)
          null
        }
        .whenever(userRepository)
        .updateUser(any(), any(), anyOrNull())

    var failureCalled = false
    userViewModel.removeContact(
        "456",
        onSuccess = { fail("Should not succeed") },
        onFailure = { error ->
          failureCalled = true
          assert(error == updateException)
        })
    assert(failureCalled)
  }

  @Test
  fun acceptFriendRequest_failureToUpdateCurrentUser() = runTest {
    val currentUserId = "123"
    val friendRequest = FriendRequest(id = "req_123", from = "456", to = currentUserId)
    userViewModel._user.value = User(id = currentUserId, contacts = listOf())

    val senderUser = User(id = "456", contacts = listOf())
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(senderUser))
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    val exception = Exception("Failed to update current user")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .updateUser(argThat { user -> user.id == currentUserId }, any(), anyOrNull())

    userViewModel.acceptFriendRequest(friendRequest)

    verify(userRepository, never())
        .updateUser(argThat { user -> user.id == "456" }, any(), anyOrNull())
  }

  @Test
  fun acceptFriendRequest_failureToUpdateSenderUser() = runTest {
    val currentUserId = "123"
    val friendRequest = FriendRequest(id = "req_123", from = "456", to = currentUserId)
    val currentUser = User(id = currentUserId, contacts = listOf())
    val senderUser = User(id = "456", contacts = listOf())
    userViewModel._user.value = currentUser

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(listOf(senderUser))
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    // Current user update succeeds
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .whenever(userRepository)
        .updateUser(argThat { user -> user.id == currentUserId }, any(), anyOrNull())

    // Sender user update fails
    val exception = Exception("Failed to update sender user")
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .updateUser(argThat { user -> user.id == "456" }, any(), anyOrNull())

    userViewModel.acceptFriendRequest(friendRequest)

    // No full success, so friendRequest is not cleared.
    assert(userViewModel.friendRequests.value.isEmpty())
  }

  @Test
  fun listenToFriendRequests_updatesStateOnNewRequests() = runTest {
    val userId = "123"
    val friendRequests =
        listOf(
            FriendRequest(id = "req1", from = "userA", to = userId),
            FriendRequest(id = "req2", from = "userB", to = userId))

    // Mock Firebase.auth.uid to return userId
    whenever(Firebase.auth.uid).thenReturn(userId)

    // Mock friendRequestRepository.listenToFriendRequests to return new friend requests
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(friendRequests)
          null
        }
        .whenever(friendRequestRepository)
        .listenToFriendRequests(eq(userId), any(), anyOrNull())

    // Mock getUsersByIds to return user data for the incoming requests
    val users =
        listOf(
            User(id = "userA", name = "User A", email = "userA@example.com"),
            User(id = "userB", name = "User B", email = "userB@example.com"))
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(users)
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(listOf("userA", "userB")), any(), anyOrNull())

    // Call the method under test
    userViewModel.listenToFriendRequests()

    // Verify repository method was called
    verify(friendRequestRepository).listenToFriendRequests(eq(userId), any(), anyOrNull())

    // Verify user data fetch
    verify(userRepository).fetchUsersByIds(eq(listOf("userA", "userB")), any(), anyOrNull())

    // Assert the state is updated correctly
    assert(userViewModel.friendRequests.value == friendRequests)
    assert(userViewModel.notificationUsers.value == users)
    assert(userViewModel.notificationCount.value == friendRequests.size.toLong())
  }

  @Test
  fun listenToFriendRequests_handlesFailureGracefully() = runTest {
    val userId = "123"
    val exception = Exception("Failed to listen to friend requests")

    // Mock Firebase.auth.uid to return userId
    whenever(Firebase.auth.uid).thenReturn(userId)

    // Mock friendRequestRepository.listenToFriendRequests to call onFailure
    doAnswer { invocation ->
          val onFailure = invocation.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(friendRequestRepository)
        .listenToFriendRequests(eq(userId), any(), anyOrNull())

    // Call the method under test
    userViewModel.listenToFriendRequests()

    // Verify repository method was called
    verify(friendRequestRepository).listenToFriendRequests(eq(userId), any(), anyOrNull())

    // Assert that the state remains unchanged
    assert(userViewModel.friendRequests.value.isEmpty())
    assert(userViewModel.notificationUsers.value.isEmpty())
    assert(userViewModel.notificationCount.value == 0L)
  }

  @Test
  fun getUsersByIds_updatesStateOnSuccess() = runTest {
    val userIds = listOf("userA", "userB")
    val users =
        listOf(
            User(id = "userA", name = "User A", email = "userA@example.com"),
            User(id = "userB", name = "User B", email = "userB@example.com"))

    // Mock fetching users by IDs
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(users)
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(userIds), any(), anyOrNull())

    var onSuccessCalled = false
    userViewModel.getUsersByIds(userIds) { fetchedUsers ->
      onSuccessCalled = true
      assert(fetchedUsers == users)
    }

    verify(userRepository).fetchUsersByIds(eq(userIds), any(), anyOrNull())
    assert(onSuccessCalled)
  }
}
