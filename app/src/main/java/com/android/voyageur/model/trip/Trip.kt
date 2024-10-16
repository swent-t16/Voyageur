package com.android.voyageur.model.trip

import com.android.voyageur.R
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
    val activities: List<Any> = emptyList(), // TODO : replace this with activity model
    val type: TripType = TripType.TOURISM,
    val imageId: Int = R.drawable.default_trip_image // default image for trip
) {
  // Exclude from Firestore serialization
  @get:Exclude
  val tripType: TripType
    get() = TripType.valueOf(type.toString())

  override fun equals(other: Any?): Boolean {
    return other is Trip && id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}

enum class TripType {
  BUSINESS,
  TOURISM
}
