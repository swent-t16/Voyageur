package com.android.voyageur.ui.e2e

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.overview.AddTripScreen
import com.android.voyageur.ui.overview.OverviewScreen
import com.android.voyageur.ui.profile.ProfileScreen
import com.android.voyageur.ui.search.SearchScreen
import com.android.voyageur.ui.trip.TopTabs
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull

@RunWith(AndroidJUnit4::class)
class E2ETest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel

  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  private lateinit var tripInviteRepository: TripInviteRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser
  private lateinit var mockAuthResult: AuthResult
  private lateinit var mockAuthTask: Task<AuthResult>
  private lateinit var friendRequestRepository: FriendRequestRepository
  private val mockMail = "test@gmail.com"
  private val sampleTrip = Trip(name = "Sample Trip", participants = listOf("userId123"))

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripInviteRepository = mock(TripInviteRepository::class.java)
    tripsViewModel = TripsViewModel(tripRepository, tripInviteRepository)
    tripsViewModel.selectTrip(sampleTrip)
    friendRequestRepository = mock(FriendRequestRepository::class.java)

    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)

    userRepository = mock(UserRepository::class.java)

    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
    firebaseAuth = mock(FirebaseAuth::class.java)
    val firebaseUser = mock(FirebaseUser::class.java)

    // Mock FirebaseAuth to return our mocked firebaseUser
    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)

    // Mock methods of FirebaseUser to return non-null values
    `when`(firebaseUser.uid).thenReturn("123")
    `when`(firebaseUser.displayName).thenReturn("Test User")
    `when`(firebaseUser.email).thenReturn("test@example.com")
    `when`(firebaseUser.photoUrl).thenReturn(null) // Or a valid URI if needed

    // Mock userRepository.getUserById to call onSuccess with a User
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user = User(userId, "Test User", "test@example.com", interests = emptyList())
          onSuccess(user)
          null
        }
        .`when`(userRepository)
        .getUserById(anyString(), anyOrNull(), anyOrNull())
    // Mock the new id for the user dropdown
    `when`(tripRepository.getNewTripId()).thenReturn("456")

    // Mock userRepository.fetchUsersByIds to call onSuccess with the list of users
    doAnswer { invocation ->
          val userIds = invocation.getArgument<List<String>>(0)
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)

          val mockUsers =
              userIds.map { userId ->
                User(id = userId, name = "User $userId", email = "$userId@example.com")
              }
          onSuccess(mockUsers)
          null
        }
        .`when`(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    // Create the UserViewModel with the mocked userRepository and firebaseAuth
    userViewModel = UserViewModel(userRepository, firebaseAuth, friendRequestRepository)
    mockUser = mock(FirebaseUser::class.java)
    mockAuthResult = mock(AuthResult::class.java)
    mockAuthTask = mock(Task::class.java) as Task<AuthResult>

    `when`(firebaseAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.displayName).thenReturn("Test User")
    `when`(mockAuthTask.isSuccessful).thenReturn(true)
    `when`(mockAuthTask.result).thenReturn(mockAuthResult)

    `when`(firebaseAuth.signInWithCredential(any())).thenReturn(mockAuthTask)

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
  }

  @Suppress("RememberReturnType")
  @Test
  fun e2ETest() {
    userViewModel._user.value = User("1")

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigation = remember { NavigationActions(navController) }
      NavHost(
          navController = navController,
          startDestination = Route.OVERVIEW,
      ) {
        composable(Route.OVERVIEW) { OverviewScreen(tripsViewModel, navigation, userViewModel) }
        composable(Route.PROFILE) { ProfileScreen(userViewModel, tripsViewModel, navigation) }
        composable(Route.SEARCH) {
          SearchScreen(userViewModel, placesViewModel, tripsViewModel, navigation)
        }
        composable(Route.TOP_TABS) {
          TopTabs(tripsViewModel, navigation, userViewModel, placesViewModel)
        }
        composable(Screen.ADD_TRIP) {
          AddTripScreen(tripsViewModel, navigation, placesViewModel = placesViewModel)
        }
        composable(Screen.OVERVIEW) { OverviewScreen(tripsViewModel, navigation, userViewModel) }
        composable(Screen.PROFILE) { ProfileScreen(userViewModel, tripsViewModel, navigation) }
        composable(Screen.SEARCH) {
          SearchScreen(userViewModel, placesViewModel, tripsViewModel, navigation)
        }
        composable(Screen.TOP_TABS) {
          TopTabs(tripsViewModel, navigation, userViewModel, placesViewModel)
        }
      }
    }
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("createTripButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTripButton").performClick()
    composeTestRule.onNodeWithTag("addTrip").assertIsDisplayed() // check add trip

    composeTestRule.onNodeWithTag("goBackButton").performClick() // back to overview
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    val mockTrip =
        Trip(id = "mockTripId", participants = listOf("Alice", "Bob"), name = "Mock Trip")
    val mockTrips = listOf(mockTrip)

    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripsViewModel.getTrips()
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed() // check view by day of the trip
    composeTestRule.onNodeWithTag("cardItem").performClick()

    assert(tripsViewModel.selectedTrip.value == mockTrip)
    composeTestRule.onNodeWithTag("topTabs").assertIsDisplayed()

    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()

    composeTestRule.onNodeWithText("Search").performClick() // go on search bar
    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()
  }
}
