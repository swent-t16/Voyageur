package com.android.voyageur.model.notifications

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

  fun getNewId(): String
}
