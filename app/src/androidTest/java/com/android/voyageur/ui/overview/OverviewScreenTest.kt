package com.android.voyageur.ui.overview

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
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
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.TimeZone
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class OverviewScreenTest {
  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripViewModel: TripsViewModel
  private lateinit var tripInviteRepository: TripInviteRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    tripInviteRepository = mock(TripInviteRepository::class.java)
    tripViewModel = TripsViewModel(tripRepository, tripInviteRepository)
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)

    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)

    // Mock methods of FirebaseUser to return non-null values
    `when`(firebaseUser.uid).thenReturn("123")
    `when`(firebaseUser.displayName).thenReturn("Test User")
    `when`(firebaseUser.email).thenReturn("test@example.com")
    `when`(firebaseUser.photoUrl).thenReturn(null) // Or a valid URI if needed
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user = User(userId, "Test User", "test@example.com", interests = emptyList())
          onSuccess(user)
          null
        }
        .`when`(userRepository)
        .getUserById(anyString(), anyOrNull(), anyOrNull())

    // Mock userRepository.listenToUser to call onSuccess with a User
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(User) -> Unit>(1)
          val user = User(userId, "Test User", "test@example.com", interests = emptyList())
          onSuccess(user)
          null
        }
        .`when`(userRepository)
        .listenToUser(anyString(), anyOrNull(), anyOrNull())

    // Mock userRepository.fetchUsersByIds to return an empty list
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<User>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .`when`(userRepository)
        .fetchUsersByIds(anyOrNull(), anyOrNull(), anyOrNull())

    // Mock friendRequestRepository.getFriendRequests to return an empty list
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(List<FriendRequest>) -> Unit>(1)
          onSuccess(emptyList()) // Return empty list or desired data
          null
        }
        .`when`(friendRequestRepository)
        .getFriendRequests(anyString(), anyOrNull(), anyOrNull())

    // Mock friendRequestRepository.getNotificationCount to return zero
    doAnswer { invocation ->
          val userId = invocation.getArgument<String>(0)
          val onSuccess = invocation.getArgument<(Long) -> Unit>(1)
          onSuccess(0L) // Return zero notifications
          null
        }
        .`when`(friendRequestRepository)
        .getNotificationCount(anyString(), anyOrNull(), anyOrNull())

    // Create the UserViewModel with the mocked userRepository and firebaseAuth
    userViewModel =
        UserViewModel(
            userRepository,
            firebaseAuth,
            friendRequestRepository,
            addAuthStateListener = false // Prevent adding the AuthStateListener during tests
            )
    userViewModel.shouldFetch = false

    whenever(tripViewModel.getNewTripId()).thenReturn("mockTripId")
    doNothing().`when`(tripRepository).updateTrip(any(), any(), any())

    // Mocking initial navigation state

    `when`(navigationActions.currentRoute()).thenReturn(Route.OVERVIEW)
    `when`(navigationActions.getNavigationState()).thenReturn(NavigationState())
    composeTestRule.setContent { OverviewScreen(tripViewModel, navigationActions, userViewModel) }
    // set a non-null user for tests
    userViewModel._user.value = User()
  }

  @Test
  fun displayTextWhenEmpty() {
    `when`(tripRepository.getTrips(eq(""), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf())
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("emptyTripPrompt").assertIsDisplayed()
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun overviewDisplaysCard() {
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                participants = listOf("Alex", "Mihai", "Ioana", "Andrei", "Maria", "Matei"),
                name = "Paris Trip"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardRow", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("additionalParticipantsText", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun noParticipantsDisplaysNoAvatars() {
    val mockTrips = listOf(Trip(id = "23", participants = emptyList(), name = "Paris Trip"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("lazyColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cardItem").assertIsDisplayed()
    composeTestRule.onNodeWithTag("participantAvatar").assertDoesNotExist()
  }

  @Test
  fun clickingTripCardNavigatesToTripDetails() {
    val mockTrip = Trip(id = "1", participants = listOf("Alex", "Mihai"), name = "Paris Trip")
    val mockTrips = listOf(mockTrip)

    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }

    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("cardItem").performClick()

    verify(navigationActions).navigateTo(screen = Screen.TOP_TABS)
  }

  @Test
  fun clickingTripCardUpdatesNavigationState() {
    val mockTrip = Trip(id = "1", participants = listOf("Alex", "Mihai"), name = "Paris Trip")
    val mockTrips = listOf(mockTrip)

    // Simulate getting the mock trip from the repository
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }

    tripViewModel.getTrips()

    // Simulate clicking the trip card
    composeTestRule.onNodeWithTag("cardItem").performClick()

    // Verify the trip is selected
    assert(tripViewModel.selectedTrip.value == mockTrip)

    // Verify the navigation state is updated
    val navigationState = navigationActions.getNavigationState()
    assert(navigationState.currentTabIndexForTrip == 0)
    assert(navigationState.isDailyViewSelected)
  }

  @Test
  fun createTripButtonCallsNavActions() {
    composeTestRule.onNodeWithTag("overviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTripButton").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Floating action button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("createTripButton").performClick()
    verify(navigationActions).navigateTo(screen = Screen.ADD_TRIP)
  }

  @Test
  fun helperStringReturnsCorrectStrings() {
    val result0 = generateParticipantString(0)
    val result1 = generateParticipantString(1)
    val result2 = generateParticipantString(2)
    assert(result0 == "No participants.")
    assert(result1 == "1 Participant:")
    assert(result2 == "2 Participants:")
  }

  @Test
  fun tripWithImageDisplaysImage() {
    val mockTrip =
        Trip(
            id = "1",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "https://example.com/image.jpg",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("tripImage", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("defaultTripImage").assertDoesNotExist()
  }

  @Test
  fun tripWithoutImageDisplaysDefaultImage() {
    val mockTrip =
        Trip(
            id = "1",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("defaultTripImage", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("tripImage").assertDoesNotExist()
  }

  @Test
  fun deleteTripMethodIsCalled() {
    val mockTrip =
        Trip(
            id = "1",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("expandIcon_${mockTrip.name}").performClick()
    composeTestRule.onNodeWithText("Delete").performClick()
    composeTestRule.onNodeWithText("Remove").performClick()

    verify(tripRepository).deleteTripById(eq(mockTrip.id), any(), any())
  }

  @Test
  fun leaveTripMethod() {
    val user = User("123", "Jane Doe", "jane@example.com", interests = emptyList())
    userViewModel._user.value = user
    userViewModel._isLoading.value = false
    val mockTrip =
        Trip(
            id = "1",
            participants = listOf(user.id),
            name = "Paris Trip",
            imageUri = "",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("expandIcon_${mockTrip.name}").performClick()
    composeTestRule.onNodeWithTag("leaveMenuItem_${mockTrip.name}").assertIsDisplayed()
    composeTestRule.onNodeWithText("Leave Trip").performClick()
    composeTestRule.onNodeWithText("Leave").performClick()
    composeTestRule.waitForIdle()
    val updatedParticipants = mockTrip.participants.filter { it != user.id }
    val updatedTrip = mockTrip.copy(participants = updatedParticipants)
    tripViewModel.selectedTrip.value?.participants?.isEmpty()?.let { assert(it) }
  }

  @Test
  fun leaveTripUserNotAParticipant() {
    val userId = "mockUserId"
    val trip =
        Trip(id = "1", participants = listOf("anotherUserId")) // User is not in participants list

    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(trip))
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("expandIcon_${trip.name}").performClick()
    composeTestRule.onNodeWithText("Leave Trip").performClick()
    composeTestRule.onNodeWithText("Leave").performClick()
    composeTestRule.waitForIdle()

    // Verify no update is triggered
    verify(tripRepository, never()).updateTrip(any(), any(), any())
  }

  @Test
  fun searchField_filtersTrips() {
    val mockTrip =
        Trip(
            id = "1",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "https://example.com/image.jpg",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()

    // Enter search query
    composeTestRule.onNodeWithTag("searchField").performTextInput("1")

    // Check if only matching trip is shown
    composeTestRule.onNodeWithText("1").assertExists()
  }

  @Test
  fun noResultsFound_isDisplayed_whenSearchHasNoMatches() {
    // Given - Set up trips with specific names
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                name = "Paris Trip",
                startDate = Timestamp.now(),
                endDate = Timestamp.now()),
            Trip(
                id = "2",
                name = "London Adventure",
                startDate = Timestamp.now(),
                endDate = Timestamp.now()))

    // When - Set up trips in repository
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    // Perform search with no matches
    composeTestRule.onNodeWithTag("searchField").performTextInput("Berlin")

    // Then - Verify NoResultsFound is displayed
    composeTestRule.onNodeWithTag("noSearchResults").assertIsDisplayed()

    // Verify trip cards are not shown
    composeTestRule.onAllNodesWithTag("cardItem").assertCountEquals(0)
  }

  @Test
  fun noResultsFound_isNotDisplayed_whenSearchHasMatches() {
    // Given
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                name = "Paris Trip",
                startDate = Timestamp.now(),
                endDate = Timestamp.now()))

    // When
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    // Perform search with matches
    composeTestRule.onNodeWithTag("searchField").performTextInput("Paris")

    // Then
    composeTestRule.onNodeWithTag("noResults").assertDoesNotExist()
    composeTestRule.onAllNodesWithTag("cardItem").assertCountEquals(1)
  }

  @Test
  fun noResultsFound_isNotDisplayed_whenSearchFieldIsEmpty() {
    // Given
    val mockTrips =
        listOf(
            Trip(
                id = "1",
                name = "Paris Trip",
                startDate = Timestamp.now(),
                endDate = Timestamp.now()))

    // When
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    // Then - verify NoResultsFound is not displayed with empty search
    composeTestRule.onNodeWithTag("noResults").assertDoesNotExist()
    composeTestRule.onAllNodesWithTag("cardItem").assertCountEquals(1)
  }

  @Test
  fun addTripToCalendarItemIsDisplayed() {
    val mockTrip =
        Trip(
            id = "1",
            participants = listOf("Alex"),
            name = "Paris Trip",
            imageUri = "",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(listOf(mockTrip))
    }
    tripViewModel.getTrips()
    composeTestRule.onNodeWithTag("expandIcon_${mockTrip.name}").performClick()
    composeTestRule
        .onNodeWithTag("addToCalendarMenuItem_${mockTrip.name}")
        .assertIsDisplayed() // Assert the item is displayed
    composeTestRule
        .onNodeWithTag("addToCalendarMenuItem_${mockTrip.name}")
        .performClick() // Click on adding a calendar
  }

  @Test
  fun openGoogleCalendarCreatesCorrectIntent() {
    val mockTrip =
        Trip(
            id = "1",
            participants = listOf("Alex"),
            name = "Paris Trip",
            startDate = Timestamp.now(),
            endDate = Timestamp.now())

    // Mock context
    val context = mock(Context::class.java)

    openGoogleCalendar(context, mockTrip)

    // Capture the intent passed to startActivity
    val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
    verify(context).startActivity(intentCaptor.capture())

    val capturedIntent = intentCaptor.value

    // Assert the intent properties correspond to trip values
    assertEquals(Intent.ACTION_INSERT, capturedIntent.action)
    assertEquals(CalendarContract.Events.CONTENT_URI, capturedIntent.data)
    assertEquals(mockTrip.name, capturedIntent.getStringExtra(CalendarContract.Events.TITLE))
    assertEquals(
        mockTrip.startDate.toDate().time,
        capturedIntent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1))
    assertEquals(
        mockTrip.description, capturedIntent.getStringExtra(CalendarContract.Events.DESCRIPTION))
    assertEquals(
        mockTrip.startDate.toDate().time,
        capturedIntent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1))
    assertEquals(
        mockTrip.endDate.toDate().time,
        capturedIntent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1))
    assertEquals(
        TimeZone.getDefault().id,
        capturedIntent.getStringExtra(CalendarContract.Events.EVENT_TIMEZONE))
  }

  @Test
  fun nullUserDoesNotComposeScreen() {
    userViewModel._user.value = null
    composeTestRule.onNodeWithTag("overviewScreen").assertDoesNotExist()
  }

  @Test
  fun favoriteButtons_exist() {
    val mockTrips = listOf(Trip(id = "1", name = "Trip 1"), Trip(id = "2", name = "Trip 2"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("favoriteFilterButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("favoriteButton_Trip 1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("favoriteButton_Trip 2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("favoriteButton_Trip 3").assertDoesNotExist()
  }

  @Test
  fun clickingOnFavoriteButton_addsTripToFavorites() {
    val mockTrips = listOf(Trip(id = "1", name = "Trip 1"), Trip(id = "2", name = "Trip 2"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("favoriteButton_Trip 1").performClick()

    verify(userRepository).updateUser(eq(User().copy(favoriteTrips = listOf("1"))), any(), any())
  }

  @Test
  fun toggleFavoriteFilter_updatesTripsDisplay() {
    val mockTrips = listOf(Trip(id = "1", name = "Trip 1"), Trip(id = "2", name = "Trip 2"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    // Set user with favorite trips
    userViewModel._user.value = User(favoriteTrips = listOf("1"))

    composeTestRule.onNodeWithText("Trip 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trip 2").assertIsDisplayed()

    composeTestRule.onNodeWithTag("favoriteFilterButton").performClick()

    composeTestRule.onNodeWithText("Trip 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trip 2").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("favoriteFilterButton").performClick()

    composeTestRule.onNodeWithText("Trip 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trip 2").assertIsDisplayed()
  }

  @Test
  fun favoriteFilter_noFavorites_showsEmptyState() {
    val mockTrips = listOf(Trip(id = "1", name = "Trip 1"), Trip(id = "2", name = "Trip 2"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    composeTestRule.onNodeWithTag("favoriteFilterButton").performClick()

    composeTestRule.onNodeWithTag("emptyTripPrompt").assertIsDisplayed()
    composeTestRule.onNodeWithText("You have no favorite trips yet.").assertIsDisplayed()
  }

  @Test
  fun nonExistentTrip_doNotAppearInFavoriteList() {
    val mockTrips = listOf(Trip(id = "1", name = "Trip 1"), Trip(id = "2", name = "Trip 2"))
    `when`(tripRepository.getTrips(any(), any(), any())).then {
      it.getArgument<(List<Trip>) -> Unit>(1)(mockTrips)
    }
    tripViewModel.getTrips()

    // Set user with favorite trips
    userViewModel._user.value = User(favoriteTrips = listOf("3", "2"))

    composeTestRule.onNodeWithTag("favoriteFilterButton").performClick()

    verify(userRepository).updateUser(eq(User().copy(favoriteTrips = listOf("2"))), any(), any())
  }
}
