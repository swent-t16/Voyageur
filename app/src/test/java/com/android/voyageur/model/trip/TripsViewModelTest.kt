import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TripsViewModelTest {
  private lateinit var tripsRepository: TripRepository
  private lateinit var tripsViewModel: TripsViewModel

  private val trip =
      Trip(
          "1",
          "creator",
          emptyList(),
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
    tripsRepository = mock(TripRepository::class.java)
    tripsViewModel = TripsViewModel(tripsRepository)
    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
  }

  @Test
  fun testSelectTrip() {
    tripsViewModel.selectTrip(trip)
    assert(tripsViewModel.selectedTrip.value == trip)
  }

  @Test
  fun testGetNewTripId() {
    `when`(tripsRepository.getNewTripId()).thenReturn("uid")
    assertThat(tripsViewModel.getNewTripId(), `is`("uid"))
  }

  @Test
  fun getTripsCallsRepository() {

    // Simulate a successful result from the repository

  }

  @Test
  fun createTripCallsRepository() {
    tripsViewModel.createTrip(trip)
    verify(tripsRepository).createTrip(any(), any(), any())
  }

  @Test
  fun deleteTripByIdCallsRepository() {
    tripsViewModel.deleteTripById("1")
    verify(tripsRepository).deleteTripById(any(), any(), any())
  }

  @Test
  fun updateTripCallsRepository() {
    tripsViewModel.updateTrip(trip)
    verify(tripsRepository).updateTrip(any(), any(), any())
  }

  @Test
  fun testUploadImageToFirebase_success() {
    // Mock Firebase components
    val mockUri = mock(Uri::class.java)
    val mockStorage = mock(FirebaseStorage::class.java)
    val mockStorageRef = mock(StorageReference::class.java)
    val mockFileRef = mock(StorageReference::class.java)
    val mockTask = mock(UploadTask::class.java)
    val mockSuccessUri = mock(Uri::class.java)

    // Stubbing methods
    `when`(mockUri.lastPathSegment).thenReturn("image.jpg")
    `when`(mockStorage.reference).thenReturn(mockStorageRef)
    `when`(mockStorageRef.child(anyString())).thenReturn(mockFileRef)
    `when`(mockFileRef.putFile(any())).thenReturn(mockTask)

    // Simulate success scenario
    doAnswer {
          val successListener = it.getArgument<OnSuccessListener<UploadTask.TaskSnapshot>>(0)
          successListener.onSuccess(mock(UploadTask.TaskSnapshot::class.java))
          null
        }
        .`when`(mockTask)
        .addOnSuccessListener(any<OnSuccessListener<UploadTask.TaskSnapshot>>())

    doAnswer {
          val successListener = it.getArgument<OnSuccessListener<Uri>>(0)
          `when`(mockSuccessUri.toString()).thenReturn("https://firebase.com/download/image.jpg")
          successListener.onSuccess(mockSuccessUri)
          null
        }
        .`when`(mockFileRef)
        .downloadUrl

    // Test the upload function
    tripsViewModel.uploadImageToFirebase(
        mockUri,
        onSuccess = { url -> assert(url == "https://firebase.com/download/image.jpg") },
        onFailure = {
          assert(false) // This should not be called in the success scenario
        })
  }

  @Test
  fun testUploadImageToFirebase_failure() {
    // Mock Firebase components
    val mockUri = mock(Uri::class.java)
    val mockStorage = mock(FirebaseStorage::class.java)
    val mockStorageRef = mock(StorageReference::class.java)
    val mockFileRef = mock(StorageReference::class.java)
    val mockTask = mock(UploadTask::class.java)

    // Stubbing methods
    `when`(mockUri.lastPathSegment).thenReturn("image.jpg")
    `when`(mockStorage.reference).thenReturn(mockStorageRef)
    `when`(mockStorageRef.child(anyString())).thenReturn(mockFileRef)
    `when`(mockFileRef.putFile(any())).thenReturn(mockTask)

    // Simulate failure scenario
    doAnswer {
          val failureListener = it.getArgument<OnFailureListener>(0)
          failureListener.onFailure(Exception("Upload failed"))
          null
        }
        .`when`(mockTask)
        .addOnFailureListener(any<OnFailureListener>())

    // Test the upload function
    tripsViewModel.uploadImageToFirebase(
        mockUri,
        onSuccess = {
          assert(false) // This should not be called in the failure scenario
        },
        onFailure = { exception -> assert(exception.message == "Upload failed") })
  }
}
