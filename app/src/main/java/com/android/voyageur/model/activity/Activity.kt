package com.android.voyageur.model.activity

import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp

/**
 * Represents an activity within a trip, including its details such as title, location,
 * timing, and estimated price.
 *
 * @property title The name of the activity.
 * @property description A brief description of the activity.
 * @property location The location where the activity takes place, represented by a [Location] object.
 * @property startTime The start time of the activity, represented as a [Timestamp]. Defaults to an empty [Timestamp].
 * @property endTime The end time of the activity, represented as a [Timestamp]. Defaults to an empty [Timestamp].
 * @property estimatedPrice The estimated cost of the activity. Defaults to 0.0.
 * @property activityType The type of activity, represented by the [ActivityType] enum. Defaults to [ActivityType.OTHER].
 */
data class Activity(
    val title: String = "",
    val description: String = "",
    val location: Location = Location("", "", "", 0.0, 0.0),
    val startTime: Timestamp = Timestamp(0, 0),
    val endTime: Timestamp = Timestamp(0, 0),
    val estimatedPrice: Double = 0.0,
    val activityType: ActivityType = ActivityType.OTHER,
)

/**
 * Enum class representing different types of activities that can be associated with a trip.
 */
enum class ActivityType {
  WALK,
  RESTAURANT,
  MUSEUM,
  SPORTS,
  OUTDOORS,
  TRANSPORT,
  OTHER,
}

/**
 * Checks if the activity has a non-empty description.
 *
 * @return `true` if the description is not empty; otherwise, `false`.
 */
fun Activity.hasDescription() = description != ""

/**
 * Checks if the activity has a valid start time.
 *
 * @return `true` if the start time is not the default empty [Timestamp]; otherwise, `false`.
 */
fun Activity.hasStartTime() = startTime != Timestamp(0, 0)

/**
 * Checks if the activity has a valid end time.
 *
 * @return `true` if the end time is not the default empty [Timestamp]; otherwise, `false`.
 */
fun Activity.hasEndDate() = endTime != Timestamp(0, 0)

/**
 * Determines if the activity is a draft, meaning it lacks a complete set of start and end times.
 *
 * @return `true` if the activity is missing either the start or end time; otherwise, `false`.
 */
fun Activity.isDraft() = !(hasStartTime() && hasEndDate())
