package com.android.voyageur.model.user

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepositoryFirebase
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class FriendRequestRepositoryFirebaseTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockAggregateQuerySnapshot: AggregateQuerySnapshot
  @Mock private lateinit var mockQuery: Query

  private lateinit var friendRequestRepository: FriendRequestRepositoryFirebase
  private val testFriendRequest = FriendRequest("1", "fromUser", "toUser")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    friendRequestRepository = FriendRequestRepositoryFirebase(mockFirestore)

    `when`(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun `getFriendRequests calls success callback with correct data`() {
    val userId = "toUser"
    val mockFriendRequests = listOf(FriendRequest("1", "fromUser", "toUser"))
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>

    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      `when`(mockQuerySnapshot.toObjects(FriendRequest::class.java)).thenReturn(mockFriendRequests)
      listener.onSuccess(mockQuerySnapshot)
      mockTask
    }

    val onSuccess: (List<FriendRequest>) -> Unit = { requests ->
      assertEquals(mockFriendRequests, requests)
    }

    val onFailure: (Exception) -> Unit = { fail("Failure callback should not be called") }

    friendRequestRepository.getFriendRequests(userId, onSuccess, onFailure)

    verify(mockQuery).get()
    verify(mockTask).addOnSuccessListener(any())
  }

  @Test
  fun `getNotificationCount calls success callback with correct count`() {
    val userId = "toUser"
    val mockAggregateQuery = mock(AggregateQuery::class.java)
    val mockTask = mock(Task::class.java) as Task<AggregateQuerySnapshot>
    val mockAggregateQuerySnapshot = mock(AggregateQuerySnapshot::class.java)

    // Mock the query and aggregate query behavior
    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    `when`(mockQuery.count()).thenReturn(mockAggregateQuery)
    `when`(mockAggregateQuery.get(AggregateSource.SERVER)).thenReturn(mockTask)

    // Mock the behavior of the Task when success occurs
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<AggregateQuerySnapshot>
      `when`(mockAggregateQuerySnapshot.count).thenReturn(5L)
      listener.onSuccess(mockAggregateQuerySnapshot)
      mockTask
    }

    // Ensure addOnFailureListener is mocked to avoid NullPointerException
    `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    // Test logic
    friendRequestRepository.getNotificationCount(
        userId,
        onSuccess = { count -> assertEquals(5L, count) },
        onFailure = { fail("Failure callback should not be called") })

    // Verify behavior
    verify(mockQuery).count()
    verify(mockAggregateQuery).get(AggregateSource.SERVER)
    verify(mockTask).addOnSuccessListener(any())
  }

  @Test
  fun `createRequest should call set with FriendRequest data`() {
    `when`(mockDocumentReference.set(any(FriendRequest::class.java), any<SetOptions>()))
        .thenReturn(Tasks.forResult(null))

    friendRequestRepository.createRequest(
        testFriendRequest,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).set(any(FriendRequest::class.java), any<SetOptions>())
  }

  @Test
  fun `getNewId should return generated ID`() {
    `when`(mockDocumentReference.id).thenReturn("generatedId")
    val newId = friendRequestRepository.getNewId()
    assertEquals("generatedId", newId)
  }

  @Test
  fun `init calls onSuccess when Firestore query succeeds`() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

    var initCalled = false
    friendRequestRepository.init { initCalled = true }

    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(initCalled)
  }

  @Test
  fun `getFriendRequests handles failure gracefully`() {
    val userId = "toUser"
    val exception = Exception("Firestore error")
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>

    // Mock the query behavior
    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockTask)

    // Mock the behavior of the Task when failure occurs
    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockTask
    }

    // Ensure addOnSuccessListener returns the mockTask to avoid NullPointerException
    `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)

    // Test logic
    friendRequestRepository.getFriendRequests(
        userId,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { e -> assertEquals("Firestore error", e.message) })

    // Verify behavior
    verify(mockQuery).get()
    verify(mockTask).addOnFailureListener(any())
  }
}
