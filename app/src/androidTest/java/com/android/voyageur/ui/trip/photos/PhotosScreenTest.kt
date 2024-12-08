package com.android.voyageur.ui.trip.photos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class PhotosScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockTripsViewModel: TripsViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel
  @get:Rule val composeTestRule = createComposeRule()

  private val sampleTrip =
      Trip(name = "Sample Trip", photos = listOf("photo1_url", "photo2_url", "photo3_url"))

  private val emptyTrip = Trip(name = "Empty Trip", photos = emptyList())

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    mockTripsViewModel = mock(TripsViewModel::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    // Mock selectedTrip as StateFlow
    val tripStateFlow: StateFlow<Trip?> = MutableStateFlow(sampleTrip)
    `when`(mockTripsViewModel.selectedTrip).thenReturn(tripStateFlow)
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)
  }

  @Test
  fun photosScreen_hasInitialComponents() {
    composeTestRule.setContent {
      PhotosScreen(
          tripsViewModel = mockTripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    composeTestRule.onNodeWithTag("photosScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photosTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Photos").assertIsDisplayed()
  }

  @Test
  fun photosScreen_addPhoto() {
    composeTestRule.setContent {
      PhotosScreen(
          tripsViewModel = mockTripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    composeTestRule.onNodeWithTag("addPhotoButton").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun photosScreen_displaysEmptyState_whenNoPhotos() {
    `when`(mockTripsViewModel.selectedTrip).thenReturn(MutableStateFlow(emptyTrip))

    composeTestRule.setContent {
      PhotosScreen(
          tripsViewModel = mockTripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    composeTestRule.onNodeWithTag("emptyPhotosPrompt").assertIsDisplayed()
    composeTestRule.onNodeWithText("You have no photos yet.").assertIsDisplayed()
  }

  @Test
  fun photosScreen_displaysPhotos_whenPhotosExist() {
    composeTestRule.setContent {
      PhotosScreen(
          tripsViewModel = mockTripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    composeTestRule.onNodeWithTag("lazyVerticalGrid").assertIsDisplayed()
    sampleTrip.photos.forEach { photoUri ->
      composeTestRule.onNodeWithTag("photoThumbnail_$photoUri").assertIsDisplayed()
    }
  }

  @Test
  fun photosScreen_handlesPhotoDialogAndNavigation() {
    composeTestRule.setContent {
      PhotosScreen(
          tripsViewModel = mockTripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    // Open photo dialog
    composeTestRule.onNodeWithTag("photoThumbnail_photo1_url").performClick()
    composeTestRule.onNodeWithTag("photoDialog_photo1_url").assertIsDisplayed()

    // Navigate photo list in dialog
    composeTestRule.onNodeWithTag("goRightButton").performClick()
    composeTestRule.onNodeWithTag("goLeftButton").performClick()
    composeTestRule.onNodeWithTag("closeButton_photo1_url").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteButton_photo1_url").assertIsDisplayed()

    composeTestRule.onNodeWithTag("photoDialog_photo1_url").performClick()
  }

  @Test
  fun photosScreen_handlesDeletePhoto() {
    composeTestRule.setContent {
      PhotosScreen(
          tripsViewModel = mockTripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    // Open photo dialog
    composeTestRule.onNodeWithTag("photoThumbnail_photo2_url").performClick()

    // Open delete dialog
    composeTestRule.onNodeWithTag("deleteButton_photo2_url").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteButton_photo2_url").performClick()
  }

  @Test
  fun photosScreen_navigatesBottomMenu() {
    composeTestRule.setContent {
      PhotosScreen(
          tripsViewModel = mockTripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    // Verify that the bottom navigation has items with correct actions
    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }
}
