package com.android.voyageur.model.user

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertEquals
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
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserViewModelTest {
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  private val user = User("1", "name", "email", "", "bio", listOf(), emptyList(), "username")

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
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
    delay(200) // Wait for debounce delay
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

    `when`(userRepository.updateUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    userViewModel.updateUser(user)

    assert(userViewModel.user.value == user)
    assert(!userViewModel.isLoading.value)
  }

  // Test for signing out a user
  @Test
  fun signOutUser() {
    userViewModel.signOutUser()
    assert(userViewModel.user.value == null)
  }

  @Test
  fun loadUser_success() = runTest {
    val userId = "123"
    val mockFirebaseUser = mock(FirebaseUser::class.java)
    val mockUser = User(id = userId, name = "Test User", email = "test@example.com")

    // Mock successful retrieval of user from repository
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (User) -> Unit
          onSuccess(mockUser)
        }
        .`when`(userRepository)
        .getUserById(eq(userId), any(), any())

    // Call the loadUser function
    userViewModel.loadUser(userId, mockFirebaseUser)

    // Verify the repository was called
    verify(userRepository).getUserById(eq(userId), any(), any())

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
            username = "firebase" // Update this to match the actual invocation
            )
    // Mock failure of user retrieval from repository
    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(Exception("User not found"))
        }
        .`when`(userRepository)
        .getUserById(eq(userId), any(), any())

    // Mock successful user creation
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as () -> Unit
          onSuccess()
        }
        .`when`(userRepository)
        .createUser(eq(newUser), any(), any())

    // Call the loadUser function
    userViewModel.loadUser(userId, mockFirebaseUser)

    // Verify the repository tried to retrieve the user
    verify(userRepository).getUserById(eq(userId), any(), any())

    // Verify the repository created a new user after failure
    verify(userRepository).createUser(eq(newUser), any(), any())

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
        }
        .`when`(userRepository)
        .getUserById(eq(userId), any(), any())

    // Call the loadUser function with no FirebaseUser
    userViewModel.loadUser(userId, null)

    // Verify the repository tried to retrieve the user
    verify(userRepository).getUserById(eq(userId), any(), any())

    // Assert that the ViewModel's user state is null after failure
    assert(userViewModel.user.value == null)

    // Assert that loading is no longer in progress
    assert(!userViewModel.isLoading.value)
  }

  @Test
  fun testAddContact() = runTest {
    // Initial setup: mock an existing user with no contacts
    val initialUser =
        User(id = "123", name = "Test User", email = "test@example.com", contacts = emptyList())
    userViewModel._user.value = initialUser

    // Set up mock behavior for the repository updateUser method
    `when`(userRepository.updateUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }

    // Add a new contact
    val newContactId = "456"
    userViewModel.addContact(newContactId)

    // Expected user after adding the contact
    val expectedUser = initialUser.copy(contacts = listOf(newContactId))

    // Verify that updateUser in the repository was called with the expected user data
    verify(userRepository).updateUser(eq(expectedUser), any(), any())

    // Assert that the ViewModel's user state is updated with the new contact
    assertEquals(expectedUser, userViewModel.user.value)
  }
}
