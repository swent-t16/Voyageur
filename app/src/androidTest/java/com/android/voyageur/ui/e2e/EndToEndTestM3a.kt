package com.android.voyageur.ui.e2e

import androidx.compose.runtime.*
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
import com.android.voyageur.ui.navigation.NavigationState
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.overview.AddTripScreen
import com.android.voyageur.ui.overview.OverviewScreen
import com.android.voyageur.ui.profile.ProfileScreen
import com.android.voyageur.ui.search.SearchScreen
import com.android.voyageur.ui.trip.AddActivityScreen
import com.android.voyageur.ui.trip.TopTabs
import com.android.voyageur.ui.trip.activities.ActivitiesForOneDayScreen
import com.android.voyageur.ui.trip.activities.EditActivityScreen
import com.android.voyageur.ui.trip.assistant.AssistantScreen
import com.google.android.gms.tasks.Task
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
class E2ETestM3a {
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

    whenever(tripsViewModel.getNewTripId()).thenReturn("mockTripId")

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    `when`(navigationActions.getNavigationState()).thenReturn(NavigationState())

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
        composable(Route.TOP_TABS) {
          TopTabs(tripsViewModel, navigation, userViewModel, placesViewModel)
        }
        composable(Screen.ADD_TRIP) {
          AddTripScreen(tripsViewModel, navigation, placesViewModel = placesViewModel)
        }
        composable(Screen.OVERVIEW) { OverviewScreen(tripsViewModel, navigation, userViewModel) }
        composable(Screen.TOP_TABS) {
          TopTabs(tripsViewModel, navigation, userViewModel, placesViewModel)
        }
        composable(Screen.ADD_ACTIVITY) {
          AddActivityScreen(tripsViewModel, navigation, placesViewModel = placesViewModel)
        }
        composable(Screen.ACTIVITIES_FOR_ONE_DAY) {
          ActivitiesForOneDayScreen(tripsViewModel, navigation)
        }
        composable(Screen.EDIT_ACTIVITY) {
          EditActivityScreen(navigation, tripsViewModel, placesViewModel)
        }
        composable(Screen.ASSISTANT) { AssistantScreen(tripsViewModel, navigation, userViewModel) }
      }
    }
    // starting screen is overview
    // check if all the components are displayed
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reverseTripsOrderButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("favoriteFilterButton").assertIsDisplayed()

    composeTestRule
        .onNodeWithText("You have no trips yet.")
        .assertIsDisplayed() // no trips initially

    composeTestRule.onNodeWithTag("createTripButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTripButton").performClick()
    composeTestRule.onNodeWithTag("addTrip").assertIsDisplayed()
    // check add trip components
    composeTestRule.onNodeWithTag("addTripTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("imageContainer").assertExists()
    composeTestRule
        .onNodeWithText("Select Image from Gallery", useUnmergedTree = true)
        .assertExists()
    composeTestRule.onNodeWithTag("inputTripTitle").assertExists()
    composeTestRule.onNodeWithTag("informativeText").assertExists()
    composeTestRule.onNodeWithTag("inputTripDescription").assertExists()
    composeTestRule.onNodeWithTag("searchTextField").assertExists() // location
    composeTestRule.onNodeWithTag("inputStartDate").assertExists()
    composeTestRule.onNodeWithTag("inputEndDate").assertExists()
    composeTestRule.onNodeWithTag("tripTypeDropdown").assertExists()
    composeTestRule.onNodeWithTag("discoverableCheckbox").assertExists()
    composeTestRule.onNodeWithTag("tripSave").assertExists()

    // go back to overview
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()

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
            photos = listOf("photo1_url", "photo2_url", "photo3_url"),
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

    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripsViewModel.getTrips()
    userViewModel._isLoading.value = false

    // check trip card components
    composeTestRule.onNodeWithText("Trip with activities").assertIsDisplayed()
    composeTestRule.onNodeWithText("Oct 03 2024 - Nov 10 2024").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardRow", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("additionalParticipantsText", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("defaultTripImage", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("favoriteButton_${mockTrip1.name}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expandIcon_${mockTrip1.name}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("expandIcon_${mockTrip1.name}").performClick()
    composeTestRule.onNodeWithTag("deleteMenuItem_${mockTrip1.name}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addToCalendarMenuItem_${mockTrip1.name}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("leaveMenuItem_${mockTrip1.name}").assertIsDisplayed()

    // go to trip details
    composeTestRule.onNodeWithTag("cardItem").performClick()
    assert(tripsViewModel.selectedTrip.value == mockTrip1)

    // check daily view details
    composeTestRule.onNodeWithTag("topTabs").assertIsDisplayed()
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed() // activity card
    composeTestRule
        .onNodeWithText("Thursday, 3 October", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Activity 1").assertIsDisplayed()

    // go to one day view
    composeTestRule.onNodeWithText("Activity 1").performClick()
    composeTestRule.onNodeWithTag("activitiesForOneDayScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem_${mockTrip1.activities[0].title}").assertIsDisplayed()
    // expand activity card
    composeTestRule.onNodeWithTag("expandIcon_${mockTrip1.activities[0].title}").performClick()
    composeTestRule.onNodeWithTag("editIcon_${mockTrip1.activities[0].title}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteIcon_${mockTrip1.activities[0].title}").assertIsDisplayed()
    composeTestRule.onNodeWithText("Description", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("Price", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("Type", useUnmergedTree = true).assertExists()
    // total price at the end
    composeTestRule.onNodeWithTag("totalEstimatedPriceBox").assertExists()
    // go back to Schedule Screen
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    composeTestRule.onNodeWithTag("scheduleScreen").assertIsDisplayed()

    // Go to weekly view
    composeTestRule.onNodeWithText("Weekly").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sep 30 - Oct 6").assertExists()
    composeTestRule.onNodeWithText("T 3 - 1 activity").assertExists()

    // check add activity
    composeTestRule.onNodeWithTag("createActivityButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createActivityButton").performClick()
    composeTestRule.onNodeWithTag("addActivity").assertIsDisplayed()
    // check add activity components
    composeTestRule.onNodeWithTag("addActivityTitle").assertTextEquals("Create a New Activity")
    composeTestRule.onNodeWithTag("inputActivityTitle").assertExists()
    composeTestRule.onNodeWithTag("inputActivityDescription").assertExists()
    composeTestRule.onNodeWithTag("searchTextField").assertExists()
    composeTestRule.onNodeWithTag("inputDate").assertExists()
    composeTestRule.onNodeWithTag("inputStartTime").assertExists()
    composeTestRule.onNodeWithTag("inputEndTime").assertExists()
    composeTestRule.onNodeWithTag("inputActivityPrice").assertExists()
    composeTestRule.onNodeWithTag("inputActivityType").assertExists()
    composeTestRule.onNodeWithTag("activitySave").assertExists()
    composeTestRule.onNodeWithTag("goBackButton").performClick()
    // check that after going back from add activity the screen is the same(weekly view)
    composeTestRule.onNodeWithTag("weeklyViewScreen").assertIsDisplayed()
    // Back to Daily - check toggle button
    composeTestRule.onNodeWithText("Daily").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("byDayScreen").assertIsDisplayed()
    // go to assistant
    composeTestRule.onNodeWithText("Ask Assistant").performClick()
    composeTestRule.onNodeWithTag("assistantScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AIRequestTextField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AIRequestButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("settingsButton").performClick()
    composeTestRule.onNodeWithTag("settingsDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("provideFinalActivitiesSwitch").assertIsDisplayed()
    composeTestRule.onNodeWithTag("provideFinalActivitiesSwitch").assertIsOff()
    composeTestRule.onNodeWithTag("useUserInterestsSwitch").assertIsDisplayed()
    composeTestRule.onNodeWithTag("useUserInterestsSwitch").assertIsOff()
    composeTestRule.onNodeWithTag("closeDialogButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("closeDialogButton").performClick()

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    // go to activities screen
    composeTestRule.onNodeWithText("Activities").performClick()
    composeTestRule.onNodeWithTag("activitiesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchField").assertIsDisplayed()
    // check filter button
    composeTestRule.onNodeWithTag("filterButton").performClick()
    composeTestRule.onNodeWithTag("filterActivityAlertDialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Walk", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmButtonDialog").performClick()
    composeTestRule.onNodeWithText("Final", useUnmergedTree = true).assertExists()
    // same activity card as in one day view
    composeTestRule.onNodeWithText("Activity 1").assertExists()
    composeTestRule.onNodeWithTag("expandIcon_${mockTrip1.activities[0].title}").performClick()
    composeTestRule.onNodeWithTag("totalEstimatedPriceBox").assertExists()
    // add activity button is displayed in this screen as well
    composeTestRule.onNodeWithTag("createActivityButton").assertIsDisplayed()

    // go to map screen
    composeTestRule.onNodeWithText("Map").performClick()
    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("GoogleMap").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterButton").performClick()
    composeTestRule.onNodeWithTag("filterByTypeButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("typeFilterActivityAlertDialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel", useUnmergedTree = true).performClick()
    composeTestRule.onNodeWithText("Filter by Date", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("clearFiltersButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Close", useUnmergedTree = true).performClick()

    // Go to Photos Screen
    composeTestRule.onNodeWithText("Photos").performClick()
    composeTestRule.onNodeWithTag("photosScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addPhotoButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("photosTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("lazyVerticalGrid").assertIsDisplayed()
    mockTrip1.photos.forEach { photoUri ->
      composeTestRule.onNodeWithTag("photoThumbnail_$photoUri").assertIsDisplayed()
    }
    // Open photo dialog
    composeTestRule.onNodeWithTag("photoThumbnail_photo1_url").performClick()
    composeTestRule.onNodeWithTag("photoDialog_photo1_url").assertIsDisplayed()

    // Navigate photo list in dialog
    composeTestRule.onNodeWithTag("goRightButton").performClick()
    composeTestRule.onNodeWithTag("goLeftButton").performClick()
    composeTestRule.onNodeWithTag("closeButton_photo1_url").assertIsDisplayed()
    composeTestRule.onNodeWithTag("deleteButton_photo1_url").assertIsDisplayed()

    // Go to edit trip and assert that the fields are filled
    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule.onNodeWithTag("settingsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputTripTitle").assertTextContains("Trip with activities")
    composeTestRule.onNodeWithTag("inputTripTitle").performTextClearance()
    composeTestRule.onNodeWithTag("inputTripTitle").performTextInput("Changed Title")
    composeTestRule.onNodeWithTag("inputTripTitle").assertTextContains("Changed Title")
    composeTestRule.onNodeWithTag("imageContainer").assertExists()
    composeTestRule
        .onNodeWithText("Select Image from Gallery", useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag("inputTripDescription")
        .assertTextContains("Description of the trip")
    composeTestRule.onNodeWithTag("searchTextField").assertExists()
    composeTestRule.onNodeWithTag("inputStartDate").assertExists()
    composeTestRule.onNodeWithTag("inputEndDate").assertExists()
    composeTestRule.onNodeWithTag("tripTypeDropdown").assertExists()
    composeTestRule.onNodeWithTag("discoverableCheckbox").assertHasClickAction()
    composeTestRule.onNodeWithTag("tripSave").assertIsEnabled()
  }
}
