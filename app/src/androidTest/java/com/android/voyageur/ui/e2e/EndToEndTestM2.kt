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
import com.android.voyageur.model.notifications.FriendRequestRepository
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
import com.android.voyageur.ui.overview.AddTripScreen
import com.android.voyageur.ui.overview.OverviewScreen
import com.android.voyageur.ui.profile.EditProfileScreen
import com.android.voyageur.ui.profile.ProfileScreen
import com.android.voyageur.ui.search.SearchScreen
import com.android.voyageur.ui.search.SearchUserProfileScreen
import com.android.voyageur.ui.trip.AddActivityScreen
import com.android.voyageur.ui.trip.TopTabs
import com.android.voyageur.ui.trip.activities.ActivitiesForOneDayScreen
import com.google.android.gms.maps.model.LatLng
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class E2ETestM2 {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var tripRepository: TripRepository
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
    tripsViewModel = TripsViewModel(tripRepository)

    placesRepository = mock(PlacesRepository::class.java)
    placesViewModel = PlacesViewModel(placesRepository)

    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)

    userViewModel = UserViewModel(userRepository, friendRequestRepository = friendRequestRepository)
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

    whenever(tripsViewModel.getNewTripId()).thenReturn("mockTripId")

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)

    Locale.setDefault(Locale.US)
  }

  @Suppress("RememberReturnType")
  @Test
  fun e2ETest() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigation = remember { NavigationActions(navController) }
      NavHost(
          navController = navController,
          startDestination = Route.OVERVIEW,
      ) {
        composable(Route.OVERVIEW) { OverviewScreen(tripsViewModel, navigation, userViewModel) }
        composable(Route.PROFILE) { ProfileScreen(userViewModel, navigation) }
        composable(Route.SEARCH) {
          SearchScreen(userViewModel, placesViewModel, navigation, requirePermission = false)
        }
        composable(Route.TOP_TABS) { TopTabs(tripsViewModel, navigation, userViewModel) }
        composable(Route.SEARCH_USER_PROFILE) { SearchUserProfileScreen(userViewModel, navigation) }
        composable(Screen.ADD_TRIP) { AddTripScreen(tripsViewModel, navigation) }
        composable(Screen.OVERVIEW) { OverviewScreen(tripsViewModel, navigation, userViewModel) }
        composable(Screen.PROFILE) { ProfileScreen(userViewModel, navigation) }
        composable(Screen.EDIT_PROFILE) { EditProfileScreen(userViewModel, navigation) }
        composable(Screen.SEARCH) { SearchScreen(userViewModel, placesViewModel, navigation) }
        composable(Screen.TOP_TABS) { TopTabs(tripsViewModel, navigation, userViewModel) }
        composable(Screen.ADD_ACTIVITY) { AddActivityScreen(tripsViewModel, navigation) }
        composable(Screen.ACTIVITIES_FOR_ONE_DAY) {
          ActivitiesForOneDayScreen(tripsViewModel, navigation)
        }
        composable(Screen.SEARCH_USER_PROFILE) {
          SearchUserProfileScreen(userViewModel, navigation)
        }
      }
    }
    // starting screen is overview
    // check if all the components are displayed
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Your trips").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBarTitle").assertIsDisplayed()

    composeTestRule
        .onNodeWithText("You have no trips yet.")
        .assertIsDisplayed() // no trips initially

    composeTestRule.onNodeWithTag("createTripButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTripButton").performClick()
    composeTestRule.onNodeWithTag("addTrip").assertIsDisplayed()
    // check add trip

    // check trip creation
    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Test Trip")
    composeTestRule.onNodeWithTag("inputTripDescription").performTextInput("Best trip of my life")
    composeTestRule.onNodeWithText("Start Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithText("End Date *").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.onNodeWithTag("tripSave").assertIsEnabled()

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

    val mockTrip1 =
        Trip(
            id = "mockTripId",
            name = "Trip with activities",
            startDate =
                Timestamp(Date.from(LocalDateTime.of(2024, 10, 3, 0, 0).toInstant(ZoneOffset.UTC))),
            endDate =
                Timestamp(
                    Date.from(LocalDateTime.of(2024, 11, 10, 0, 0).toInstant(ZoneOffset.UTC))),
            activities =
                listOf(
                    Activity(
                        "Activity 1",
                        "Museum Visit",
                        Location("", "", "", ""),
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

    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripsViewModel.getTrips()

    composeTestRule
        .onNodeWithText("You have no trips yet.")
        .assertIsNotDisplayed() // We have a trip so no trips text is not displayed

    composeTestRule
        .onNodeWithText("Trip with activities")
        .assertIsDisplayed() // check if trip name is displayed
    composeTestRule
        .onNodeWithText("Oct 03 2024 - Nov 10 2024")
        .assertIsDisplayed() // check if dates are displayed in the correct format

    // go to trip details - schedule/activities/settings
    composeTestRule.onNodeWithText("Trip with activities").performClick()
    assert(tripsViewModel.selectedTrip.value == mockTrip1)

    composeTestRule.onNodeWithTag("topTabs").assertIsDisplayed()
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()
    // default view of activities
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertIsNotDisplayed()

    // Go to weekly
    composeTestRule.onNodeWithText("Weekly").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertIsDisplayed()

    // Back to Daily
    composeTestRule.onNodeWithText("Daily").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()

    // Go to edit trip
    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule.onNodeWithTag("inputTripTitle").assertTextContains("Trip with activities")
    composeTestRule.onNodeWithTag("inputTripTitle").performTextClearance()
    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Changed Title")
    composeTestRule.onNodeWithTag("inputTripTitle").assertTextContains("Changed Title")

    composeTestRule.onNodeWithText("Schedule").performClick()
    // check activity is displayed in daily view
    composeTestRule.onNodeWithText("Activity 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Daily").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Weekly").performClick()
    // check activity is displayed in weekly view
    composeTestRule.onNodeWithText("SEP 30 - OCT 6", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("T 3", useUnmergedTree = true).assertExists()

    // check add activity
    composeTestRule.onNodeWithTag("createActivityButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createActivityButton").performClick()
    composeTestRule.onNodeWithTag("addActivity").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").performClick()

    // check that after going back from add activity the screen is the same(weekly view)
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertIsDisplayed()

    // go to activities screen
    composeTestRule.onNodeWithText("Activities").performClick()
    composeTestRule.onNodeWithText("Activity 1").assertIsDisplayed()

    composeTestRule.onNodeWithText("Schedule").performClick()
    // back to weekly view
    composeTestRule.onNodeWithText("Daily").performClick()
    composeTestRule.waitForIdle()

    // test activities in one day screen
    composeTestRule.onNodeWithText("Activity 1").performClick()
    composeTestRule.onNodeWithTag("activitiesForOneDayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Activity 1").assertIsDisplayed()

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()

    // back to overview
    composeTestRule.onNodeWithText("Overview").performClick()
    composeTestRule.onNodeWithTag("Overview").assertIsDisplayed()

    composeTestRule.onNodeWithText("Search").performClick() // go on search bar
    composeTestRule.onNodeWithTag("searchScreen").assertIsDisplayed()

    val mockUserList = listOf(User(id = "1", name = "John Doe", email = "johndoe@test.com"))
    `when`(userRepository.searchUsers(eq("test"), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(mockUserList)
    }
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    // check searching for user(not case sensitive)
    composeTestRule.onNodeWithTag("searchTextField").performTextClearance()
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("john")
    composeTestRule.onNodeWithTag("searchResultsUsers").assertIsDisplayed()
    // check searching for non-existent user
    composeTestRule.onNodeWithTag("searchTextField").performTextClearance()
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("bob")
    composeTestRule.onNodeWithText("John Doe").assertIsNotDisplayed()

    userViewModel._user.value =
        User(id = "2", name = "Alice", email = "alice@example.com", contacts = mutableListOf())
    val selectedUser =
        User("2", "Alice", "alice@example.com", interests = listOf("Hiking", "Piano"))
    userViewModel._selectedUser.value = selectedUser

    composeTestRule.onNodeWithTag("noInterests").assertIsNotDisplayed()
    // the user has 2 interests

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
    `when`(placesRepository.searchPlaces(eq("Test Place"), any(), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.arguments[1] as (List<Place>) -> Unit
      onSuccess(listOf(customPlace.place))
      null
    }

    // go to search places screen
    composeTestRule.onNodeWithText("PLACES").performClick()
    composeTestRule.onNodeWithTag("searchResultsPlaces").assertIsDisplayed()
    composeTestRule.onNodeWithText("No results found").assertIsDisplayed() // default message

    composeTestRule.onNodeWithTag("searchTextField").performTextClearance()
    composeTestRule.onNodeWithTag("searchTextField").performTextInput("Test")
    composeTestRule.onNodeWithTag("searchTextField").performClick()

    // check and go to profile page
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()

    // mock a user
    val interests = listOf("Potions", "Monsters", "Wizards")
    val user = User("2", "Harry Potter", "harry@potter.com", interests = interests)
    userViewModel._user.value = user
    userViewModel._isLoading.value = false

//    composeTestRule.onNodeWithTag("userName").assertIsDisplayed()
//    composeTestRule.onNodeWithTag("userEmail").assertIsDisplayed()
//    composeTestRule.onNodeWithTag("interestsFlowRow").assertIsDisplayed()
//    interests.forEach { interest -> composeTestRule.onNodeWithText(interest).assertIsDisplayed() }
//
//    composeTestRule.onNodeWithTag("signOutButton").assertIsDisplayed()
//    composeTestRule.onNodeWithTag("editButton").assertIsDisplayed()

    // go to edit profile and add an interest
//    composeTestRule.onNodeWithTag("editButton").performClick()
//    interests.forEach { interest -> composeTestRule.onNodeWithText(interest).assertIsDisplayed() }
//    val newInterest = "Spells"
//    composeTestRule.onNodeWithTag("newInterestField").performTextInput(newInterest)
//    composeTestRule.onNodeWithText(newInterest).assertIsDisplayed()
//    composeTestRule.onNodeWithTag("saveButton").performClick() // save and go back
  }
}
