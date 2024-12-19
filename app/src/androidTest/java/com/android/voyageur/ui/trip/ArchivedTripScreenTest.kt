package com.android.voyageur.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.NavigationState
import com.android.voyageur.ui.navigation.Route
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*

class ArchivedTripsScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var tripInviteRepository: TripInviteRepository

  private val testUser = User(id = "test-user-id", email = "test@example.com", name = "Test User")

  @Before
  fun setUp() {
    tripRepository = mock()
    tripInviteRepository = mock()
    navigationActions = mock()
    userRepository = mock()
    firebaseAuth = mock()
    firebaseUser = mock()

    // Create real TripsViewModel with mocked dependencies
    tripsViewModel = TripsViewModel(tripRepository, tripInviteRepository)

    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
    `when`(firebaseUser.uid).thenReturn("test-user-id")
    `when`(firebaseUser.email).thenReturn("test@example.com")
    `when`(firebaseUser.displayName).thenReturn("Test User")

    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          onSuccess(testUser)
          null
        }
        .`when`(userRepository)
        .getUserById(anyString(), any(), any())

    userViewModel =
        UserViewModel(userRepository, firebaseAuth, mock(), addAuthStateListener = false)
    userViewModel._user.value = testUser
    userViewModel._isLoading.value = false

    `when`(navigationActions.currentRoute()).thenReturn(Route.ARCHIVED_TRIPS)
    `when`(navigationActions.getNavigationState()).thenReturn(NavigationState())
  }

  @Test
  fun whenLoading_showsLoadingIndicator() {
    // Given
    userViewModel._isLoading.value = true

    // When
    composeTestRule.setContent {
      ArchivedTripsScreen(
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    // Then
    composeTestRule.onNodeWithTag("loadingIndicator").assertExists()
  }

  @Test
  fun whenNoArchivedTrips_showsEmptyMessage() {
    // Given
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Trip>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .`when`(tripRepository)
        .getTrips(any(), any(), any())
    tripsViewModel.getTrips()

    // When
    composeTestRule.setContent {
      ArchivedTripsScreen(
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    // Then
    composeTestRule.onNodeWithTag("archivedTripsColumn").assertDoesNotExist()
  }

  @Test
  fun clickingBackButton_navigatesBack() {
    // When
    composeTestRule.setContent {
      ArchivedTripsScreen(
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    composeTestRule.onNodeWithTag("backFromArchiveButton").performClick()

    // Then
    verify(navigationActions).goBack()
  }

  @Test
  fun nullUser_doesNotDisplayScreen() {
    // Given
    userViewModel._user.value = null

    // When
    composeTestRule.setContent {
      ArchivedTripsScreen(
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    // Then
    composeTestRule.onNodeWithTag("archivedTripsColumn").assertDoesNotExist()
    composeTestRule.onNodeWithTag("emptyArchivePrompt").assertDoesNotExist()
  }

  @Test
  fun displaysOnlyArchivedTrips() {
    // Given
    val mockTrips =
        listOf(
            Trip(id = "1", name = "Archived Trip", archived = true),
            Trip(id = "2", name = "Active Trip", archived = false))
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Trip>) -> Unit>(1)
          onSuccess(mockTrips)
          null
        }
        .`when`(tripRepository)
        .getTrips(any(), any(), any())
    tripsViewModel.getTrips()

    // When
    composeTestRule.setContent {
      ArchivedTripsScreen(
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    // Then
    composeTestRule.onNodeWithText("Active Trip").assertDoesNotExist()
  }

  @Test
  fun hasRequiredComponents() {
    // When
    composeTestRule.setContent {
      ArchivedTripsScreen(
          tripsViewModel = tripsViewModel,
          navigationActions = navigationActions,
          userViewModel = userViewModel)
    }

    // Then
    composeTestRule.onNodeWithTag("backFromArchiveButton").assertExists()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertExists()
  }
}
