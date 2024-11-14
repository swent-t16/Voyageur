package com.android.voyageur.model.place

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PlacesViewModelTest {
  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)
    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun setQueryUpdatesQuery() {
    val query = "test"
    placesViewModel.setQuery(query, LatLng(0.0, 0.0))
    assert(placesViewModel.query.value == query)
  }

  @Test
  fun setQueryTriggersSearchAfterDelay() = runTest {
    val query1 = "test1"
    val query2 = "test2"
    placesViewModel.setQuery(query1, LatLng(0.0, 0.0))
    assert(placesViewModel.query.value == query1)
    verify(placesRepository, never()).searchPlaces(eq(query1), any(), any(), any())
    placesViewModel.setQuery(query2, LatLng(0.0, 0.0))
    delay(200) // Wait for debounce delay
    verify(placesRepository).searchPlaces(eq(query2), any(), any(), any())
  }

  @Test
  fun testSelectPlace() {
    val place = CustomPlace(place = mock(Place::class.java), photos = emptyList())
    placesViewModel.selectPlace(place)
    assert(placesViewModel.selectedPlace.value == place)
  }

  @Test
  fun testDeselectPlace() {
    val place = CustomPlace(place = mock(Place::class.java), photos = emptyList())
    placesViewModel.selectPlace(place)
    assert(placesViewModel.selectedPlace.value == place)
    placesViewModel.deselectPlace()
    assert(placesViewModel.selectedPlace.value == null)
  }
}
