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
    val location: Location = Location("", "", "", 0.0, 0.0),
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Trip) return false

    return id == other.id &&
        creator == other.creator &&
        participants == other.participants &&
        description == other.description &&
        name == other.name &&
        location == other.location &&
        startDate == other.startDate &&
        endDate == other.endDate &&
        activities == other.activities &&
        type == other.type &&
        imageUri == other.imageUri
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + creator.hashCode()
    result = 31 * result + participants.hashCode()
    result = 31 * result + description.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + location.hashCode()
    result = 31 * result + startDate.hashCode()
    result = 31 * result + endDate.hashCode()
    result = 31 * result + activities.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + imageUri.hashCode()
    return result
  }
}

enum class TripType {
  BUSINESS,
  TOURISM,
}
