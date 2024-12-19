package com.android.voyageur.model.notifications

/**
 * Represents a trip invitation sent from one user to another.
 *
 * A `TripInvite` encapsulates the details of an invitation for a user to join a specific trip.
 * It includes information about the invite ID, the trip being invited to, the sender, the recipient,
 * and whether the invitation has been accepted.
 *
 * @property id A unique identifier for the trip invite.
 * @property tripId The unique identifier of the trip the invitation is for.
 * @property from The unique identifier of the user who sent the invitation.
 * @property to The unique identifier of the user who received the invitation.
 * @property accepted A flag indicating whether the invitation has been accepted. Defaults to `false`.
 */
data class TripInvite(
    val id: String = "",
    val tripId: String = "",
    val from: String = "",
    val to: String = "",
    val accepted: Boolean = false
) {
    /**
     * Determines if this `TripInvite` is equal to another object.
     *
     * Two `TripInvite` objects are considered equal if they have the same values for the `id`,
     * `tripId`, `from`, and `to` properties.
     *
     * @param other The object to compare with this instance.
     * @return `true` if the other object is equal to this instance, otherwise `false`.
     */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as TripInvite

    if (id != other.id) return false
    if (tripId != other.tripId) return false
    if (from != other.from) return false
    if (to != other.to) return false

    return true
  }

    /**
     * Computes a hash code for this `TripInvite`.
     *
     * The hash code is computed based on the `id`, `tripId`, `from`, and `to` properties.
     *
     * @return An integer hash code for the `TripInvite` instance.
     */
  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + tripId.hashCode()
    result = 31 * result + from.hashCode()
    result = 31 * result + to.hashCode()
    return result
  }
}
