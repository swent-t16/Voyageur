package com.android.voyageur.model.activity

import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp
import java.util.Date

data class Activity(
    val title: String = "",
    val description: String = "",
    val location: Location = Location(""),
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now(),
    val estimatedPrice: Double = 0.0,
    val activityType: ActivityType = ActivityType.WALK,
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
