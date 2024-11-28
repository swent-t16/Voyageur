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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.util.Assert.fail
import junit.framework.TestCase
import org.junit.Assert.assertEquals
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

  private lateinit var tripRepository: TripRepositoryFirebase

  private val trip =
      Trip(
          "1",
          "creator",
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
}
