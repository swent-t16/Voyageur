package com.android.voyageur.ui.trip.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SettingsScreenTest {

  private val sampleTrip = Trip(name = "Sample Trip")
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    tripsViewModel = mock(TripsViewModel::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    // Mock selectedTrip as StateFlow
    val tripStateFlow: StateFlow<Trip?> = MutableStateFlow(sampleTrip)
    `when`(tripsViewModel.selectedTrip).thenReturn(tripStateFlow)
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)
  }

  @Test
  fun hasRequiredComponents() {
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

  @Test
  fun displaysBottomNavigationCorrectly() {
    composeTestRule.setContent {
      SettingsScreen(
          trip = sampleTrip,
          navigationActions = navigationActions,
          tripsViewModel = tripsViewModel,
          userViewModel,
          placesViewModel)
    }

    // Check that the bottom navigation menu is displayed
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    // Verify that the bottom navigation has items with correct actions
    LIST_TOP_LEVEL_DESTINATION.forEach { destination ->
      composeTestRule.onNodeWithText(destination.textId).assertExists()
    }
  }
}
