package com.android.voyageur.model.user

import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
            addAuthStateListener = false // Prevent adding the AuthStateListener during tests
            )
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
  fun getSentFriendRequests_UpdatesState() = runTest {
    val mockSentRequests =
        listOf(
            FriendRequest(id = "req_1", from = "123", to = "456"),
            FriendRequest(id = "req_2", from = "123", to = "789"))

    // Mock the repository to return mock data
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(mockSentRequests)
          null
        }
        .`when`(friendRequestRepository)
        .getSentFriendRequests(eq(""), any(), anyOrNull())

    // Call the method under test
    userViewModel.getSentFriendRequests()

    // Verify the repository method was called
    verify(friendRequestRepository).getSentFriendRequests(eq(""), any(), anyOrNull())

    // Assert the state is updated
    assert(userViewModel.sentFriendRequests.value == mockSentRequests)
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

    // Verify the friend request was deleted
    verify(friendRequestRepository).deleteRequest(eq(friendRequest.id), any(), anyOrNull())
  }
}
