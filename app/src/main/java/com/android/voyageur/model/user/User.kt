package com.android.voyageur.model.user

import com.android.voyageur.model.trip.Trip

data class User(
    val id: String = "",
    var name: String = "",
    var email: String = "",
    var profilePicture: String = "",
    var bio: String = "",
    var contacts: List<String> = mutableListOf(),
    var interests: List<String> = mutableListOf(),
    var username: String = "",
    var favoriteTrips: List<Trip> = mutableListOf()
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as User

    if (id != other.id) return false
    if (name != other.name) return false
    if (email != other.email) return false
    if (profilePicture != other.profilePicture) return false
    if (bio != other.bio) return false
    if (!contacts.containsAll(other.contacts) || !other.contacts.containsAll(contacts)) return false
    if (!interests.containsAll(other.interests) || !other.interests.containsAll(interests))
        return false
    if (username != other.username) return false
      if (!favoriteTrips.containsAll(other.favoriteTrips) || !other.favoriteTrips.containsAll(favoriteTrips))
          return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + email.hashCode()
    result = 31 * result + profilePicture.hashCode()
    result = 31 * result + bio.hashCode()
    result = 31 * result + contacts.toSet().hashCode()
    result = 31 * result + interests.toSet().hashCode()
    result = 31 * result + username.hashCode()
    result = 31 * result + favoriteTrips.toSet().hashCode()
    return result
  }

    fun addFavoriteTrip(trip: Trip) {
        favoriteTrips = favoriteTrips.toMutableList().apply { add(trip) }
    }
    fun removeFavoriteTrip(trip: Trip) {
        favoriteTrips = favoriteTrips.toMutableList().apply { remove(trip) }
    }
}
