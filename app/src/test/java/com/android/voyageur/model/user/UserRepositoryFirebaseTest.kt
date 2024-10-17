package com.android.voyageur.model.user

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirebaseTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuery: Query

  private lateinit var userRepository: UserRepositoryFirebase
  private val testUser = User("1", "name", "email", "password", "phone")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    userRepository = UserRepositoryFirebase(mockFirestore)

    `when`(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.orderBy(anyString())).thenReturn(mockQuery)
    `when`(mockQuery.startAt(anyString())).thenReturn(mockQuery)
    `when`(mockQuery.endAt(anyString())).thenReturn(mockQuery)
    `when`(mockQuery.limit(anyLong())).thenReturn(mockQuery)
  }

  // Existing test for searching users (success)
  @Test
  fun testSearchUsers_success() {
    val query = "test"
    val mockUserList = listOf(User(id = "1", name = "Test User", email = "test@example.com"))
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>

    `when`(mockQuery.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      listener.onSuccess(mockQuerySnapshot) // Simulate success callback
      mockTask
    }
    `when`(mockQuerySnapshot.toObjects(User::class.java)).thenReturn(mockUserList)

    val onSuccess: (List<User>) -> Unit = { users -> assertEquals(mockUserList, users) }

    val onFailure: (Exception) -> Unit = {
      assert(false) { "Failure should not be called in the success test" }
    }

    userRepository.searchUsers(query, onSuccess, onFailure)

    verify(mockQuery).get()
    verify(mockTask).addOnSuccessListener(any())
  }

  @Test
  fun testSearchUsers_failure() {
    val query = "test"
    val exception = Exception("Firestore error")

    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>

    `when`(mockQuery.get()).thenReturn(mockTask)
    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception) // Simulate failure callback
      mockTask
    }

    `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)

    val onSuccess: (List<User>) -> Unit = {
      assert(false) { "Success should not be called in the failure test" }
    }

    val onFailure: (Exception) -> Unit = { e -> assertEquals("Firestore error", e.message) }

    userRepository.searchUsers(query, onSuccess, onFailure)

    verify(mockQuery).get()
    verify(mockTask).addOnFailureListener(any())
  }

  // Test for generating a new user ID
  @Test
  fun getNewUserId_returnsNewId() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = userRepository.getNewUserId()
    assertEquals("1", uid)
  }

  // Test for retrieving user by ID
  @Test
  fun getUserById_callsDocumentGet() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.toObject(User::class.java)).thenReturn(testUser)

    userRepository.getUserById(
        "1",
        onSuccess = { user -> assertEquals(testUser, user) },
        onFailure = { fail("Failure callback should not be called") })

    verify(mockDocumentReference).get()
  }

  // Test for retrieving user by ID (failure)
  @Test
  fun IF_getUserById_fails_THEN_callOnFailure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forException(exception))

    userRepository.getUserById(
        "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { assertEquals("Test exception", it.message) })
  }

  // Test for creating a user
  @Test
  fun createUser_shouldCallSet() {
    `when`(mockDocumentReference.set(any(User::class.java), any<SetOptions>()))
        .thenReturn(Tasks.forResult(null))

    userRepository.createUser(
        testUser, onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).set(any(User::class.java), any<SetOptions>())
  }

  // Test for creating a user (failure)
  @Test
  fun IF_createUser_fails_THEN_callOnFailure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.set(any(User::class.java), any<SetOptions>()))
        .thenReturn(Tasks.forException(exception))

    userRepository.createUser(
        testUser,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { assertEquals("Test exception", it.message) })
  }

  // Test for updating a user
  @Test
  fun updateUser_shouldCallSet() {
    `when`(mockDocumentReference.set(any(User::class.java), any<SetOptions>()))
        .thenReturn(Tasks.forResult(null))

    userRepository.updateUser(
        testUser, onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).set(any(User::class.java), any<SetOptions>())
  }

  // Test for updating a user (failure)
  @Test
  fun IF_updateUser_fails_THEN_callOnFailure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.set(any(User::class.java), any<SetOptions>()))
        .thenReturn(Tasks.forException(exception))

    userRepository.updateUser(
        testUser,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { assertEquals("Test exception", it.message) })
  }

  // Test for deleting a user by ID
  @Test
  fun deleteUserById_shouldCallDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    userRepository.deleteUserById(
        "1", onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).delete()
  }

  // Test for deleting a user by ID (failure)
  @Test
  fun IF_deleteUserById_fails_THEN_callOnFailure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forException(exception))

    userRepository.deleteUserById(
        "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { assertEquals("Test exception", it.message) })
  }

  // Test for initializing user repository (success)
  @Test
  fun init_callsOnSuccess() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(null))

    var onSuccessCalled = false
    userRepository.init { onSuccessCalled = true }

    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(onSuccessCalled)
  }

  // Test for initializing user repository (failure)
  @Test
  fun IF_init_fails_THEN_logsError() {
    val exception = Exception("Test exception")
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forException(exception))

    userRepository.init { fail("Success callback should not be called") }

    shadowOf(Looper.getMainLooper()).idle()
  }
}
