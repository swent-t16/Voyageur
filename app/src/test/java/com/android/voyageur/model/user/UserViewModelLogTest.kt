import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserRepository
import com.android.voyageur.model.user.UserViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserViewModelLogTest {

  private lateinit var userRepository: UserRepository
  private lateinit var friendRequestRepository: FriendRequestRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser
  private lateinit var logMock: MockedStatic<Log>

  @Before
  fun setUp() {
    // Initialize FirebaseApp
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())

    // Mock dependencies
    userRepository = mock(UserRepository::class.java)
    friendRequestRepository = mock(FriendRequestRepository::class.java)
    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java).apply { `when`(uid).thenReturn("123") }

    // Mock static FirebaseAuth
    logMock = mockStatic(Log::class.java)
    val mockedFirebaseAuth = mockStatic(FirebaseAuth::class.java)

    mockedFirebaseAuth.use {
      it.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(firebaseAuth)
      `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
    }

    userViewModel =
        UserViewModel(
            userRepository = userRepository, friendRequestRepository = friendRequestRepository)
  }

  @After
  fun tearDown() {
    logMock.close()
  }

  @Test
  fun updateUser_logsSuccessMessage() = runTest {
    val user = User(id = "123", name = "Test User")

    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .whenever(userRepository)
        .updateUser(any(), any(), anyOrNull())

    userViewModel.updateUser(user)

    // Verify the success log
    logMock.verify { Log.d(eq("USER_UPDATE"), eq("Successfully updated user: 123")) }
  }

  @Test
  fun updateUser_logsFailureMessage() = runTest {
    val user = User(id = "123", name = "Test User")
    val exception = Exception("Update failed")

    doAnswer { invocation ->
          val onFailure: (Exception) -> Unit = invocation.getArgument(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .updateUser(any(), any(), anyOrNull())

    userViewModel.updateUser(user)

    // Adjust the expected message to include the exception class
    val expectedMessage = "Failed to update user: $exception"

    // Verify the error log
    logMock.verify { Log.e(eq("USER_UPDATE"), eq(expectedMessage)) }
  }

  @Test
  fun clearFriendRequestState_logsSuccessMessage() = runTest {
    val friendRequestId = "req_123"
    val friendRequest = FriendRequest(id = friendRequestId, from = "123", to = "456")
    userViewModel._friendRequests.value = listOf(friendRequest)

    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .whenever(friendRequestRepository)
        .deleteRequest(eq(friendRequestId), any(), anyOrNull())

    userViewModel.clearFriendRequestState(friendRequest.to)

    // Verify the success log
    logMock.verify { Log.d(eq("FRIEND_REQUEST"), eq("Deleted request: $friendRequestId")) }
  }

  @Test
  fun clearFriendRequestState_logsFailureMessage() = runTest {
    val friendRequestId = "req_123"
    val friendRequest = FriendRequest(id = friendRequestId, from = "123", to = "456")
    val exception = Exception("Delete failed")
    userViewModel._friendRequests.value = listOf(friendRequest)

    doAnswer {
          val onFailure = it.getArgument<(Exception) -> Unit>(2)
          onFailure(exception)
          null
        }
        .whenever(friendRequestRepository)
        .deleteRequest(eq(friendRequestId), any(), anyOrNull())

    userViewModel.clearFriendRequestState(friendRequest.to)

    // Verify the error log
    logMock.verify {
      Log.e(
          eq("FRIEND_REQUEST"),
          eq("Failed to delete request: $friendRequestId, ${exception.message}"))
    }
  }

  @Test
  fun removeContact_logsFailureMessage() = runTest {
    val currentUser = User(id = "123", contacts = listOf("456"))
    val exception = Exception("User not found")
    userViewModel._user.value = currentUser

    doAnswer { invocation ->
          val onFailure: (Exception) -> Unit = invocation.getArgument(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(any(), any(), anyOrNull())

    userViewModel.removeContact("456")

    // Verify the actual log message
    logMock.verify { Log.e(eq("USER_VIEW_MODEL"), eq("User not found")) }
  }

  @Test
  fun acceptFriendRequest_logsFailureToUpdateCurrentUser() = runTest {
    val friendRequest = FriendRequest(id = "req_123", from = "456", to = "123")
    val exception = Exception("Failed to update current user")
    val currentUser = User(id = "123", contacts = listOf())

    userViewModel._user.value = currentUser

    // Mock updateUser to fail when updating current user
    doAnswer { invocation ->
          val onFailure: (Exception) -> Unit = invocation.getArgument(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .updateUser(eq(currentUser.copy(contacts = listOf("456"))), any(), any())

    userViewModel.acceptFriendRequest(friendRequest)

    // Verify the error log
    logMock.verify {
      Log.e(eq("ACCEPT_REQUEST"), eq("Failed to update current user: ${exception.message}"))
    }
  }

  @Test
  fun acceptFriendRequest_logsFailureToUpdateSender() = runTest {
    val friendRequest = FriendRequest(id = "req_123", from = "456", to = "123")
    val exception = Exception("Failed to update sender")
    val currentUser = User(id = "123", contacts = listOf())
    val senderUser = User(id = "456", contacts = listOf())

    userViewModel._user.value = currentUser

    // Mock updateUser to succeed when updating current user
    doAnswer { invocation ->
          val onSuccess: () -> Unit = invocation.getArgument(1)
          onSuccess()
          null
        }
        .whenever(userRepository)
        .updateUser(eq(currentUser.copy(contacts = listOf("456"))), any(), any())

    // Mock getUsersByIds to return senderUser
    doAnswer { invocation ->
          val onSuccess: (List<User>) -> Unit = invocation.getArgument(1)
          onSuccess(listOf(senderUser))
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(eq(listOf("456")), any(), any())

    // Mock updateUser to fail when updating sender
    doAnswer { invocation ->
          val onFailure: (Exception) -> Unit = invocation.getArgument(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .updateUser(eq(senderUser.copy(contacts = listOf("123"))), any(), any())

    userViewModel.acceptFriendRequest(friendRequest)

    // Verify the error log
    logMock.verify {
      Log.e(eq("ACCEPT_REQUEST"), eq("Failed to update sender: ${exception.message}"))
    }
  }

  @Test
  fun deleteFriendRequest_logsSuccessMessage() = runTest {
    val reqId = "req_123"

    doAnswer {
          val onSuccess: () -> Unit = it.getArgument(1)
          onSuccess()
          null
        }
        .whenever(friendRequestRepository)
        .deleteRequest(eq(reqId), any(), anyOrNull())

    userViewModel.deleteFriendRequest(reqId)

    // Verify the success log
    logMock.verify { Log.d(eq("FRIEND_REQUEST"), eq("Friend request $reqId successfully deleted")) }
  }

  @Test
  fun deleteFriendRequest_logsFailureMessage() = runTest {
    val reqId = "req_123"
    val exception = Exception("Delete failed")

    doAnswer { invocation ->
          val onFailure: (Exception) -> Unit = invocation.getArgument(2)
          onFailure(exception)
          null
        }
        .whenever(friendRequestRepository)
        .deleteRequest(eq(reqId), any(), any())

    userViewModel.deleteFriendRequest(reqId)

    // Verify the error log
    logMock.verify {
      Log.e(eq("FRIEND_REQUEST"), eq("Failed to delete friend request: ${exception.message}"))
    }
  }

  @Test
  fun getFriendRequests_logsFailureMessage() = runTest {
    val exception = Exception("Failed to fetch friend requests")

    doAnswer { invocation ->
          val onFailure: (Exception) -> Unit = invocation.getArgument(2)
          onFailure(exception)
          null
        }
        .whenever(friendRequestRepository)
        .getFriendRequests(any(), any(), any())

    userViewModel.getFriendRequests {}

    // Verify the error log
    logMock.verify { Log.e(eq("USER_VIEW_MODEL"), eq(exception.message.orEmpty())) }
  }

  @Test
  fun getNotificationsCount_logsFailureMessage() = runTest {
    val exception = Exception("Failed to get notifications count")

    doAnswer { invocation ->
          val onFailure: (Exception) -> Unit = invocation.getArgument(2)
          onFailure(exception)
          null
        }
        .whenever(friendRequestRepository)
        .getNotificationCount(any(), any(), any())

    userViewModel.getNotificationsCount {}

    // Verify the error log
    logMock.verify { Log.e(eq("USER_VIEW_MODEL"), eq(exception.message.orEmpty())) }
  }

  @Test
  fun getUsersByIds_logsFailureMessage() = runTest {
    val exception = Exception("Failed to fetch users by IDs")

    doAnswer { invocation ->
          val onFailure: (Exception) -> Unit = invocation.getArgument(2)
          onFailure(exception)
          null
        }
        .whenever(userRepository)
        .fetchUsersByIds(any(), any(), any())

    userViewModel.getUsersByIds(listOf("123")) {}

    // Verify the error log
    logMock.verify { Log.e(eq("USER_VIEW_MODEL"), eq(exception.message.orEmpty())) }
  }
}
