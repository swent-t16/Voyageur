package com.android.voyageur.model.user

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.voyageur.model.notifications.FriendRequest
import com.android.voyageur.model.notifications.FriendRequestRepository
import com.android.voyageur.ui.notifications.AndroidNotificationProvider
import com.android.voyageur.ui.notifications.AndroidStringProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserViewModelTestsAdditional {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var friendRequestRepository: FriendRequestRepository

    @Mock
    private lateinit var listenerRegistration: ListenerRegistration

    private lateinit var context: Context
    private lateinit var notificationProvider: AndroidNotificationProvider
    private lateinit var stringProvider: AndroidStringProvider
    private lateinit var viewModel: UserViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        context = ApplicationProvider.getApplicationContext<Application>()
        FirebaseApp.initializeApp(context)

        notificationProvider = AndroidNotificationProvider(context)
        stringProvider = AndroidStringProvider(context)

        doReturn("testUserId").`when`(firebaseAuth).uid

        // Initialize ViewModel with mocked dependencies
        viewModel = UserViewModel(
            userRepository = userRepository,
            firebaseAuth = firebaseAuth,
            friendRequestRepository = friendRequestRepository,
            notificationProvider = notificationProvider,
            stringProvider = stringProvider,
            addAuthStateListener = false
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `getSentFriendRequests should handle empty userId`() {
        // Given
        doReturn("").`when`(firebaseAuth).uid

        // When
        viewModel.getSentFriendRequests()

        // Then
        verify(friendRequestRepository, never()).listenToSentFriendRequests(
            anyString(),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `getSentFriendRequests should handle repository failure`() {
        // Given
        val testException = Exception("Test error")

        doAnswer { invocation ->
            val onFailure = invocation.arguments[2] as (Exception) -> Unit
            onFailure(testException)
            listenerRegistration
        }.`when`(friendRequestRepository).listenToSentFriendRequests(
            anyString(),
            anyOrNull(),
            anyOrNull()
        )

        // When
        viewModel.getSentFriendRequests()

        // Then
        assertEquals(emptyList<FriendRequest>(), viewModel.sentFriendRequests.value)
    }
}