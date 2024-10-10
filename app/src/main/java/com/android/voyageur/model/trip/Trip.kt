package com.android.voyageur.model.trip

import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp

data class Trip(
    val id: String,
    val creator: String,
    val participants: List<String>,
    val description: String,
    val name: String,
    val locations: List<Location>,
    val startDate: Timestamp,
    val endDate: Timestamp,
    val activities: List<Any>, // TODO : replace this with activity model
    val type: TripType,
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
