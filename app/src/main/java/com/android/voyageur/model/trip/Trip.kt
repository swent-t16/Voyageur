package com.android.voyageur.model.trip

import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Trip(
    val id: String = "",
    val creator: String = "",
    val participants: List<String> = emptyList(),
    val description: String = "",
    val name: String = "",
    val locations: List<Location> = emptyList(),
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    var activities: List<Activity> = emptyList(),
    val type: TripType = TripType.TOURISM,
    val imageUri: String = "" // default image for trip
) {
  // Exclude from Firestore serialization
  @get:Exclude
  val tripType: TripType
    get() = TripType.valueOf(type.toString())

  override fun equals(other: Any?): Boolean = other is Trip && id == other.id

  override fun hashCode(): Int = id.hashCode()
}

enum class TripType {
  BUSINESS,
  TOURISM,
}
