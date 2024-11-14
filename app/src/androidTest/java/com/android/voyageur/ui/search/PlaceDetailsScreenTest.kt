package com.android.voyageur.ui.search

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.place.CustomPlace
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.OpeningHours
import com.google.android.libraries.places.api.model.Place
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PlaceDetailsScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var placesViewModel: PlacesViewModel
  private lateinit var placesRepository: PlacesRepository

  @get:Rule val composeTestRule = createComposeRule()

  val place =
      Place.builder()
          .setName("Test Place")
          .setAddress("123 Test St")
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
  val customPlace = CustomPlace(place, bitmapList)

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    placesRepository = mock(PlacesRepository::class.java)
    userViewModel = UserViewModel(userRepository)
    placesViewModel = PlacesViewModel(placesRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Route.SEARCH)
    composeTestRule.setContent { PlaceDetailsScreen(navigationActions, placesViewModel) }
    placesViewModel.selectPlace(customPlace)
  }

  @Test
  fun testInitialState() {
    composeTestRule.onNodeWithTag("PlaceDetailsContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RatingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PriceLevelText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PhoneNumberText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("OpeningHoursWidget").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PhotoCarousel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("LearnMoreButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GetDirectionsButton").assertIsDisplayed()
  }

  @Test
  fun testNoRatingsAvailable() {
    placesViewModel.deselectPlace()
    val noRatingPlace =
        Place.builder()
            .setName("Test Place")
            .setAddress("123 Test St")
            .setLatLng(LatLng(0.0, 0.0))
            .setUserRatingsTotal(0)
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

    val noRatingCustomPlace = CustomPlace(noRatingPlace, bitmapList)
    placesViewModel.selectPlace(noRatingCustomPlace)

    composeTestRule.onNodeWithTag("NoRatingsText").assertIsDisplayed()
  }

  @Test
  fun testNoOpeningHoursAvailable() {
    placesViewModel.deselectPlace()
    val noOpeningHoursPlace =
        Place.builder()
            .setName("Test Place")
            .setAddress("123 Test St")
            .setLatLng(LatLng(0.0, 0.0))
            .setRating(4.5)
            .setUserRatingsTotal(100)
            .setPriceLevel(2)
            .setWebsiteUri(Uri.parse("https://www.test.com"))
            .setInternationalPhoneNumber("+1234567890")
            .build()

    val noOpeningHoursCustomPlace = CustomPlace(noOpeningHoursPlace, bitmapList)
    placesViewModel.selectPlace(noOpeningHoursCustomPlace)

    composeTestRule.onNodeWithTag("NoOpeningHoursText").assertIsDisplayed()
  }

  @Test
  fun testFallbackImage() {
    placesViewModel.deselectPlace()
    val noPhotosPlace =
        Place.builder()
            .setName("Test Place")
            .setAddress("123 Test St")
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

    val noPhotosCustomPlace = CustomPlace(noPhotosPlace, emptyList())
    placesViewModel.selectPlace(noPhotosCustomPlace)

    composeTestRule.onNodeWithTag("PlaceholderImage").assertIsDisplayed()
  }

  @Test
  fun testCarouselIconSwitchImages() {
    composeTestRule.onNodeWithTag("PhotoCarousel").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PreviousImageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NextImageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PlacePhoto0").assertIsDisplayed()

    composeTestRule.onNodeWithTag("NextImageButton").performClick()
    composeTestRule.onNodeWithTag("PlacePhoto1").assertIsDisplayed()

    composeTestRule.onNodeWithTag("PreviousImageButton").performClick()
    composeTestRule.onNodeWithTag("PlacePhoto0").assertIsDisplayed()
  }
}
