import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.google.firebase.Timestamp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

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
          emptyList(),
          Timestamp.now(),
          Timestamp.now(),
          emptyList(),
          TripType.TOURISM)

  @Before
  fun setUp() {
    tripsRepository = mock(TripRepository::class.java)
    tripsViewModel = TripsViewModel(tripsRepository)
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
}
