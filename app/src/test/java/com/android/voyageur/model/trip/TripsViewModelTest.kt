import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.trip.UiState
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
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
import org.mockito.MockedStatic
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TripsViewModelTest {
  private lateinit var tripsRepository: TripRepository
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var mockTripsViewModel: TripsViewModel

  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var firebaseAuthMockStatic: MockedStatic<FirebaseAuth>

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
          "",
          emptyList())

  @Before
  fun setUp() {
    tripsRepository = mock(TripRepository::class.java)
    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
    `when`(firebaseUser.uid).thenReturn("123")
    `when`(firebaseUser.displayName).thenReturn("Test User")
    `when`(firebaseUser.email).thenReturn("test@example.com")
    `when`(firebaseUser.photoUrl).thenReturn(null)

    tripsViewModel = TripsViewModel(tripsRepository, true, firebaseAuth)
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
  fun testAuthStateListener() {
    // Arrange
    val mockAuthStateListener = tripsViewModel.authStateListener
    val mockFirebaseUser = mock(FirebaseUser::class.java)
    val userId = "123"

    // Stub the FirebaseAuth behavior
    `when`(firebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.uid).thenReturn(userId)

    // Simulate adding the listener and a change in auth state
    mockAuthStateListener.onAuthStateChanged(firebaseAuth)

    // Assert - Verify tripsRepository listens for trip updates
    verify(tripsRepository).listenForTripUpdates(any(), any(), any())
  }

  @Test
  fun testAuthStateListener_noUser() {
    // Arrange
    val mockAuthStateListener = tripsViewModel.authStateListener

    // Stub FirebaseAuth to return no current user
    `when`(firebaseAuth.currentUser).thenReturn(null)

    // Simulate adding the listener and a change in auth state
    mockAuthStateListener.onAuthStateChanged(firebaseAuth)

    // Assert - Verify tripsRepository does not listen for trip updates
    verify(tripsRepository, never()).listenForTripUpdates(any(), any(), any())
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
                "",
                emptyList()))

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
    tripsViewModel.addActivityToTrip(activity)
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
  fun testGetPhotosForSelectedTrip() {
    tripsViewModel.selectTrip(trip)
    assert(tripsViewModel.getPhotosForSelectedTrip() == trip.photos)
  }

  @Test
  fun testAddPhotoToTrip() {
    tripsViewModel.selectTrip(trip)
    val photo = "http://example.com/image.jpg"
    tripsViewModel.addPhotoToTrip(photo)
    assert(tripsViewModel.getPhotosForSelectedTrip() == trip.photos)
  }

  @Test
  fun testRemovePhotoFromTrip() {
    tripsViewModel.selectTrip(trip)
    val photo = "http://example.com/image.jpg"
    // mock add and remove a photo
    tripsViewModel.addPhotoToTrip(photo)
    tripsViewModel.removePhotoFromTrip(photo)
    assert(tripsViewModel.getPhotosForSelectedTrip().isEmpty())
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
  fun addPhotoToTrip_onSuccess() {
    // Arrange
    val photoUrl = "https://example.com/photo.jpg"
    val updatedTrip = trip.copy(photos = trip.photos + photoUrl)
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as () -> Unit
          onSuccess()
          null
        }
        .whenever(tripsRepository)
        .updateTrip(any(), any(), any())

    // Act
    tripsViewModel.selectTrip(trip)
    tripsViewModel.addPhotoToTrip(photoUrl)

    // Assert
    verify(tripsRepository).updateTrip(any(), any(), any())
  }

  @Test
  fun addPhotoToTrip_onFailure() {
    // Arrange
    val photoUrl = "https://example.com/photo.jpg"
    val exception = Exception("Failed to add photo")
    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .whenever(tripsRepository)
        .updateTrip(any(), any(), any())

    // Act
    tripsViewModel.selectTrip(trip)
    tripsViewModel.addPhotoToTrip(photoUrl)

    // Assert
    verify(tripsRepository).updateTrip(any(), any(), any())
    assert(tripsViewModel.selectedTrip.value?.photos?.contains(photoUrl) == false)
  }

  @Test
  fun removePhotoFromTrip_onSuccess() {
    // Arrange
    val photoUrl = "https://example.com/photo.jpg"
    val initialTrip = trip.copy(photos = trip.photos + photoUrl)
    val updatedTrip = trip.copy(photos = trip.photos - photoUrl)
    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as () -> Unit
          onSuccess()
          null
        }
        .whenever(tripsRepository)
        .updateTrip(any(), any(), any())

    // Act
    tripsViewModel.selectTrip(initialTrip)
    tripsViewModel.removePhotoFromTrip(photoUrl)

    // Assert
    verify(tripsRepository).updateTrip(any(), any(), any())
  }

  @Test
  fun removePhotoFromTrip_onFailure() {
    // Arrange
    val photoUrl = "https://example.com/photo.jpg"
    val exception = Exception("Failed to remove photo")
    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(exception)
          null
        }
        .whenever(tripsRepository)
        .updateTrip(any(), any(), any())

    // Act
    tripsViewModel.selectTrip(trip)
    tripsViewModel.removePhotoFromTrip(photoUrl)

    // Assert
    verify(tripsRepository).updateTrip(any(), any(), any())
  }

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

  @Test
  fun testAuthStateListener_onSuccess() {
    // Arrange
    val mockAuthStateListener = tripsViewModel.authStateListener
    val mockFirebaseUser = mock(FirebaseUser::class.java)
    val userId = "123"
    val tripsList =
        listOf(
            Trip(
                id = "1",
                participants = emptyList(),
                description = "Test Trip",
                name = "Trip 1",
                location = Location("", "", "", 0.0, 0.0),
                startDate = Timestamp.now(),
                endDate = Timestamp.now(),
                activities = emptyList(),
                type = TripType.TOURISM,
                imageUri = ""))

    // Stub FirebaseAuth and tripsRepository behavior
    `when`(firebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.uid).thenReturn(userId)

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (List<Trip>) -> Unit
          onSuccess(tripsList) // Simulate a successful callback
          null
        }
        .whenever(tripsRepository)
        .listenForTripUpdates(any(), any(), any())

    // Act
    mockAuthStateListener.onAuthStateChanged(firebaseAuth)

    // Assert - Verify trips were updated
    assert(tripsViewModel.trips.value == tripsList)
  }

  @Test
  fun testAuthStateListener_onFailure() {
    // Arrange
    val mockAuthStateListener = tripsViewModel.authStateListener
    val mockFirebaseUser = mock(FirebaseUser::class.java)
    val userId = "123"
    val exception = Exception("Failed to listen for trip updates")

    // Stub FirebaseAuth and tripsRepository behavior
    `when`(firebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.uid).thenReturn(userId)

    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(exception) // Simulate a failure callback
          null
        }
        .whenever(tripsRepository)
        .listenForTripUpdates(any(), any(), any())

    // Act
    mockAuthStateListener.onAuthStateChanged(firebaseAuth)

    // Assert - Verify trips were not updated and error is handled
    assert(tripsViewModel.trips.value.isEmpty()) // Trips should remain empty on failure
    verify(tripsRepository).listenForTripUpdates(any(), any(), any())
  }

  @Test
  fun testCopyTrip() {
    val userRepository = mock(UserRepository::class.java)
    val friendRequestRepository = mock(FriendRequestRepository::class.java)
    val userViewModel =
        UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)

    val mockListenerRegistration = mock(ListenerRegistration::class.java)
    whenever(userRepository.listenToUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (User) -> Unit
      onSuccess(User(id = "test"))
      mockListenerRegistration
    }
    tripsViewModel.selectTrip(Trip(id = "1"))
    userViewModel.loadUser("test")
    tripsViewModel.copyTrip(userViewModel) {}
    verify(tripsRepository).createTrip(any(), any(), any())
  }
}
