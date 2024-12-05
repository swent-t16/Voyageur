package com.android.voyageur.model.trip

import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

/**
 * Represents a trip, including details such as its name, participants, location, activities, and
 * type.
 *
 * @property id Unique identifier for the trip.
 * @property creator The user ID of the person who created the trip.
 * @property participants A list of user IDs participating in the trip.
 * @property description A brief description of the trip.
 * @property name The name of the trip.
 * @property location The location associated with the trip.
 * @property startDate The start date of the trip, stored as a Firebase [Timestamp].
 * @property endDate The end date of the trip, stored as a Firebase [Timestamp].
 * @property activities A list of [Activity] instances planned for the trip.
 * @property type The type of the trip, as defined by the [TripType] enum.
 * @property imageUri A URI string pointing to an image associated with the trip.
 */
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
    val imageUri: String = "", // default image for trip
    val photos: List<String> = emptyList()
) {
  /**
   * A computed property that retrieves the [TripType] of the trip, excluding it from Firestore
   * serialization.
   */
  @get:Exclude
  val tripType: TripType
    get() = TripType.valueOf(type.toString())

  /**
   * Checks equality between this [Trip] and another object. Two trips are considered equal if all
   * their properties match.
   *
   * @param other The object to compare.
   * @return `true` if the objects are equal, otherwise `false`.
   */
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
        imageUri == other.imageUri &&
        photos == other.photos
  }

  /**
   * Generates a hash code for the [Trip] object based on its properties.
   *
   * @return The hash code value.
   */
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
    result = 31 * result + photos.hashCode()
    return result
  }
}

/** Enum representing the type of a trip - Business or Tourism. */
enum class TripType {
  BUSINESS,
  TOURISM,
}
