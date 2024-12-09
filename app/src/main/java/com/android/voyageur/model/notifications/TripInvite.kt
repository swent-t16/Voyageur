package com.android.voyageur.model.notifications

data class TripInvite(
    val id: String,
    val tripId: String,
    val from: String,
    val to: String,
) {
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

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + tripId.hashCode()
    result = 31 * result + from.hashCode()
    result = 31 * result + to.hashCode()
    return result
  }
}
