package com.android.voyageur.model.notifications

import com.google.firebase.firestore.ListenerRegistration

interface FriendRequestRepository {

  fun init(onSuccess: () -> Unit)

  fun getFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getNotificationCount(
      userId: String,
      onSuccess: (Long) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun createRequest(req: FriendRequest, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getSentFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteRequest(reqId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getNewId(): String

  fun listenToSentFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration

  fun listenToFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration
}
