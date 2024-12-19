package com.android.voyageur.ui.trip.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class SettingsScreenTest {

  private val sampleTrip = Trip(name = "Sample Trip")

  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel
  private lateinit var tripRepository: TripRepository
  private lateinit var tripInviteRepository: TripInviteRepository
  private lateinit var firebaseAuth: FirebaseAuth
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    tripInviteRepository = mock(TripInviteRepository::class.java)
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)
    tripsViewModel =
        TripsViewModel(
            tripsRepository = tripRepository,
            tripInviteRepository = tripInviteRepository,
            firebaseAuth = firebaseAuth)
    whenever(firebaseAuth.uid).thenReturn("mockUserId")
    whenever(firebaseAuth.uid.orEmpty()).thenReturn("mockUserId")
    whenever(tripRepository.getNewTripId()).thenReturn("mockTripId")
    whenever(navigationActions.currentRoute()).thenReturn(Screen.ADD_TRIP)
  }

  @Test
  fun hasRequiredComponents() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent {
      SettingsScreen(
          trip = sampleTrip,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel,
          userViewModel,
          placesViewModel)
    }

    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun displaysCorrectTripNameInEditMode() {
    tripsViewModel.selectTrip(sampleTrip)
    composeTestRule.setContent {
      SettingsScreen(
          trip = sampleTrip,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel,
          userViewModel,
          placesViewModel)
    }

    // Check if the screen displays the trip name correctly in edit mode
    composeTestRule.onNodeWithTag("inputTripTitle").assertTextContains("Sample Trip")
  }
}
