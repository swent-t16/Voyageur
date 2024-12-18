import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.voyageur.model.notifications.TripInvite
import com.android.voyageur.model.notifications.TripInviteRepository
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.ui.formFields.UserDropdown
import com.android.voyageur.ui.formFields.UserIcon
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class DropDownTest {
  private lateinit var tripRepository: TripRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var tripInviteRepository: TripInviteRepository
  private lateinit var tripsViewModel: TripsViewModel
  private lateinit var mockTripsViewModel: TripsViewModel
  private lateinit var firebaseAuth: FirebaseAuth
  private val sampleTrip = Trip(name = "Sample Trip", participants = listOf("userId123"))
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    tripRepository = mock(TripRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    tripInviteRepository = mock(TripInviteRepository::class.java)
    mockTripsViewModel = mock(TripsViewModel::class.java)
    tripsViewModel =
        TripsViewModel(
            tripsRepository = tripRepository,
            tripInviteRepository = tripInviteRepository,
            firebaseAuth = firebaseAuth)

    whenever(tripInviteRepository.getNewId()).thenReturn("123")
    whenever(firebaseAuth.uid).thenReturn("mockUserId")
    whenever(firebaseAuth.uid.orEmpty()).thenReturn("mockUserId")
    whenever(tripRepository.getNewTripId()).thenReturn("mockTripId")
    whenever(navigationActions.currentRoute()).thenReturn(Screen.ADD_TRIP)
  }

  @Test
  fun userIcon_displaysProfilePic() {
    composeTestRule.setContent {
      UserIcon(User(name = "Alice", profilePicture = "https://test.com/profile.jpg"))
    }
    // Assert that the participant profile picture is displayed
    composeTestRule.onNodeWithTag("profilePic").assertExists()
  }

  // Tests for UserDropdown
  @Test
  fun userDropdown_displaysParticipants() {
    val users =
        listOf(Pair(User(name = "Alice", profilePicture = "https://test.com/profile.jpg"), true))

    composeTestRule.setContent {
      UserDropdown(
          users = users,
          tripId = "testTripId",
          tripsViewModel = tripsViewModel,
          onRemove = { _, _ -> })
    }
    // Assert that the participant profile picture is displayed
    composeTestRule.onNodeWithTag("profilePic", useUnmergedTree = true).assertExists()

    // Verify the label "Participants" does not exist because participants are selected
    composeTestRule.onNodeWithText("Participants").assertDoesNotExist()
  }

  @Test
  fun userDropdown_sendInviteButton() {
    val mockUser = User("mockId", "Mock User")
    val users = listOf(mockUser to false) // User is not selected
    composeTestRule.setContent {
      UserDropdown(
          users = users,
          tripsViewModel = tripsViewModel,
          tripId = "testTripId",
          onRemove = { _, _ -> })
    }
    composeTestRule.onNodeWithTag("expander").assertExists().performClick()
    // Press on invite button to send invitation
    composeTestRule.onNodeWithTag("inviteButton_${mockUser.id}").assertExists().performClick()
  }

  @Test
  fun userDropdown_removeInviteButton() {
    // the mockUser has the id of the user which is already a participant to a trip
    tripsViewModel.selectTrip(sampleTrip)
    val mockUser = User("userId123", "Mock User")
    val users = listOf(mockUser to false) // User is not selected
    composeTestRule.setContent {
      UserDropdown(
          users = users,
          tripsViewModel = tripsViewModel,
          tripId = "testTripId",
          onRemove = { _, _ -> })
    }
    composeTestRule.onNodeWithTag("expander").assertExists().performClick()
    // Press on remove button to remove participant from trip
    composeTestRule.onNodeWithTag("removeButton_${mockUser.id}").assertExists().performClick()
  }

  @Test
  fun userDropdown_cancelInviteButton() {
    // the mockUser has the id of the user which is already a participant to a trip
    val mockTripInvite =
        TripInvite(
            id = "mockedInviteId",
            tripId = "testTripId",
            from = "mockUserId",
            to = "userId123",
            accepted = false // Default value for new invites
            )
    tripsViewModel.set_tripInvites(listOf(mockTripInvite))
    val mockUser = User("userId123", "Mock User")
    val users = listOf(mockUser to false) // User is not selected
    composeTestRule.setContent {
      UserDropdown(
          users = users,
          tripsViewModel = tripsViewModel,
          tripId = "testTripId",
          onRemove = { _, _ -> })
    }
    composeTestRule.onNodeWithTag("expander").assertExists().performClick()
    // Press on cancel button to cancel sent invitation
    composeTestRule.onNodeWithTag("cancelButton_${mockUser.id}").assertExists().performClick()
  }
}
