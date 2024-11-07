package com.android.voyageur.model.activity

import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp

data class Activity(
    val title: String,
    val description: String,
    val location: Location,
    val startTime: Timestamp,
    val endTime: Timestamp,
    val estimatedPrice: Number,
    val activityType: ActivityType,
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
