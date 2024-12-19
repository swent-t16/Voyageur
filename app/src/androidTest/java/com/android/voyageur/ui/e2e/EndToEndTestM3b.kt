package com.android.voyageur.ui.e2e

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.notifications.TripInvite
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.place.CustomPlace
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
import com.android.voyageur.ui.overview.OverviewScreen
import com.android.voyageur.ui.profile.EditProfileScreen
import com.android.voyageur.ui.profile.ProfileScreen
import com.android.voyageur.ui.search.PlaceDetailsScreen
import com.android.voyageur.ui.search.SearchScreen
import com.android.voyageur.ui.search.SearchUserProfileScreen
import com.android.voyageur.ui.trip.TopTabs
import com.android.voyageur.ui.trip.activities.ActivitiesForOneDayScreen
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.OpeningHours
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
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
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class E2ETestM3b {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var tripRepository: TripRepository
  private lateinit var tripInviteRepository: TripInviteRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripsViewModel: TripsViewModel

  private lateinit var placesRepository: PlacesRepository
  private lateinit var placesViewModel: PlacesViewModel

  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel

  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser
  private lateinit var mockAuthResult: AuthResult
  private lateinit var mockAuthTask: Task<AuthResult>
  private lateinit var friendRequestRepository: FriendRequestRepository

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    tripInviteRepository = mock(TripInviteRepository::class.java)
    tripsViewModel = TripsViewModel(tripRepository, tripInviteRepository)

    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)

    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)

    firebaseAuth = mock(FirebaseAuth::class.java)
    mockUser = mock(FirebaseUser::class.java)
    mockAuthResult = mock(AuthResult::class.java)
    mockAuthTask = mock(Task::class.java) as Task<AuthResult>

    `when`(firebaseAuth.currentUser).thenReturn(mockUser)
    `when`(mockAuthTask.isSuccessful).thenReturn(true)
    `when`(mockAuthTask.result).thenReturn(mockAuthResult)

    `when`(mockUser.uid).thenReturn("2")
    `when`(mockUser.displayName).thenReturn("Test User")
    `when`(mockUser.email).thenReturn("test@example.com")
    `when`(mockUser.photoUrl).thenReturn(null)

    `when`(firebaseAuth.signInWithCredential(any())).thenReturn(mockAuthTask)

    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user = User(userId, "Test User", "test@example.com", interests = emptyList())
          onSuccess(user)
          null
        }
        .`when`(userRepository)
        .getUserById(anyString(), anyOrNull(), anyOrNull())

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

    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(emptyList()) // Return empty list or desired data
          null
        }
        .`when`(friendRequestRepository)
        .getFriendRequests(anyString(), anyOrNull(), anyOrNull())

    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(Long) -> Unit>(1)
          onSuccess(0L) // Return zero notifications
          null
        }
        .`when`(friendRequestRepository)
        .getNotificationCount(anyString(), anyOrNull(), anyOrNull())

    userViewModel =
        UserViewModel(
            userRepository,
            firebaseAuth,
            friendRequestRepository,
            addAuthStateListener = false // Prevent adding the AuthStateListener during tests
            )
    userViewModel.shouldFetch = false

    whenever(tripsViewModel.getNewTripId()).thenReturn("mockTripId")

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)

    Locale.setDefault(Locale.US)
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
          SearchScreen(
              userViewModel, placesViewModel, tripsViewModel, navigation, requirePermission = false)
        }
        composable(Route.SEARCH_USER_PROFILE) { SearchUserProfileScreen(userViewModel, navigation) }
        composable(Route.EDIT_PROFILE) {
          EditProfileScreen(userViewModel, navigation, tripsViewModel)
        }
        composable(Route.TOP_TABS) {
          TopTabs(tripsViewModel, navigation, userViewModel, placesViewModel)
        }
        composable(Screen.OVERVIEW) { OverviewScreen(tripsViewModel, navigation, userViewModel) }
        composable(Screen.PROFILE) { ProfileScreen(userViewModel, tripsViewModel, navigation) }
        composable(Screen.EDIT_PROFILE) {
          EditProfileScreen(
              userViewModel,
              navigation,
              tripsViewModel = tripsViewModel,
          )
        }
        composable(Screen.TOP_TABS) {
          TopTabs(tripsViewModel, navigation, userViewModel, placesViewModel)
        }
        composable(Screen.SEARCH) {
          SearchScreen(userViewModel, placesViewModel, tripsViewModel, navigation)
        }
        composable(Screen.SEARCH_USER_PROFILE) {
          SearchUserProfileScreen(userViewModel, navigation)
        }
        composable(Screen.ACTIVITIES_FOR_ONE_DAY) {
          ActivitiesForOneDayScreen(tripsViewModel, navigation)
        }
        composable(Screen.PLACE_DETAILS) { PlaceDetailsScreen(navigation, placesViewModel) }
      }
    }
    // starting screen is overview
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()

    composeTestRule.onNodeWithText("Search").performClick() // go on search bar
    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()

    val mockUserList =
        listOf(
            User(id = "1", name = "John Doe", email = "johndoe@test.com", username = "john_doe"),
            User(id = "2", name = "Bob", email = "bob@test.com", username = "BobTheBob"))
    `when`(userRepository.searchUsers(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(mockUserList)
    }
    userViewModel.searchUsers("")
    // current user is john
    userViewModel._user.value =
        User(id = "1", name = "John Doe", email = "johndoe@test.com", username = "john_doe")

    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchScreenContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tabRow").assertIsDisplayed()
    // check we are in search users
    composeTestRule.onNodeWithTag("filterButton_USERS").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchResultsUsers").assertIsDisplayed()
    // check user components
    composeTestRule.onNodeWithTag("userItem_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userItem_2").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("John Doe's profile picture").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("@john_doe", useUnmergedTree = true).assertIsDisplayed()
    // just for bob, as john is the current user
    composeTestRule.onNodeWithText("Add", useUnmergedTree = true).assertIsDisplayed()

    // check searching for user(not case sensitive)
    composeTestRule.onNodeWithTag("searchTextField").performTextClearance()
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("john")

    userViewModel._selectedUser.value =
        User(id = "1", name = "John Doe", email = "johndoe@test.com", username = "john_doe")
    // go to profile of the user
    composeTestRule.onNodeWithTag("userItem_1").performClick()
    composeTestRule.onNodeWithTag("userProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("defaultProfilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userEmail").assertIsDisplayed()
    // go back
    composeTestRule.onNodeWithTag("userProfileBackButton", useUnmergedTree = true).performClick()

    val place =
        Place.builder()
            .setName("Test Place")
            .setAddress("123 Test St")
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
    `when`(placesRepository.searchPlaces(any(), any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.arguments[1] as (List<Place>) -> Unit
      onSuccess(listOf(customPlace.place))
      null
    }
    placesViewModel.searchPlaces("", null)
    // go to search places screen
    composeTestRule.onNodeWithText("PLACES").performClick()
    // check map view
    composeTestRule.onNodeWithTag("toggleMapViewButton").performClick()
    composeTestRule.onNodeWithTag("googleMap").assertIsDisplayed()
    // go back
    composeTestRule.onNodeWithTag("toggleMapViewButton").performClick()
    composeTestRule.onNodeWithTag("searchResultsPlaces").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noResults").assertIsDisplayed()

    // go to discover trips page
    composeTestRule.onNodeWithText("DISCOVER", useUnmergedTree = true).performClick()
    composeTestRule.onNodeWithTag("discoverTab").assertIsDisplayed()
    composeTestRule.onNodeWithText("Search results", useUnmergedTree = true).assertExists()

    // no trips initially
    composeTestRule.onNodeWithTag("noTripsFound").assertIsDisplayed()
    composeTestRule.onNodeWithText("No trips to discover").assertIsDisplayed()
    composeTestRule.onNodeWithText("Please check back later.").assertIsDisplayed()
    val mockTrip1 =
        Trip(
            id = "mockTripId",
            name = "Trip with activities",
            description = "Description of the trip",
            participants = listOf("Alex", "Mihai", "Ioana", "Andrei", "Maria", "Matei"),
            startDate =
                Timestamp(Date.from(LocalDateTime.of(2024, 10, 3, 0, 0).toInstant(ZoneOffset.UTC))),
            endDate =
                Timestamp(
                    Date.from(LocalDateTime.of(2024, 11, 10, 0, 0).toInstant(ZoneOffset.UTC))),
            discoverable = true,
            activities =
                listOf(
                    Activity(
                        "Activity 1",
                        "Museum Visit",
                        Location(""),
                        startTime =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 3, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        endTime =
                            Timestamp(
                                Date.from(
                                    LocalDateTime.of(2024, 10, 3, 14, 0)
                                        .toInstant(ZoneOffset.UTC))),
                        0.0,
                        ActivityType.MUSEUM)))
    val mockTrips = listOf(mockTrip1)

    `when`(tripRepository.getFeed(any(), any(), any())).thenAnswer {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripsViewModel.getFeed("")

    // details of discoverable trip
    composeTestRule.onNodeWithTag("pager").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tripCard_mockTripId").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Trip Image").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trip with activities").assertIsDisplayed()
    composeTestRule.onNodeWithText("Description of the trip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("copyTripDetailsButton").assertIsDisplayed()
    // go to trip read-only view
    composeTestRule.onNodeWithTag("viewTripDetailsButton").performClick()
    composeTestRule.onNodeWithTag("topTabs").assertIsDisplayed()
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed() // activity card
    composeTestRule
        .onNodeWithText("Thursday, 3 October", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("createActivityButton").assertIsNotDisplayed()
    // check one day view
    composeTestRule.onNodeWithText("Activity 1").performClick()
    composeTestRule.onNodeWithTag("activitiesForOneDayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_${mockTrip1.activities[0].title}").assertIsDisplayed()
    // expand activity card - edit and delete should not be displayed
    composeTestRule.onNodeWithTag("expandIcon_${mockTrip1.activities[0].title}").performClick()
    composeTestRule
        .onNodeWithTag("editIcon_${mockTrip1.activities[0].title}")
        .assertIsNotDisplayed()
    composeTestRule
        .onNodeWithTag("deleteIcon_${mockTrip1.activities[0].title}")
        .assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Description", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("Price", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("Type", useUnmergedTree = true).assertExists()
    // total price at the end
    composeTestRule.onNodeWithTag("totalEstimatedPriceBox").assertExists()
    // go back to Schedule Screen
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // Go to weekly view
    composeTestRule.onNodeWithText("Weekly").performClick()
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sep 30 - Oct 6").assertExists()
    composeTestRule.onNodeWithText("T 3 - 1 activity").assertExists()

    // components that should not be displayed in read-only mode
    composeTestRule.onNodeWithTag("createActivityButton").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Ask Assistant").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Map").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Photos").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Settings").assertIsNotDisplayed()

    // go to activities screen
    composeTestRule.onNodeWithText("Activities").performClick()
    composeTestRule.onNodeWithTag("activitiesScreen").assertIsDisplayed()

    // go to profile screen
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()
    // mock a user
    val interests = listOf("Hiking", "Ski", "Swimming")
    val user =
        User(
            id = "1",
            name = "John Doe",
            email = "johndoe@test.com",
            username = "john_doe",
            interests = interests)
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

    composeTestRule.onNodeWithTag("profileScreenContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userEmail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("interestsFlowRow").assertIsDisplayed()
    interests.forEach { interest -> composeTestRule.onNodeWithText(interest).assertIsDisplayed() }

    composeTestRule.onNodeWithTag("signOutButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editButton").assertIsDisplayed()

    // mock a friend request and check is displayed
    val mockFriendRequests = listOf(FriendRequest(from = "user1", to = "user2"))
    userViewModel._friendRequests.value = mockFriendRequests
    userViewModel._notificationUsers.value =
        listOf(User(id = "user1", name = "User One", profilePicture = "http://example.com/pic.jpg"))

    composeTestRule.onNodeWithTag("friendRequestCard").assertExists()
    composeTestRule.onNodeWithTag("friendRequestBox").assertExists()
    // Assert: Check that the LazyColumn is displayed
    composeTestRule.onNodeWithTag("friendRequestLazyColumn").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("friendRequest", useUnmergedTree = true).assertCountEquals(1)
    composeTestRule.onNodeWithTag("profilePicture", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("User One", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("acceptButton", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("denyButton", useUnmergedTree = true).assertIsDisplayed()

    // mock a trip invite and check is displayed
    val invite = TripInvite(id = "invite1", from = "user1", to = "user2", tripId = "trip1")
    tripsViewModel.set_tripInvites(listOf(invite))

    composeTestRule.onNodeWithTag("tripInviteCard").assertIsDisplayed()

    // go to edit profile and add an interest
    composeTestRule.onNodeWithTag("editButton").performClick()
    composeTestRule.onNodeWithTag("editProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("defaultProfilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editImageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailField").assertIsDisplayed()
    interests.forEach { interest -> composeTestRule.onNodeWithText(interest).assertIsDisplayed() }
    val newInterest = "Golf"
    composeTestRule.onNodeWithTag("newInterestField").performTextInput(newInterest)
    composeTestRule.onNodeWithText(newInterest).assertIsDisplayed()
    composeTestRule.onNodeWithTag("saveButton").assertExists()
    composeTestRule.onNodeWithTag("goBack", useUnmergedTree = true).performClick()

    composeTestRule.onNodeWithTag("signOutButton").performClick()
  }
}
