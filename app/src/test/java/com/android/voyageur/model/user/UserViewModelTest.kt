package com.android.voyageur.model.user

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import org.mockito.kotlin.anyOrNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UserViewModelTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var userViewModel: UserViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Initialize FirebaseApp only if it's not already initialized
        val context: Context = RuntimeEnvironment.getApplication()

        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:1234567890:android:abcdef")
                .setApiKey("your_api_key")
                .setDatabaseUrl("https://your-database-url.firebaseio.com")
                .build()
            FirebaseApp.initializeApp(context, options)
        }

        userViewModel = UserViewModel(userRepository)
    }

    @Test
    fun updateUser_success() = runTest {
        val user = User("123", "Jane Doe", "jane@example.com")
        `when`(userRepository.updateUser(anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer {
            val onSuccess = it.getArgument<() -> Unit>(1)
            onSuccess()
        }

        userViewModel.updateUser(user)

        assert(userViewModel.user.value == user)
        assert(!userViewModel.isLoading.value)
    }

    @Test
    fun signOutUser() {
        userViewModel.signOutUser()

        assert(userViewModel.user.value == null)
    }
}