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

  override fun hashCode(): Int = id.hashCode()

  override fun equals(other: Any?): Boolean {
    if (javaClass != other?.javaClass) return false

    other as Trip

    if (id != other.id) return false
    if (creator != other.creator) return false
    if (!participants.containsAll(other.participants) ||
        !other.participants.containsAll(participants))
        return false
    if (description != other.description) return false
    if (name != other.name) return false
    if (!locations.containsAll(other.locations) || !other.locations.containsAll(locations))
        return false
    if (startDate != other.startDate) return false
    if (endDate != other.endDate) return false
    if (!activities.containsAll(other.activities) || !other.activities.containsAll(activities))
        return false
    if (type != other.type) return false
    if (imageUri != other.imageUri) return false
    if (tripType != other.tripType) return false

    return true
  }
}

enum class TripType {
  BUSINESS,
  TOURISM,
}
