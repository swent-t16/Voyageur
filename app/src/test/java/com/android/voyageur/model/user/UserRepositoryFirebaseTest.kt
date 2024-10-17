package com.android.voyageur.model.user

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirebaseTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  private lateinit var userRepository: UserRepositoryFirebase

  private val testUser =
      User(
          id = "1",
          name = "Test User",
          email = "test@example.com",
          profilePicture = "http://example.com/profile.jpg",
          bio = "Test Bio")

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
  }

  @Test
  fun getNewUserId_returnsNewId() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = userRepository.getNewUserId()
    assertEquals("1", uid)
  }

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

  @Test
  fun IF_getUserById_fails_THEN_callOnFailure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forException(exception))

    userRepository.getUserById(
        "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { assertEquals("Test exception", it.message) })
  }

  @Test
  fun createUser_shouldCallSet() {
    `when`(mockDocumentReference.set(any(User::class.java), any<SetOptions>()))
        .thenReturn(Tasks.forResult(null))

    userRepository.createUser(
        testUser, onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any(User::class.java), any<SetOptions>())
  }

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

  @Test
  fun updateUser_shouldCallSet() {
    `when`(mockDocumentReference.set(any(User::class.java), any<SetOptions>()))
        .thenReturn(Tasks.forResult(null))

    userRepository.updateUser(
        testUser, onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any(User::class.java), any<SetOptions>())
  }

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

  @Test
  fun deleteUserById_shouldCallDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    userRepository.deleteUserById(
        "1", onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
  }

  @Test
  fun IF_deleteUserById_fails_THEN_callOnFailure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forException(exception))

    userRepository.deleteUserById(
        "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { assertEquals("Test exception", it.message) })
  }

  @Test
  fun init_callsOnSuccess() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(null))

    var onSuccessCalled = false
    userRepository.init { onSuccessCalled = true }

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(onSuccessCalled)
  }

  @Test
  fun IF_init_fails_THEN_logsError() {
    val exception = Exception("Test exception")
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forException(exception))

    userRepository.init { fail("Success callback should not be called") }

    shadowOf(Looper.getMainLooper()).idle()
  }
}
