package com.android.voyageur.model.notifications

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FriendRequestRepositoryFirebase(private val db: FirebaseFirestore) : FriendRequestRepository {
  private val collectionPath = "friendRequests"

  /**
   * @param userId the user for who to fetch the friend requests (to field)
   * @param onSuccess callback to execute after successful fetch
   * @param onFailure exception handling callback
   */
  override fun getFriendRequests(
      userId: String,
      onSuccess: (List<FriendRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (userId.isEmpty()) onSuccess(listOf())
    else
        db.collection(collectionPath)
            .whereEqualTo("to", userId)
            .get()
            .addOnSuccessListener { documents ->
              onSuccess(documents.toObjects(FriendRequest::class.java))
            }
            .addOnFailureListener { exception -> onFailure(exception) }
  }
  /**
   * @param userId the user for who to fetch the notifications count
   * @param onSuccess callback to execute after successful fetch
   * @param onFailure exception handling callback
   */
  override fun getNotificationCount(
      userId: String,
      onSuccess: (Long) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("to", userId)
        .count()
        .get(AggregateSource.SERVER)
        .addOnSuccessListener { onSuccess(it.count) }
        .addOnFailureListener { exception -> onFailure(exception) }
  }
  /**
   * @param req friend request to be created
   * @param onSuccess callback to execute after successful operation
   * @param onFailure exception handling callback
   */
  override fun createRequest(
      req: FriendRequest,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(req.id)
        .set(req, SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun init(onSuccess: () -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> }
  }

  companion object {
    fun create(): FriendRequestRepository {
      val db = FirebaseFirestore.getInstance()
      return FriendRequestRepositoryFirebase(db)
    }
  }

  override fun getNewId(): String {
    return db.collection(collectionPath).document().id
  }
}
