package com.android.voyageur.notifications

// test the TripInviteRepositoryFirebase class

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.notifications.TripInvite
import com.android.voyageur.model.notifications.TripInviteRepositoryFirebase
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.AggregateQuery
import com.google.firebase.firestore.AggregateQuerySnapshot
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class TripInviteRepositoryFirebaseTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockAggregateQuerySnapshot: AggregateQuerySnapshot
  @Mock private lateinit var mockQuery: Query

  private lateinit var tripInviteRepository: TripInviteRepositoryFirebase
  private val testTripInvite = TripInvite("1", "tripId", "fromUser", "toUser")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    tripInviteRepository = TripInviteRepositoryFirebase(mockFirestore)

    `when`(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun `init calls onSuccess when Firestore query succeeds`() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

    var initCalled = false
    tripInviteRepository.init { initCalled = true }

    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(initCalled)
  }

  @Test
  fun `getTripInvitesCount calls success callback with correct count`() {
    val userId = "toUser"
    val mockAggregateQuery = mock(AggregateQuery::class.java)
    val mockTask = mock(Task::class.java) as Task<AggregateQuerySnapshot>

    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    `when`(mockQuery.count()).thenReturn(mockAggregateQuery)
    `when`(mockAggregateQuery.get(AggregateSource.SERVER)).thenReturn(mockTask)

    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<AggregateQuerySnapshot>
      `when`(mockAggregateQuerySnapshot.count).thenReturn(3L)
      listener.onSuccess(mockAggregateQuerySnapshot)
      mockTask
    }

    tripInviteRepository.getTripInvitesCount(
        userId,
        onSuccess = { count -> assertEquals(3L, count) },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockQuery).count()
  }

  @Test
  fun `deleteTripInvite deletes invite by ID`() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    tripInviteRepository.deleteTripInvite(
        testTripInvite.id,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()
    verify(mockDocumentReference).delete()
  }

  @Test
  fun `getNewId generates a unique ID`() {
    `when`(mockDocumentReference.id).thenReturn("uniqueId")
    val newId = tripInviteRepository.getNewId()
    assertEquals("uniqueId", newId)
  }

  @Test
  fun `listenToSentTripInvites registers listener for correct user`() {
    val userId = "fromUser"
    val mockListenerRegistration = mock(ListenerRegistration::class.java)

    `when`(mockCollectionReference.whereEqualTo("from", userId)).thenReturn(mockQuery)
    `when`(mockQuery.addSnapshotListener(any())).thenReturn(mockListenerRegistration)

    val listener =
        tripInviteRepository.listenToSentTripInvites(userId, onSuccess = {}, onFailure = {})

    assertEquals(mockListenerRegistration, listener)
    verify(mockQuery).addSnapshotListener(any())
  }

  @Test
  fun `listenToTripInvites registers listener for received invites`() {
    val userId = "toUser"
    val mockListenerRegistration = mock(ListenerRegistration::class.java)

    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    `when`(mockQuery.addSnapshotListener(any())).thenReturn(mockListenerRegistration)

    val listener = tripInviteRepository.listenToTripInvites(userId, onSuccess = {}, onFailure = {})

    assertEquals(mockListenerRegistration, listener)
    verify(mockQuery).addSnapshotListener(any())
  }

  @Test
  fun `getTripInvites fetches trip invites for the correct user`() {
    val userId = "toUser"
    val tripInvites =
        listOf(
            TripInvite("1", "trip1", "fromUser1", "toUser"),
            TripInvite("2", "trip2", "fromUser2", "toUser"))

    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.toObjects(TripInvite::class.java)).thenReturn(tripInvites)

    tripInviteRepository.getTripInvites(
        userId,
        onSuccess = { result ->
          assertEquals(2, result.size)
          assertEquals(tripInvites, result)
        },
        onFailure = { fail("Failure callback should not be called") })

    verify(mockQuery).get()
  }

  @Test
  fun `getInvitesForTrip fetches invites for the correct trip`() {
    val tripId = "tripId"
    val tripInvites =
        listOf(
            TripInvite("1", tripId, "fromUser1", "toUser1"),
            TripInvite("2", tripId, "fromUser2", "toUser2"))

    `when`(mockCollectionReference.whereEqualTo("tripId", tripId)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.toObjects(TripInvite::class.java)).thenReturn(tripInvites)

    tripInviteRepository.getInvitesForTrip(
        tripId,
        onSuccess = { result ->
          assertEquals(2, result.size)
          assertEquals(tripInvites, result)
        },
        onFailure = { fail("Failure callback should not be called") })

    verify(mockQuery).get()
  }

  @Test
  fun `listenToSentTripInvites registers listener and processes snapshots`() {
    val userId = "fromUser"
    val tripInvites =
        listOf(
            TripInvite("1", "trip1", "fromUser", "toUser1"),
            TripInvite("2", "trip2", "fromUser", "toUser2"))
    val mockListenerRegistration = mock(ListenerRegistration::class.java)

    `when`(mockCollectionReference.whereEqualTo("from", userId)).thenReturn(mockQuery)
    `when`(mockQuery.addSnapshotListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
      listener.onEvent(mockQuerySnapshot, null)
      mockListenerRegistration
    }
    `when`(mockQuerySnapshot.toObjects(TripInvite::class.java)).thenReturn(tripInvites)

    val onSuccess: (List<TripInvite>) -> Unit = { result -> assertEquals(tripInvites, result) }
    val onFailure: (Exception) -> Unit = { fail("Failure callback should not be called") }

    val listener = tripInviteRepository.listenToSentTripInvites(userId, onSuccess, onFailure)

    verify(mockQuery).addSnapshotListener(any())
    assertEquals(mockListenerRegistration, listener)
  }

  @Test
  fun `listenToTripInvites registers listener and processes snapshots`() {
    val userId = "toUser"
    val tripInvites =
        listOf(
            TripInvite("1", "trip1", "fromUser1", "toUser"),
            TripInvite("2", "trip2", "fromUser2", "toUser"))
    val mockListenerRegistration = mock(ListenerRegistration::class.java)

    `when`(mockCollectionReference.whereEqualTo("to", userId)).thenReturn(mockQuery)
    `when`(mockQuery.addSnapshotListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
      listener.onEvent(mockQuerySnapshot, null)
      mockListenerRegistration
    }
    `when`(mockQuerySnapshot.toObjects(TripInvite::class.java)).thenReturn(tripInvites)

    val onSuccess: (List<TripInvite>) -> Unit = { result -> assertEquals(tripInvites, result) }
    val onFailure: (Exception) -> Unit = { fail("Failure callback should not be called") }

    val listener = tripInviteRepository.listenToTripInvites(userId, onSuccess, onFailure)

    verify(mockQuery).addSnapshotListener(any())
    assertEquals(mockListenerRegistration, listener)
  }
}
