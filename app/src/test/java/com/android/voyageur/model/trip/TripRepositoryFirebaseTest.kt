package com.android.voyageur.model.trip

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.location.Location
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.util.Assert.fail
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class TripRepositoryFirebaseTest {
  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

  private lateinit var tripRepository: TripRepositoryFirebase

  private val trip =
      Trip(
          "1",
          listOf("creator"),
          "description",
          "name",
          Location("", "", "", 0.0, 0.0),
          Timestamp.now(),
          Timestamp.now(),
          emptyList(),
          TripType.TOURISM,
          "")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    tripRepository = TripRepositoryFirebase(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.whereEqualTo(eq("creator"), any())).thenReturn(mockQuery)
    `when`(mockCollectionReference.whereArrayContains(eq("participants"), eq("creator")))
        .thenReturn(mockQuery)
    `when`(mockCollectionReference.whereEqualTo(eq("discoverable"), any())).thenReturn(mockQuery)

    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    tripRepository = TripRepositoryFirebase(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun getNewUid() {
    `when`(mockDocumentReference.id).thenReturn("1")
    val uid = tripRepository.getNewTripId()
    assert(uid == "1")
  }

  @Test
  fun getTrips_callsDocuments() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

    `when`(mockQuerySnapshot.documents).thenReturn(listOf())

    tripRepository.getTrips(
        creator = "creator",
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") },
    )

    verify(mockCollectionReference).whereArrayContains("participants", "creator")

    // Verify that get() was called on the query
    verify(mockQuery).get()
  }

  @Test
  fun getTrips_Failure() {
    val exception = Exception("Firestore error")
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>

    `when`(mockQuery.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      // Do not call onSuccess to simulate failure
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception) // Simulate failure callback
      mockTask
    }

    val onSuccess: (List<Trip>) -> Unit = {
      assert(false) { "Success should not be called in the failure test" }
    }

    val onFailure: (Exception) -> Unit = { e ->
      TestCase.assertEquals("Firestore error", e.message)
    }

    tripRepository.getTrips("creator", onSuccess, onFailure)

    verify(mockQuery).get()
    verify(mockTask).addOnFailureListener(any())
  }

  @Test
  fun createTrip_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    tripRepository.createTrip(trip, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun createTrip_failure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.set(any()))
        .thenReturn(Tasks.forException(exception)) // Simulate failure

    val onSuccess: () -> Unit = { fail("Success callback should not be called") }
    val onFailure: (Exception) -> Unit = { e -> assertEquals("Test exception", e.message) }

    tripRepository.createTrip(trip, onSuccess, onFailure)

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun deleteToDoById_shouldCallDocumentReferenceDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    tripRepository.deleteTripById("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteTripById_failure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.delete())
        .thenReturn(Tasks.forException(exception)) // Simulate failure

    val onSuccess: () -> Unit = { fail("Success callback should not be called") }
    val onFailure: (Exception) -> Unit = { e -> assertEquals("Test exception", e.message) }

    tripRepository.deleteTripById("1", onSuccess, onFailure)

    // shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockDocumentReference).delete()
  }

  @Test
  fun updateTrip_shouldCallDocumentReferenceSet() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    tripRepository.updateTrip(trip, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun updateTrip_failure() {
    val exception = Exception("Test exception")
    `when`(mockDocumentReference.set(any()))
        .thenReturn(Tasks.forException(exception)) // Simulate failure

    val onSuccess: () -> Unit = { fail("Success callback should not be called") }
    val onFailure: (Exception) -> Unit = { e -> assertEquals("Test exception", e.message) }

    tripRepository.updateTrip(trip, onSuccess, onFailure)

    shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun getFeed_callsDocuments() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

    `when`(mockQuerySnapshot.documents).thenReturn(listOf())

    tripRepository.getFeed("test", {}, {})

    verify(mockCollectionReference).whereEqualTo("discoverable", true)

    // Verify that get() was called on the query
    verify(mockQuery).get()
  }

  @Test
  fun getFeed() {
    val exception = Exception("Firestore error")
    val mockTask = mock(Task::class.java) as Task<QuerySnapshot>

    `when`(mockQuery.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
      // Do not call onSuccess to simulate failure
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception) // Simulate failure callback
      mockTask
    }

    val onSuccess: (List<Trip>) -> Unit = {
      assert(false) { "Success should not be called in the failure test" }
    }

    val onFailure: (Exception) -> Unit = { e ->
      TestCase.assertEquals("Firestore error", e.message)
    }

    tripRepository.getFeed("creator", onSuccess, onFailure)

    verify(mockQuery).get()
    verify(mockTask).addOnFailureListener(any())
  }

  @Test
  fun `listenForTripUpdates calls onFailure when exception occurs`() {
    val userId = "userId"
    val testException =
        FirebaseFirestoreException("Test Error", FirebaseFirestoreException.Code.UNKNOWN)

    // Mock query for trips where the user is a participant
    `when`(mockCollectionReference.whereArrayContains("participants", userId)).thenReturn(mockQuery)

    // Simulate Firestore addSnapshotListener call
    `when`(mockQuery.addSnapshotListener(any<EventListener<QuerySnapshot>>())).thenAnswer {
        invocation ->
      val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
      // Simulate an exception in the snapshot listener
      listener.onEvent(null, testException)
      mockListenerRegistration
    }

    var failureCalled = false
    tripRepository.listenForTripUpdates(
        userId,
        onSuccess = { fail("onSuccess should not be called on error") },
        onFailure = {
          failureCalled = true
          assertEquals("Test Error", it.message)
        })

    assertTrue("onFailure should have been called", failureCalled)
  }

  @Test
  fun `getTripById success returns trip object`() {
    // Mock successful document snapshot that contains a Trip
    val mockDocumentSnapshot = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)
    `when`(mockDocumentSnapshot.toObject(Trip::class.java)).thenReturn(trip)

    // Configure the mock task to return success with our mock snapshot
    val mockTask = mock(Task::class.java) as Task<com.google.firebase.firestore.DocumentSnapshot>
    `when`(mockDocumentReference.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0]
              as OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockTask
    }

    var successCalled = false
    var resultTrip: Trip? = null

    tripRepository.getTripById(
        "1",
        onSuccess = {
          successCalled = true
          resultTrip = it
        },
        onFailure = { fail("Failure callback should not be called") })

    assertTrue("Success callback should have been called", successCalled)
    assertEquals(trip, resultTrip)
    verify(mockDocumentReference).get()
  }

  @Test
  fun `getTripById with null trip calls onFailure`() {
    // Mock document snapshot that returns null for toObject
    val mockDocumentSnapshot = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)
    `when`(mockDocumentSnapshot.toObject(Trip::class.java)).thenReturn(null)

    val mockTask = mock(Task::class.java) as Task<com.google.firebase.firestore.DocumentSnapshot>
    `when`(mockDocumentReference.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0]
              as OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockTask
    }

    var failureCalled = false

    tripRepository.getTripById(
        "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = {
          failureCalled = true
          assertEquals("Trip not found", it.message)
        })

    assertTrue("Failure callback should have been called", failureCalled)
    verify(mockDocumentReference).get()
  }

  @Test
  fun `getTripById failure calls onFailure`() {
    val exception = Exception("Failed to get trip")
    val mockTask = mock(Task::class.java) as Task<com.google.firebase.firestore.DocumentSnapshot>

    `when`(mockDocumentReference.get()).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockTask
    }

    var failureCalled = false

    tripRepository.getTripById(
        "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = {
          failureCalled = true
          assertEquals("Failed to get trip", it.message)
        })

    assertTrue("Failure callback should have been called", failureCalled)
    verify(mockDocumentReference).get()
  }
}
