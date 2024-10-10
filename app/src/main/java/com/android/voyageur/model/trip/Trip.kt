package com.android.voyageur.model.trip

import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp

data class Trip(
    val id: String = "",
    val creator: String = "",
    val participants: List<String> = emptyList(),
    val description: String = "",
    val name: String = "",
    val locations: List<Location> = emptyList(),
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val activities: List<Any> = emptyList(), // TODO : replace this with activity model
    val type: TripType = TripType.TOURISM,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Trip

    return id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}

enum class TripType {
  BUSINESS,
  TOURISM,
}
