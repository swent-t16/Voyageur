import android.content.Context
import android.location.Location
import com.android.voyageur.ui.components.requestUserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class UserLocationRequestTest {

  private val testDispatcher = TestCoroutineDispatcher()

  @Mock private lateinit var mockContext: Context

  @Mock private lateinit var mockFusedLocationClient: FusedLocationProviderClient

  @Before
  fun setup() {
    MockitoAnnotations.initMocks(this)
    Dispatchers.setMain(testDispatcher)

    mockStatic(LocationServices::class.java)
    `when`(LocationServices.getFusedLocationProviderClient(mockContext))
        .thenReturn(mockFusedLocationClient)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  fun `requestUserLocation returns LatLng when location is available`(): Unit = runBlocking {
    // Arrange
    val mockLocation = mock(Location::class.java)
    `when`(mockLocation.latitude).thenReturn(40.7128)
    `when`(mockLocation.longitude).thenReturn(-74.0060)

    val successTask = Tasks.forResult(mockLocation)
    `when`(mockFusedLocationClient.lastLocation).thenReturn(successTask)

    // Act
    val result = requestUserLocation(mockContext)

    // Assert
    assertNotNull(result)
    result?.latitude?.let { assertEquals(40.7128, it, 0.0001) }
    result?.longitude?.let { assertEquals(-74.0060, it, 0.0001) }
  }
}
