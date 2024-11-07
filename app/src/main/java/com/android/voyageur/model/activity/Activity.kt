package com.android.voyageur.model.activity

import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp

data class Activity(
    val title: String,
    val description: String = "",
    val location: Location = Location(),
    val startTime: Timestamp = Timestamp(0, 0),
    val endDate: Timestamp = Timestamp(0, 0),
    val estimatedPrice: Number = 0.0,
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

fun Activity.hasEndDate() = endDate != Timestamp(0, 0)
