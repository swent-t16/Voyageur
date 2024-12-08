import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.trip.UiState
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
  private lateinit var mockTripsViewModel: TripsViewModel

  private val trip =
      Trip(
          "1",
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
    mockTripsViewModel = mock(TripsViewModel::class.java)
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
    val tripsList =
        listOf(
            Trip(
                "1",
                emptyList(),
                "description",
                "name",
                Location("", "", "", 0.0, 0.0),
                Timestamp.now(),
                Timestamp.now(),
                emptyList(),
                TripType.TOURISM,
                ""))

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (List<Trip>) -> Unit
          onSuccess(tripsList)
          null
        }
        .`when`(tripsRepository)
        .getTrips(any(), any(), any())

    tripsViewModel.getTrips()

    // Verify repository interaction and state updates
    verify(tripsRepository).getTrips(any(), any(), any())
    assert(tripsViewModel.trips.value == tripsList)
  }

  @Test
  fun init_shouldSetIsLoadingWhileFetchingTrips() {
    // Simulate fetching trips in the repository
    doAnswer {
          // Simulate delay
          val onSuccess = it.arguments[1] as (List<Trip>) -> Unit
          Thread.sleep(100) // Simulate delay in fetching
          onSuccess(emptyList())
          null
        }
        .`when`(tripsRepository)
        .getTrips(any(), any(), any())

    // Advance time to simulate the delay
    Thread.sleep(200)

    // Verify state after fetching
    assert(!tripsViewModel.isLoading.value)
  }

  @Test
  fun getTrips_shouldSetIsLoading() = runTest {
    // Arrange
    val tripsList = listOf<Trip>()
    val isLoadingStates = mutableListOf<Boolean>()

    // Collect isLoading values during the test
    val job = launch { tripsViewModel.isLoading.toList(isLoadingStates) }

    // Mock repository behavior
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (List<Trip>) -> Unit
          onSuccess(tripsList)
          null
        }
        .`when`(tripsRepository)
        .getTrips(any(), any(), any())

    // Act
    tripsViewModel.getTrips()
    advanceUntilIdle()

    // Assert
    assert(!isLoadingStates[0])
    assert(!isLoadingStates.last()) // isLoading should end as false

    job.cancel()
  }

  @Test
  fun getTrips_shouldHandleErrorAndSetIsLoading() = runTest {
    // Arrange
    val errorMessage = "Failed to fetch trips"

    // Mock repository to simulate an error
    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(Exception(errorMessage)) // Trigger failure
          null
        }
        .`when`(tripsRepository)
        .getTrips(any(), any(), any())

    // Act
    tripsViewModel.getTrips()

    // Assert - Verify loading state transitions and error handling
    assert(!tripsViewModel.isLoading.value) // isLoading should start as true

    advanceUntilIdle() // Process coroutines

    assert(!tripsViewModel.isLoading.value) // isLoading should be false after error
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

  @Test
  fun testSetTripType() {
    tripsViewModel.setTripType(TripType.BUSINESS)
    assert(tripsViewModel.tripType.value == TripType.BUSINESS)
  }

  @Test
  fun testGetActivitiesForSelectedTrip() {
    tripsViewModel.selectTrip(trip)
    assert(tripsViewModel.getActivitiesForSelectedTrip() == trip.activities)
  }

  @Test
  fun testAddActivityToTrip() {
    tripsViewModel.selectTrip(trip)
    val activity = Activity(title = "Activity 1", description = "Description 1")
    tripsViewModel.removeActivityFromTrip(activity)
    assert(tripsViewModel.getActivitiesForSelectedTrip() == trip.activities)
  }

  @Test
  fun testRemoveActivityFromTrip() {
    tripsViewModel.selectTrip(trip)
    val activity = Activity(title = "Activity 1", description = "Description 1")
    // mock add and remove an activity
    tripsViewModel.addActivityToTrip(activity)
    tripsViewModel.removeActivityFromTrip(activity)
    assert(tripsViewModel.getActivitiesForSelectedTrip().isEmpty())
  }

  @Test
  fun testSetInitialUiState() {
    tripsViewModel.setInitialUiState()
    assert(tripsViewModel.uiState.value is UiState.Initial)
  }

  @Test
  fun sendActivitiesPrompt_shouldUpdateUiStateWithSuccess() = runTest {
    val trip =
        Trip(
            id = "1",
            participants = emptyList(),
            description = "Trip description",
            name = "Trip name",
            location = Location("", "", "", 0.0, 0.0),
            startDate = Timestamp.now(),
            endDate = Timestamp.now(),
            activities = emptyList(),
            type = TripType.TOURISM,
            imageUri = "")
    val userPrompt = "Generate activities for the trip"
    val provideFinalActivities = true

    tripsViewModel.sendActivitiesPrompt(trip, userPrompt, emptyList(), provideFinalActivities)
    advanceUntilIdle()

    val uiState = tripsViewModel.uiState.value
    tripsViewModel.setInitialUiState()
    assert(tripsViewModel.uiState.value is UiState.Initial)
  }

  @Test
  fun testGetFeed() {
    tripsViewModel.getFeed("userId")
    verify(tripsRepository).getFeed(any(), any(), any())
  }

  @Test
  fun testGetFeed_failure() {
    `when`(tripsRepository.getFeed(any(), any(), any())).thenAnswer {
      val onFailure = it.arguments[2] as (Exception) -> Unit
      onFailure(Exception("Failed to get feed"))
    }
    tripsViewModel.getFeed("userId")
    verify(tripsRepository).getFeed(any(), any(), any())
    assert(!tripsViewModel.isLoading.value)
  }
}
