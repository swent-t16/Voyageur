package com.android.voyageur.model.user

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserViewModelTest {
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  private val user = User("1", "name", "email", "password", "phone")

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }
    userRepository = mock(UserRepository::class.java)
    userViewModel = UserViewModel(userRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun setQueryUpdatesQuery() {
    val query = "test"
    userViewModel.setQuery(query)
    assert(userViewModel.query.value == query)
  }

  @Test
  fun setQueryTriggersSearchAfterDelay() = runTest {
    val query1 = "test1"
    val query2 = "test2"
    userViewModel.setQuery(query1)
    assert(userViewModel.query.value == query1)
    verify(userRepository, never()).searchUsers(eq(query1), any(), any())
    userViewModel.setQuery(query2)
    // Wait for debounce delay
    delay(200)
    verify(userRepository).searchUsers(eq(query2), any(), any())
  }

  @Test
  fun testSearchUsersSucceeds() = runTest {
    val query = "test"
    val mockUserList = listOf(User(id = "1", name = "Test User", email = "test@example.com"))

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (List<User>) -> Unit
          onSuccess(mockUserList)
        }
        .`when`(userRepository)
        .searchUsers(eq(query), any(), any())

    userViewModel.searchUsers(query)

    verify(userRepository).searchUsers(eq(query), any(), any())

    assert(userViewModel.searchedUsers.value == mockUserList)
  }

  @Test
  fun testSearchUsersFails() = runTest {
    val query = "test"
    val exception = Exception("Search failed")

    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(exception)
        }
        .`when`(userRepository)
        .searchUsers(eq(query), any(), any())

    userViewModel.searchUsers(query)

    verify(userRepository).searchUsers(eq(query), any(), any())

    assert(userViewModel.searchedUsers.value.isEmpty())
    assert(!userViewModel.isLoading.value)
  }
}
