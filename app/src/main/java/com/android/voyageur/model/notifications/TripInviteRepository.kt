package com.android.voyageur.model.notifications

import com.google.firebase.firestore.ListenerRegistration

interface TripInviteRepository {

  fun init(onSuccess: () -> Unit)

  fun getTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getTripInvitesCount(userId: String, onSuccess: (Long) -> Unit, onFailure: (Exception) -> Unit)

  fun createTripInvite(req: TripInvite, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteTripInvite(invite: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getNewId(): String

  fun listenToSentTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration

  fun listenToTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration

  fun getInvitesForTrip(
      tripId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
