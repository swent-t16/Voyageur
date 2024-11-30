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

  fun getSentFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteRequest(reqId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getNewId(): String

  fun listenForFriendRequests(
      userId: String,
      onNewRequest: (FriendRequest) -> Unit,
      onDeletedRequest: (String) -> Unit
  )
}
