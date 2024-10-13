package com.android.voyageur.model.user

import com.android.voyageur.model.trip.Trip

data class User(
    val id: String,
    var name: String = "",
    val email: String = "",
    var profilePicture: String = "",
    var bio: String = "",
    private val _trips: MutableList<Trip> = mutableListOf(),
    private val _friends: MutableList<User> = mutableListOf()
) {
  val trips: List<Trip>
    get() = _trips.toList()

  val friends: List<User>
    get() = _friends.toList()

  fun addTrip(trip: Trip) {
    _trips.add(trip)
  }

  fun removeTrip(trip: Trip) {
    _trips.remove(trip)
  }

  fun addFriend(friend: User) {
    _friends.add(friend)
  }

  fun removeFriend(friend: User) {
    _friends.remove(friend)
  }
}
