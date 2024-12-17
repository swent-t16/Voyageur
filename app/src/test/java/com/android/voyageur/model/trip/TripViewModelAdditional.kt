package com.android.voyageur.model.trip

import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.notifications.TripInvite
import com.android.voyageur.model.notifications.TripInviteRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TripsViewModelTestAdditional {

  private lateinit var tripsRepository: TripRepository
  private lateinit var tripInviteRepository: TripInviteRepository
  private lateinit var tripsViewModel: TripsViewModel

  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var firebaseUser: FirebaseUser

  private val trip =
      Trip(
          id = "1",
          participants = listOf("123"),
          description = "Test Trip Description",
          name = "Test Trip",
          location = Location("City", "Country", "Address", 0.0, 0.0),
          startDate = Timestamp.now(),
          endDate = Timestamp.now(),
          activities = listOf(Activity("Hiking", "Mountain Hiking Adventure")),
          type = TripType.TOURISM,
          imageUri = "https://example.com/image.jpg",
          photos = listOf("https://example.com/photo1.jpg", "https://example.com/photo2.jpg"),
          discoverable = true)

  @Before
  fun setUp() {
    tripsRepository = mock(TripRepository::class.java)
    tripInviteRepository = mock(TripInviteRepository::class.java)

    firebaseAuth = mock(FirebaseAuth::class.java)
    firebaseUser = mock(FirebaseUser::class.java)

    `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
    `when`(firebaseUser.uid).thenReturn("123")

    tripsViewModel = TripsViewModel(tripsRepository, tripInviteRepository, true, firebaseAuth)
  }

  @Test
  fun acceptTripInvite_successfulFlow() = runTest {
    val tripInvite = TripInvite(id = "1", tripId = "1", from = "user2", to = "123")

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (Trip) -> Unit
          onSuccess(trip)
          null
        }
        .`when`(tripsRepository)
        .getTripById(any(), any(), any())

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as () -> Unit
          onSuccess()
          null
        }
        .`when`(tripsRepository)
        .updateTrip(any(), any(), any())

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as () -> Unit
          onSuccess()
          null
        }
        .`when`(tripInviteRepository)
        .deleteTripInvite(any(), any(), any())

    tripsViewModel.acceptTripInvite(tripInvite)
    advanceUntilIdle()

    verify(tripsRepository).getTripById(any(), any(), any())
    verify(tripsRepository).updateTrip(any(), any(), any())
    verify(tripInviteRepository).deleteTripInvite(any(), any(), any())
  }

  @Test
  fun acceptTripInvite_fetchTripFailure() = runTest {
    val tripInvite = TripInvite(id = "1", tripId = "1", from = "user2", to = "123")

    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(Exception("Failed to fetch trip"))
          null
        }
        .`when`(tripsRepository)
        .getTripById(any(), any(), any())

    tripsViewModel.acceptTripInvite(tripInvite)
    advanceUntilIdle()

    verify(tripsRepository).getTripById(any(), any(), any())
    verify(tripsRepository, never()).updateTrip(any(), any(), any())
    verify(tripInviteRepository, never()).deleteTripInvite(any(), any(), any())
  }

  @Test
  fun acceptTripInvite_updateTripFailure() = runTest {
    val tripInvite = TripInvite(id = "1", tripId = "1", from = "user2", to = "123")

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (Trip) -> Unit
          onSuccess(trip)
          null
        }
        .`when`(tripsRepository)
        .getTripById(any(), any(), any())

    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(Exception("Failed to update trip"))
          null
        }
        .`when`(tripsRepository)
        .updateTrip(any(), any(), any())

    tripsViewModel.acceptTripInvite(tripInvite)
    advanceUntilIdle()

    verify(tripsRepository).getTripById(any(), any(), any())
    verify(tripsRepository).updateTrip(any(), any(), any())
    verify(tripInviteRepository, never()).deleteTripInvite(any(), any(), any())
  }

  @Test
  fun acceptTripInvite_deleteInviteFailure() = runTest {
    val tripInvite = TripInvite(id = "1", tripId = "1", from = "user2", to = "123")

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as (Trip) -> Unit
          onSuccess(trip)
          null
        }
        .`when`(tripsRepository)
        .getTripById(any(), any(), any())

    doAnswer { invocation ->
          val onSuccess = invocation.arguments[1] as () -> Unit
          onSuccess()
          null
        }
        .`when`(tripsRepository)
        .updateTrip(any(), any(), any())

    doAnswer { invocation ->
          val onFailure = invocation.arguments[2] as (Exception) -> Unit
          onFailure(Exception("Failed to delete invite"))
          null
        }
        .`when`(tripInviteRepository)
        .deleteTripInvite(any(), any(), any())

    tripsViewModel.acceptTripInvite(tripInvite)
    advanceUntilIdle()

    verify(tripsRepository).getTripById(any(), any(), any())
    verify(tripsRepository).updateTrip(any(), any(), any())
    verify(tripInviteRepository).deleteTripInvite(any(), any(), any())
  }
}
