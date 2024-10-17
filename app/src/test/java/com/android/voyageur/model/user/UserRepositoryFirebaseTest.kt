package com.android.voyageur.model.user

import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserRepositoryFirebaseTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  @Mock private lateinit var mockQuery: Query

  private lateinit var userRepository: UserRepositoryFirebase

  private val user = User("1", "name", "email", "password", "phone")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    userRepository = UserRepositoryFirebase(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.orderBy(anyString())).thenReturn(mockQuery)
    `when`(mockQuery.startAt(anyString())).thenReturn(mockQuery)
    `when`(mockQuery.endAt(anyString())).thenReturn(mockQuery)
    `when`(mockQuery.limit(anyLong())).thenReturn(mockQuery)
  }

  @Test
  fun testSearchUsers_success() {
    val query = "test"
    val mockUserList = listOf(User(id = "1", name = "Test User", email = "test@example.com"))
    val mockTask = mock<Task<QuerySnapshot>>()
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
}
