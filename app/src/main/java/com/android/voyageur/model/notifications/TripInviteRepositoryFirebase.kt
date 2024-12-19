package com.android.voyageur.model.notifications

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

/**
 * Used for displaying trip invites and notifications Appears as a parameter to the ${UserViewModel}
 *
 * @param db the firestore instance to use
 * @constructor creates a new TripInviteRepositoryFirebase
 * @property db the firestore instance to use
 * @property collectionPath the path to the collection of trip invites
 */
class TripInviteRepositoryFirebase(private val db: FirebaseFirestore) : TripInviteRepository {
  private val collectionPath = "tripInvites"

  override fun init(onSuccess: () -> Unit) {
    db.collection(collectionPath).get().addOnSuccessListener { onSuccess() }
  }

  /**
   * Get the number of trip invites for a user
   *
   * @param userId the user to get the number of trip invites for
   * @param onSuccess callback to execute after successful fetch
   * @param onFailure exception handling callback
   */
  override fun getTripInvitesCount(
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
   * Create a trip invite
   *
   * @param req the trip invite to create
   * @param onSuccess callback to execute after successful creation
   * @param onFailure exception handling callback
   */
  override fun createTripInvite(
      req: TripInvite,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (req.id.isEmpty()) return
    db.collection(collectionPath)
        .document(req.id)
        .set(req, SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Get all trip invites for a user
   *
   * @param userId the user to get the trip invites for
   * @param onSuccess callback to execute after successful fetch
   * @param onFailure exception handling callback
   */
  override fun getTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("to", userId)
        .get()
        .addOnSuccessListener { result ->
          val invites = result.map { document -> document.toObject(TripInvite::class.java) }
          onSuccess(invites)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Delete a trip invite
   *
   * @param invite the trip invite to delete
   * @param onSuccess callback to execute after successful deletion
   * @param onFailure exception handling callback
   */
  override fun deleteTripInvite(
      invite: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(invite)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Get a new trip invite ID
   *
   * @return the new trip invite ID
   */
  override fun getNewId(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Listen to trip invites sent by a user
   *
   * @param userId the user to listen to trip invites for
   * @param onSuccess callback to execute after successful fetch
   * @param onFailure exception handling callback
   * @return a listener registration
   */
  override fun listenToSentTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration {
    return db.collection(collectionPath).whereEqualTo("from", userId).addSnapshotListener {
        snapshot,
        exception ->
      if (exception != null) {
        onFailure(exception)
      } else if (snapshot != null) {
        val requests = snapshot.toObjects(TripInvite::class.java)
        onSuccess(requests)
      }
    }
  }

  /**
   * Listen to trip invites received by a user
   *
   * @param userId the user to listen to trip invites for
   * @param onSuccess callback to execute after successful fetch
   * @param onFailure exception handling callback
   * @return a listener registration
   */
  override fun listenToTripInvites(
      userId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  ): ListenerRegistration {
    return db.collection(collectionPath).whereEqualTo("to", userId).addSnapshotListener {
        snapshot,
        exception ->
      if (exception != null) {
        onFailure(exception)
      } else if (snapshot != null) {
        val requests = snapshot.toObjects(TripInvite::class.java)
        onSuccess(requests)
      }
    }
  }

  /**
   * Get all trip invites for a trip
   *
   * @param tripId the trip to get the trip invites for
   * @param onSuccess callback to execute after successful fetch
   * @param onFailure exception handling callback
   */
  override fun getInvitesForTrip(
      tripId: String,
      onSuccess: (List<TripInvite>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("tripId", tripId)
        .get()
        .addOnSuccessListener { result ->
          val invites = result.map { document -> document.toObject(TripInvite::class.java) }
          onSuccess(invites)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Create a new TripInviteRepository
   *
   * @return a new TripInviteRepository using the Firebase Firestore instance
   */
  companion object {
    fun create(): TripInviteRepository {
      val db = FirebaseFirestore.getInstance()
      return TripInviteRepositoryFirebase(db)
    }
  }
}
