package com.android.voyageur.model.activity

import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp
import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val title: String = "",
    val description: String = "",
    val location: Location = Location(""),
    val startTime: Timestamp = Timestamp(0, 0),
    val endTime: Timestamp = Timestamp(0, 0),
    val estimatedPrice: Double = 0.0,
    val activityType: ActivityType = ActivityType.OTHER,
)

enum class ActivityType {
  WALK,
  RESTAURANT,
  MUSEUM,
  SPORTS,
  OUTDOORS,
  TRANSPORT,
  OTHER,
}

fun Activity.hasDescription() = description != ""

fun Activity.hasStartTime() = startTime != Timestamp(0, 0)

fun Activity.hasEndDate() = endTime != Timestamp(0, 0)

fun Activity.isDraft() = !(hasStartTime() && hasEndDate())
