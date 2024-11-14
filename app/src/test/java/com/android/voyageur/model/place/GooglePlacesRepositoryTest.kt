package com.android.voyageur.model.place

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoResponse
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GooglePlacesRepositoryTest {

  @Mock private lateinit var mockPlacesClient: PlacesClient
  @Mock private lateinit var mockFetchPlaceResponse: FetchPlaceResponse
  @Mock
  private lateinit var mockFindAutocompletePredictionsResponse: FindAutocompletePredictionsResponse
  @Mock private lateinit var mockFetchPhotoResponse: FetchPhotoResponse
  @Mock private lateinit var mockAutocompletePrediction: AutocompletePrediction
  @Mock private lateinit var mockPlace: Place
  @Mock private lateinit var mockPhotoMetadata: PhotoMetadata

  private lateinit var googlePlacesRepository: GooglePlacesRepository

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    googlePlacesRepository = GooglePlacesRepository(mockPlacesClient)

    // Mock the fetchPlace method to return a valid Task
    val mockFetchPlaceTask = mock(Task::class.java) as Task<FetchPlaceResponse>
    `when`(mockPlacesClient.fetchPlace(any())).thenReturn(mockFetchPlaceTask)
    `when`(mockFetchPlaceTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<FetchPlaceResponse>
      `when`(mockFetchPlaceResponse.place).thenReturn(mockPlace)
      listener.onSuccess(mockFetchPlaceResponse)
      mockFetchPlaceTask
    }
    `when`(mockFetchPlaceTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Places API error"))
      mockFetchPlaceTask
    }
  }

  @Test
  fun testSearchPlaces_success() {
    val query = "test"
    val location = LatLng(37.7749, -122.4194)
    val placeId = "testPlaceId"
    val placeIds = listOf(placeId)
    val mockTask = mock(Task::class.java) as Task<FindAutocompletePredictionsResponse>

    `when`(mockPlacesClient.findAutocompletePredictions(any())).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as OnSuccessListener<FindAutocompletePredictionsResponse>
      `when`(mockFindAutocompletePredictionsResponse.autocompletePredictions)
          .thenReturn(listOf(mockAutocompletePrediction))
      `when`(mockAutocompletePrediction.placeId).thenReturn(placeId)
      listener.onSuccess(mockFindAutocompletePredictionsResponse)
      mockTask
    }

    val onSuccess: (List<CustomPlace>) -> Unit = { places -> assertEquals(1, places.size) }
    val onFailure: (Exception) -> Unit = { fail("Failure callback should not be called") }

    googlePlacesRepository.searchPlaces(query, location, onSuccess, onFailure)

    verify(mockPlacesClient).findAutocompletePredictions(any())
    verify(mockTask).addOnSuccessListener(any())
  }

  @Test
  fun testSearchPlaces_failure() {
    val query = "test"
    val exception = Exception("Places API error")
    val mockTask = mock(Task::class.java) as Task<FindAutocompletePredictionsResponse>

    `when`(mockPlacesClient.findAutocompletePredictions(any())).thenReturn(mockTask)
    `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener =
          invocation.arguments[0] as OnSuccessListener<FindAutocompletePredictionsResponse>
      listener.onSuccess(mockFindAutocompletePredictionsResponse)
      mockTask
    }
    `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception)
      mockTask
    }

    val onSuccess: (List<CustomPlace>) -> Unit = { fail("Success callback should not be called") }
    val onFailure: (Exception) -> Unit = { e -> assertEquals("Places API error", e.message) }

    googlePlacesRepository.searchPlaces(query, null, onSuccess, onFailure)

    verify(mockPlacesClient).findAutocompletePredictions(any())
    verify(mockTask).addOnFailureListener(any())
  }

  @Test
  fun testFetchPhotoMetadataAndRequest() {
    val placeId = "testPlaceId"
    val mockFetchPhotoTask = mock(Task::class.java) as Task<FetchPhotoResponse>

    `when`(mockPlace.photoMetadatas).thenReturn(listOf(mockPhotoMetadata))

    `when`(mockPlacesClient.fetchPhoto(any())).thenReturn(mockFetchPhotoTask)
    `when`(mockFetchPhotoTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnSuccessListener<FetchPhotoResponse>
      listener.onSuccess(mockFetchPhotoResponse) // Simulate successful photo fetch
      mockFetchPhotoTask
    }
    `when`(mockFetchPhotoTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Photo API error")) // Simulate failure in photo fetch
      mockFetchPhotoTask
    }

    val mockBitmap = mock(Bitmap::class.java)
    `when`(mockFetchPhotoResponse.bitmap).thenReturn(mockBitmap)

    val onSuccess: (List<CustomPlace>) -> Unit = { customPlaces ->
      // Check if the bitmap list is not empty
      assertTrue(customPlaces[0].photos.isNotEmpty())
    }
    val onFailure: (Exception) -> Unit = { fail("Failure callback should not be called") }

    googlePlacesRepository.fetchPlaceDetails(listOf(placeId), onSuccess, onFailure)

    verify(mockPlacesClient).fetchPhoto(any())
    verify(mockFetchPhotoTask).addOnSuccessListener(any())
  }
}
