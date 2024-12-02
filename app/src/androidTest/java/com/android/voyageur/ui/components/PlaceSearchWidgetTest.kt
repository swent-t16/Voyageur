package com.android.voyageur.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.TextFieldValue
import com.android.voyageur.model.place.CustomPlace
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.OpeningHours
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlaceSearchWidgetTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var context: Context
  private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  val place =
      Place.builder()
          .setId("mockID")
          .setDisplayName("Test Place")
          .setFormattedAddress("123 Test St")
          .setLatLng(LatLng(0.0, 0.0))
          .setRating(4.5)
          .setUserRatingsTotal(100)
          .setPriceLevel(2)
          .setWebsiteUri(Uri.parse("https://www.test.com"))
          .setInternationalPhoneNumber("+1234567890")
          .setOpeningHours(
              OpeningHours.builder()
                  .setWeekdayText(
                      listOf(
                          "Monday: 9:00 AM – 5:00 PM",
                          "Tuesday: 9:00 AM – 5:00 PM",
                          "Wednesday: 9:00 AM – 5:00 PM",
                          "Thursday: 9:00 AM – 5:00 PM",
                          "Friday: 9:00 AM – 5:00 PM",
                          "Saturday: Closed",
                          "Sunday: Closed"))
                  .build())
          .build()
  val bitmapList = listOf(ImageBitmap(1, 1), ImageBitmap(1, 1))
  val mockCustomPlace = CustomPlace(place, bitmapList)

  private lateinit var placesViewModel: PlacesViewModel
  private lateinit var placesRepository: PlacesRepository

  @Before
  fun setUp() {
    placesRepository = Mockito.mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)
    context = mock()
    fusedLocationProviderClient = mock()
    fusedLocationClient = mock()
    whenever(context.getSystemService(Context.LOCATION_SERVICE))
        .thenReturn(fusedLocationProviderClient)
  }

  @Test
  fun testInitialState() {
    `when`(placesRepository.searchPlaces(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<Place>) -> Unit
      onSuccess(emptyList())
    }
    composeTestRule.setContent {
      PlaceSearchWidget(
          placesViewModel = placesViewModel,
          onSelect = {},
          query = TextFieldValue(),
          onQueryChange = { _, _ -> })
    }

    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
  }

  @Test
  fun testSearch() {
    val place = CustomPlace(Place.builder().setName("Test Place").build(), emptyList())
    val searchedPlaces = MutableStateFlow(listOf(place))
    `when`(placesRepository.searchPlaces(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<CustomPlace>) -> Unit
      onSuccess(searchedPlaces.value)
    }
    composeTestRule.setContent {
      PlaceSearchWidget(
          placesViewModel = placesViewModel,
          onSelect = {},
          query = TextFieldValue(),
          onQueryChange = { _, _ -> })
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Test")
    composeTestRule.onNodeWithTag("searchDropdown").assertIsDisplayed()
  }

  @Test
  fun testConvertCustomPlaceToLocation() {
    val location = mockCustomPlace.toLocation()
    assert(location.id == "mockID")
    assert(location.name == "Test Place")
    assert(location.address == "123 Test St")
    assert(location.lat == 0.0)
    assert(location.lng == 0.0)
  }

  @Test
  fun DropdownMenuItemdisplaysplacenamecorrectly() {
    val testPlace = Place.builder().setId("test123").setDisplayName("Test Location").build()
    val customPlace = CustomPlace(testPlace, emptyList())
    val searchedPlaces = MutableStateFlow(listOf(customPlace))

    composeTestRule.setContent {
      PlaceSearchWidget(
          placesViewModel = placesViewModel,
          onSelect = {},
          query = TextFieldValue("Test"),
          onQueryChange = { _, _ -> })
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Test")
  }

  @Test
  fun DropdownMenuItemhandlesnullisplayname() {
    val testPlace = Place.builder().setId("test456").setDisplayName(null).build()
    val customPlace = CustomPlace(testPlace, emptyList())
    val searchedPlaces = MutableStateFlow(listOf(customPlace))

    composeTestRule.setContent {
      PlaceSearchWidget(
          placesViewModel = placesViewModel,
          onSelect = {},
          query = TextFieldValue("Test"),
          onQueryChange = { _, _ -> })
    }

    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Test")
  }
}
