package com.android.voyageur.model.user

import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
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
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserViewModelListenerTest {
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var userViewModel: TestUserViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  private val user = User("1", "name", "email", "", "bio", listOf(), emptyList(), "username")

  private lateinit var authStateListenerCaptor: KArgumentCaptor<FirebaseAuth.AuthStateListener>

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    userRepository = mock()
    friendRequestRepository = mock()
    firebaseAuth = mock()

    // Capture the AuthStateListener
    authStateListenerCaptor = argumentCaptor()
    doNothing().`when`(firebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())

    userViewModel = TestUserViewModel(userRepository, firebaseAuth, friendRequestRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // Subclass to expose the onCleared method for testing
  class TestUserViewModel(
      userRepository: UserRepository,
      firebaseAuth: FirebaseAuth,
      friendRequestRepository: FriendRequestRepository
  ) : UserViewModel(userRepository, firebaseAuth, friendRequestRepository) {
    public override fun onCleared() {
      super.onCleared()
    }
  }

  // Test to verify that listenToUser is called when loadUser is invoked
  @Test
  fun loadUser_callsListenToUser() = runTest {
    val userId = "123"
    val mockFirebaseUser = mock<FirebaseUser> { on { uid } doReturn userId }
    val listenerRegistration = mock<ListenerRegistration>()

    whenever(userRepository.listenToUser(eq(userId), any(), any())).thenReturn(listenerRegistration)

    userViewModel.loadUser(userId, mockFirebaseUser)

    verify(userRepository).listenToUser(eq(userId), any(), any())
  }

  // Test to verify that _user is updated when snapshot listener receives data
  @Test
  fun snapshotListener_updatesUserState() = runTest {
    val userId = "123"
    val mockFirebaseUser = mock<FirebaseUser> { on { uid } doReturn userId }
    val listenerRegistration = mock<ListenerRegistration>()

    // Capture the onSuccess and onFailure callbacks
    var onSuccessCallback: ((User) -> Unit)? = null
    var onFailureCallback: ((Exception) -> Unit)? = null

    whenever(userRepository.listenToUser(eq(userId), any(), any())).thenAnswer { invocation ->
      onSuccessCallback = invocation.getArgument(1)
      onFailureCallback = invocation.getArgument(2)
      listenerRegistration
    }

    userViewModel.loadUser(userId, mockFirebaseUser)

    // Simulate a delay and then invoke the onSuccess callback with updated user data
    delay(100)
    val updatedUser = User(id = userId, name = "Updated User", email = "updated@example.com")
    onSuccessCallback?.invoke(updatedUser)

    // Wait for the ViewModel to process the update
    delay(100)

    // Verify that _user.value is updated
    assert(userViewModel.user.value == updatedUser)
  }

  // Test to verify that the listener is removed when onCleared is called
  @Test
  fun onCleared_removesListener() = runTest {
    val userId = "123"
    val mockFirebaseUser = mock<FirebaseUser> { on { uid } doReturn userId }
    val listenerRegistration = mock<ListenerRegistration>()

    whenever(userRepository.listenToUser(eq(userId), any(), any())).thenReturn(listenerRegistration)

    userViewModel.loadUser(userId, mockFirebaseUser)

    // Call onCleared
    userViewModel.onCleared()

    // Verify that listenerRegistration.remove() is called
    verify(listenerRegistration).remove()
  }

  // Test to verify that the listener is removed when the user signs out
  @Test
  fun signOutUser_removesListener() = runTest {
    val userId = "123"
    val mockFirebaseUser = mock<FirebaseUser> { on { uid } doReturn userId }
    val listenerRegistration = mock<ListenerRegistration>()

    // Mock firebaseAuth.currentUser to return mockFirebaseUser initially
    whenever(firebaseAuth.currentUser).thenReturn(mockFirebaseUser)

    whenever(userRepository.listenToUser(eq(userId), any(), any())).thenReturn(listenerRegistration)

    // Simulate auth state change to signed in
    verify(firebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())

    val authStateListener = authStateListenerCaptor.firstValue
    assert(authStateListener != null) // Ensure the listener is captured

    authStateListener.onAuthStateChanged(firebaseAuth)

    // Verify that listenToUser was called
    verify(userRepository).listenToUser(eq(userId), any(), any())

    // Now call signOutUser
    userViewModel.signOutUser()

    // Verify that firebaseAuth.signOut() was called
    verify(firebaseAuth).signOut()

    // Simulate auth state change to signed out
    whenever(firebaseAuth.currentUser).thenReturn(null)
    authStateListener.onAuthStateChanged(firebaseAuth)

    // Verify that listenerRegistration.remove() was called
    verify(listenerRegistration).remove()

    // Verify that userViewModel.user.value is null
    assert(userViewModel.user.value == null)
  }

  // Adjust existing tests that might be affected by the changes
  @Test
  fun loadUser_success_withSnapshotListener() = runTest {
    val userId = "123"
    val mockFirebaseUser = mock<FirebaseUser> { on { uid } doReturn userId }
    val listenerRegistration = mock<ListenerRegistration>()
    val mockUser = User(id = userId, name = "Test User", email = "test@example.com")

    // Capture the onSuccess and onFailure callbacks
    var onSuccessCallback: ((User) -> Unit)? = null
    var onFailureCallback: ((Exception) -> Unit)? = null

    whenever(userRepository.listenToUser(eq(userId), any(), any())).thenAnswer { invocation ->
      onSuccessCallback = invocation.getArgument(1)
      onFailureCallback = invocation.getArgument(2)
      listenerRegistration
    }

    // Call the loadUser function
    userViewModel.loadUser(userId, mockFirebaseUser)

    // Simulate successful data retrieval
    onSuccessCallback?.invoke(mockUser)

    // Wait for the ViewModel to process the update
    delay(100)

    // Assert that the ViewModel's user state is updated
    assert(userViewModel.user.value == mockUser)

    // Assert that loading is no longer in progress
    assert(userViewModel.isLoading.value == false)
  }
  // Test that when a user signs in again after signing out, the ViewModel reattaches listeners
  @Test
  fun reSignInUser_reRegistersListeners() = runTest {
    val userId = "123"
    val mockFirebaseUser = mock<FirebaseUser> { on { uid } doReturn userId }
    val listenerRegistration = mock<ListenerRegistration>()

    // Initially, user signs in
    whenever(firebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    whenever(userRepository.listenToUser(eq(userId), any(), any())).thenReturn(listenerRegistration)
    val authStateListener = authStateListenerCaptor.firstValue

    // Simulate sign-in
    authStateListener.onAuthStateChanged(firebaseAuth)
    verify(userRepository).listenToUser(eq(userId), any(), any())

    // Simulate sign-out
    whenever(firebaseAuth.currentUser).thenReturn(null)
    authStateListener.onAuthStateChanged(firebaseAuth)
    verify(listenerRegistration).remove()
    assert(userViewModel.user.value == null)

    // Simulate re-sign-in
    whenever(firebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    authStateListener.onAuthStateChanged(firebaseAuth)

    // The listenToUser should be called again because the user signed in again
    verify(userRepository, times(2)).listenToUser(eq(userId), any(), any())
  }
}
