package com.android.voyageur.model.trip

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.util.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.timeout
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

    private lateinit var tripRepository: TripRepositoryFirebase

    private val trip =
        Trip(
            "1",
            "creator",
            emptyList(),
            "description",
            "name",
            emptyList(),
            Timestamp.now(),
            Timestamp.now(),
            emptyList(),
            TripType.TOURISM)

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
        `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    }

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
        `when`(mockQuerySnapshot.documents).thenReturn(listOf())

        tripRepository.getTrips(
            onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

        verify(mockCollectionReference, timeout(100)).get() // Verify that get() was called
    }

    @Test
    fun IF_getTrips_fails_THEN_callOnFailure() {
        `when`(mockCollectionReference.get())
            .thenReturn(Tasks.forException(Exception("Test exception")))

        tripRepository.getTrips(
            onSuccess = { fail("Success callback should not be called") },
            onFailure = { assert(it.message == "Test exception") })
    }

    @Test
    fun createTrip_shouldCallFirestoreCollection() {
        `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

        tripRepository.createTrip(trip, onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockDocumentReference).set(any())
    }

    @Test
    fun IF_createTrip_fails_THEN_callOnFailure() {
        `when`(mockDocumentReference.set(any()))
            .thenReturn(Tasks.forException(Exception("Test exception")))

        tripRepository.createTrip(
            trip,
            onSuccess = { fail("Success callback should not be called") },
            onFailure = { assert(it.message == "Test exception") })
    }

    @Test
    fun deleteToDoById_shouldCallDocumentReferenceDelete() {
        `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

        tripRepository.deleteTripById("1", onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle() // Ensure all asynchronous operations complete

        verify(mockDocumentReference).delete()
    }

    @Test
    fun IF_deleteTripById_fails_THEN_callOnFailure() {
        `when`(mockDocumentReference.delete())
            .thenReturn(Tasks.forException(Exception("Test exception")))

        tripRepository.deleteTripById(
            "1",
            onSuccess = { fail("Success callback should not be called") },
            onFailure = { assert(it.message == "Test exception") })
    }

    @Test
    fun updateTrip_shouldCallDocumentReferenceSet() {
        `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

        tripRepository.updateTrip(trip, onSuccess = {}, onFailure = {})

        shadowOf(Looper.getMainLooper()).idle()

        verify(mockDocumentReference).set(any())
    }

    @Test
    fun IF_updateTrip_fails_THEN_callOnFailure() {
        `when`(mockDocumentReference.set(any()))
            .thenReturn(Tasks.forException(Exception("Test exception")))

        tripRepository.updateTrip(
            trip,
            onSuccess = { fail("Success callback should not be called") },
            onFailure = { assert(it.message == "Test exception") })
    }
}