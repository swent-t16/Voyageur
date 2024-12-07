package com.android.voyageur.model.user

import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepositoryFirebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FriendRequestRepositoryFirebaseListenerTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockAggregateQuerySnapshot: AggregateQuerySnapshot
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

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
  fun `listenToFriendRequests calls onSuccess with updated friend requests`() {
    val userId = "toUser"
    val mockFriendRequests = listOf(FriendRequest("1", "fromUser", userId))

    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    // Simulate Firestore addSnapshotListener call
    `when`(mockQuery.addSnapshotListener(any<EventListener<QuerySnapshot>>())).thenAnswer {
        invocation ->
      val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
      `when`(mockQuerySnapshot.toObjects(FriendRequest::class.java)).thenReturn(mockFriendRequests)
      listener.onEvent(mockQuerySnapshot, null) // Simulate snapshot received successfully
      mockListenerRegistration
    }

    var successCalled = false
    friendRequestRepository.listenToFriendRequests(
        userId,
        onSuccess = {
          successCalled = true
          assertEquals(mockFriendRequests, it)
        },
        onFailure = { fail("Should not fail on success scenario") })

    assertTrue("onSuccess should have been called", successCalled)
  }

  @Test
  fun `listenToFriendRequests calls onFailure when exception occurs`() {
    val userId = "toUser"
    val testException =
        FirebaseFirestoreException("Test Error", FirebaseFirestoreException.Code.UNKNOWN)

    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    `when`(mockQuery.addSnapshotListener(any<EventListener<QuerySnapshot>>())).thenAnswer {
        invocation ->
      val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
      // Simulate an error scenario
      listener.onEvent(null, testException)
      mockListenerRegistration
    }

    var failureCalled = false
    friendRequestRepository.listenToFriendRequests(
        userId,
        onSuccess = { fail("onSuccess should not be called on error") },
        onFailure = {
          failureCalled = true
          assertEquals("Test Error", it.message)
        })

    assertTrue("onFailure should have been called", failureCalled)
  }

  @Test
  fun `listenToSentFriendRequests calls onSuccess with updated friend requests`() {
    val userId = "fromUser"
    val mockFriendRequests = listOf(FriendRequest("1", userId, "toUser"))

    // Mock query for sent friend requests
    `when`(mockFirestore.collection("friendRequests")).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereEqualTo("from", userId)).thenReturn(mockQuery)

    // Simulate Firestore addSnapshotListener call
    `when`(mockQuery.addSnapshotListener(any<EventListener<QuerySnapshot>>())).thenAnswer {
        invocation ->
      val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
      `when`(mockQuerySnapshot.toObjects(FriendRequest::class.java)).thenReturn(mockFriendRequests)
      listener.onEvent(mockQuerySnapshot, null) // Simulate snapshot received
      mockListenerRegistration
    }

    var successCalled = false
    friendRequestRepository.listenToSentFriendRequests(
        userId,
        onSuccess = {
          successCalled = true
          assertEquals(mockFriendRequests, it)
        },
        onFailure = { fail("Should not fail on success scenario") })

    assertTrue("onSuccess should have been called", successCalled)
  }

  @Test
  fun `listenToSentFriendRequests calls onFailure when exception occurs`() {
    val userId = "fromUser"
    val testException =
        FirebaseFirestoreException("Another Test Error", FirebaseFirestoreException.Code.UNKNOWN)

    `when`(mockFirestore.collection("friendRequests")).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.whereEqualTo("from", userId)).thenReturn(mockQuery)
    `when`(mockQuery.addSnapshotListener(any<EventListener<QuerySnapshot>>())).thenAnswer {
        invocation ->
      val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
      // Simulate an exception in the snapshot listener
      listener.onEvent(null, testException)
      mockListenerRegistration
    }

    var failureCalled = false
    friendRequestRepository.listenToSentFriendRequests(
        userId,
        onSuccess = { fail("onSuccess should not be called on error") },
        onFailure = {
          failureCalled = true
          assertEquals("Another Test Error", it.message)
        })

    assertTrue("onFailure should have been called", failureCalled)
  }
}
