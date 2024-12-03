package com.android.voyageur.ui.components

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.MainActivity
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.trip.*
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.overview.AddTripScreen
import com.android.voyageur.ui.profile.EditProfileScreen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Date
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class GalleryPermissionTests {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  private val testUri = Uri.parse("content://test/image.jpg")

  private lateinit var navigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var tripRepository: TripRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var friendRequestRepository: FriendRequestRepository

  @Before
  fun setUp() {
    // Initialize mocks
    navigationActions = mock()
    userRepository = mock()
    tripRepository = mock()
    firebaseAuth = mock()
    firebaseUser = mock()
    friendRequestRepository = mock()

    // Mock Firebase Auth
    whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
    whenever(firebaseUser.uid).thenReturn("test-uid")
    whenever(firebaseUser.email).thenReturn("test@example.com")

    // Mock TripRepository methods
    whenever(tripRepository.getNewTripId()).thenReturn("new-trip-id")

    // Create TripsViewModel using Factory
    val factory =
        object : ViewModelProvider.Factory {
          override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TripsViewModel::class.java)) {
              @Suppress("UNCHECKED_CAST") return TripsViewModel(tripRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }

    tripsViewModel = factory.create(TripsViewModel::class.java)

    // Mock trips data
    val testTrip =
        Trip(
            id = "test-trip",
            name = "Test Trip",
            description = "Test Description",
            creator = "test-uid",
            startDate = Timestamp(Date()),
            endDate = Timestamp(Date()),
            type = TripType.TOURISM,
            imageUri = testUri.toString(),
            activities = emptyList(),
            locations = emptyList(),
            participants = emptyList())

    // Set initial trip data
    tripsViewModel.selectTrip(testTrip)

    // Set up UserViewModel with mock repository responses
    whenever(userRepository.updateUser(any(), any(), any())).doAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as () -> Unit
      onSuccess()
    }

    userViewModel = UserViewModel(userRepository, firebaseAuth, friendRequestRepository)

    // Setup initial user state
    val initialUser =
        User(
            id = "test-uid",
            name = "Test User",
            email = "test@example.com",
            profilePicture = "",
            interests = emptyList())
    userViewModel._user.value = initialUser
    userViewModel._isLoading.value = false

    // Mock navigation
    whenever(navigationActions.currentRoute()).thenReturn(Route.EDIT_PROFILE)
  }

  @Test
  fun editProfile_permissionButtonIsVisible() {
    composeTestRule.activity.setContent {
      EditProfileScreen(userViewModel = userViewModel, navigationActions = navigationActions)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("editImageButton").assertIsDisplayed()
  }

  @Test
  fun editProfile_handlePermissionDenied() {
    composeTestRule.activity.setContent {
      EditProfileScreen(userViewModel = userViewModel, navigationActions = navigationActions)
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag("defaultProfilePicture", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun editProfile_handleSuccessfulImageSelection() {
    composeTestRule.activity.setContent {
      EditProfileScreen(userViewModel = userViewModel, navigationActions = navigationActions)
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag("defaultProfilePicture", useUnmergedTree = true)
        .assertIsDisplayed()

    // Update user with profile picture
    userViewModel._user.value = userViewModel._user.value?.copy(profilePicture = testUri.toString())

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profilePicture", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun addTrip_permissionButtonIsVisible() {
    composeTestRule.activity.setContent {
      AddTripScreen(tripsViewModel = tripsViewModel, navigationActions = navigationActions)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Select Image from Gallery").assertIsDisplayed()
  }

  @Test
  fun addTrip_handleSuccessfulImageSelection() {
    composeTestRule.activity.setContent {
      AddTripScreen(tripsViewModel = tripsViewModel, navigationActions = navigationActions)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("imageContainer", useUnmergedTree = true).assertIsDisplayed()
  }
}
